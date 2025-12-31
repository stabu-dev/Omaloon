package omaloon.world.patterns;

import arc.*;
import arc.math.geom.*;
import arc.struct.*;
import mindustry.game.EventType.*;
import mindustry.world.*;
import omaloon.type.shape.*;

import static mindustry.Vars.*;

public class PatternManager{
    private static final ObjectMap<Tile, PatternAnchor> anchorMap = new ObjectMap<>();
    private static final IntMap<ObjectMap<Block, Tile>> tileToAnchorMap = new IntMap<>();

    private static final IntSet dirtyTiles = new IntSet();
    private static final Bits globalToResolve = new Bits();
    private static boolean updateQueued = false;
    private static boolean initialized = false;

    private static final ObjectMap<Block, Bits> visitedPool = new ObjectMap<>();
    private static final ObjectMap<Block, Bits> claimedPool = new ObjectMap<>();
    private static final ObjectMap<Block, Bits> processedPool = new ObjectMap<>();

    public static void register(){
        Events.on(TileOverlayChangeEvent.class, event -> {
            if(event.overlay instanceof Patterned || event.previous instanceof Patterned){
                updateAround(event.tile);
                for(int i = 0; i < 4; i++){
                    Tile near = event.tile.nearby(i);
                    if(near != null) updateAround(near);
                }
            }
        });

        Events.on(WorldLoadEvent.class, event -> rebuild());
    }

    private static Bits getBits(ObjectMap<Block, Bits> pool, Block b){
        Bits bits = pool.get(b);
        int size = world.width() * world.height();
        if(bits == null || bits.numBits() < size){
            pool.put(b, bits = new Bits(size));
        }
        return bits;
    }

    public static void rebuild(){
        if(world.tiles == null) return;
        anchorMap.clear();
        tileToAnchorMap.clear();
        dirtyTiles.clear();
        globalToResolve.clear();
        updateQueued = false;
        initialized = true;

        globalToResolve.set(0, world.width() * world.height());
        
        ObjectMap<Block, Bits> resolved = new ObjectMap<>();
        resolveTiles(resolved);

        if(!headless && renderer != null && renderer.blocks != null){
            renderer.blocks.floor.reload();
        }
    }

    public static void updateAround(Tile tile){
        if(tile == null || world.isGenerating() || world.tiles == null) return;
        if(dirtyTiles.add(tile.array()) && !updateQueued){
            updateQueued = true;
            Core.app.post(PatternManager::processDirtyTiles);
        }
    }

    private static void processDirtyTiles(){
        if(!initialized) rebuild();
        if(dirtyTiles.isEmpty()){
            updateQueued = false;
            return;
        }

        IntSeq dirty = new IntSeq();
        var it = dirtyTiles.iterator();
        while(it.hasNext) dirty.add(it.next());
        dirtyTiles.clear();
        updateQueued = false;

        visitedPool.each((b, bits) -> bits.clear());
        
        globalToResolve.clear();
        ObjectSet<PatternAnchor> toRemove = new ObjectSet<>();

        for(int i = 0; i < dirty.size; i++){
            int pos = dirty.get(i);
            
            ObjectMap<Block, Tile> existing = tileToAnchorMap.get(pos);
            if(existing != null){
                existing.each((block, anchorTile) -> {
                    PatternAnchor anchor = anchorMap.get(anchorTile);
                    if(anchor != null) toRemove.add(anchor);
                });
            }

            Tile tile = world.tiles.geti(pos);
            if(tile == null) continue;

            if(tile.floor() instanceof Patterned p) handleDirty(tile, p, toRemove);
            if(tile.block() instanceof Patterned p) handleDirty(tile, p, toRemove);
            if(tile.overlay() instanceof Patterned p) handleDirty(tile, p, toRemove);
        }

        toRemove.each(anchor -> {
            anchor.shape.each((x, y) -> {
                if(anchor.shape.get(x, y)){
                    Tile member = world.tile(anchor.tile.x + x, anchor.tile.y + y);
                    if(member != null){
                        int mpos = member.array();
                        globalToResolve.set(mpos);
                        
                        ObjectMap<Block, Tile> map = tileToAnchorMap.get(mpos);
                        if(map != null){
                            map.remove((Block)anchor.patterned);
                            if(map.isEmpty()) tileToAnchorMap.remove(mpos);
                        }
                    }
                }
            });
            anchorMap.remove(anchor.tile);
        });

        if(!globalToResolve.isEmpty()){
            claimedPool.each((b, bits) -> bits.clear());
            resolveTiles(claimedPool);

            if(!headless && renderer != null && renderer.blocks != null){
                IntSet chunks = new IntSet();
                int width = world.width();
                for(int i = globalToResolve.nextSetBit(0); i >= 0; i = globalToResolve.nextSetBit(i + 1)){
                    int cx = (i % width) / 30;
                    int cy = (i / width) / 30;
                    if(chunks.add(Point2.pack(cx, cy))){
                        renderer.blocks.floor.recacheTile(i % width, i / width);
                    }
                }
            }
        }

        if(!dirtyTiles.isEmpty() && !updateQueued){
            updateQueued = true;
            Core.app.post(PatternManager::processDirtyTiles);
        }
    }

    private static void handleDirty(Tile tile, Patterned p, ObjectSet<PatternAnchor> toRemove){
        Block pBlock = (Block)p;
        Bits visited = getBits(visitedPool, pBlock);
        if(visited.get(tile.array())) return;

        findAndMarkContiguous(tile, p, toRemove);
    }

    private static void findAndMarkContiguous(Tile startTile, Patterned patterned, ObjectSet<PatternAnchor> toRemove){
        Block pBlock = (Block)patterned;
        Bits visited = getBits(visitedPool, pBlock);
        
        IntSeq stack = new IntSeq();
        stack.add(startTile.array());
        int width = world.width(), height = world.height();

        while(stack.size > 0){
            int popped = stack.pop();
            int x = popped % width, y = popped / width;
            if(visited.get(popped)) continue;

            int x1 = x;
            while(x1 >= 0 && hasPatterned(world.tile(x1, y), patterned) && !visited.get(x1 + y * width)) x1--;
            x1++;
            boolean spanAbove = false, spanBelow = false;
            while(x1 < width && hasPatterned(world.tile(x1, y), patterned) && !visited.get(x1 + y * width)){
                int pos = x1 + y * width;
                visited.set(pos);
                PatternManager.globalToResolve.set(pos);
                
                ObjectMap<Block, Tile> existing = tileToAnchorMap.get(pos);
                if(existing != null){
                    Tile anchorTile = existing.get(pBlock);
                    if(anchorTile != null){
                        PatternAnchor anchor = anchorMap.get(anchorTile);
                        if(anchor != null) toRemove.add(anchor);
                    }
                }

                if(!spanAbove && y > 0 && hasPatterned(world.tile(x1, y - 1), patterned) && !visited.get(x1 + (y - 1) * width)){
                    stack.add(x1 + (y - 1) * width);
                    spanAbove = true;
                }else if(spanAbove && !(hasPatterned(world.tile(x1, y - 1), patterned) && !visited.get(x1 + (y - 1) * width))){
                    spanAbove = false;
                }
                if(!spanBelow && y < height - 1 && hasPatterned(world.tile(x1, y + 1), patterned) && !visited.get(x1 + (y + 1) * width)){
                    stack.add(x1 + (y + 1) * width);
                    spanBelow = true;
                }else if(spanBelow && y < height - 1 && !(hasPatterned(world.tile(x1, y + 1), patterned) && !visited.get(x1 + (y + 1) * width))){
                    spanBelow = false;
                }
                x1++;
            }
        }
    }

    private static void resolveTiles(ObjectMap<Block, Bits> claimed){
        processedPool.each((b, bits) -> bits.clear());
        
        for(int i = PatternManager.globalToResolve.nextSetBit(0); i >= 0; i = PatternManager.globalToResolve.nextSetBit(i + 1)){
            Tile tile = world.tiles.geti(i);
            if(tile == null) continue;

            if(tile.floor() instanceof Patterned p) handleResolve(tile, p, claimed);
            if(tile.block() instanceof Patterned p) handleResolve(tile, p, claimed);
            if(tile.overlay() instanceof Patterned p) handleResolve(tile, p, claimed);
        }
    }

    private static void handleResolve(Tile tile, Patterned p, ObjectMap<Block, Bits> claimedMap){
        Block pBlock = (Block)p;
        Bits claimed = getBits(claimedMap, pBlock);
        if(claimed.get(tile.array())) return;

        Bits processed = getBits(processedPool, pBlock);
        Shape shape = p.getShape();
        
        shape.each((sx, sy) -> {
            if(shape.get(sx, sy)){
                Tile potentialAnchor = world.tile(tile.x - sx, tile.y - sy);
                if(potentialAnchor != null){
                    int apos = potentialAnchor.array();
                    if(processed.get(apos)) return;
                    processed.set(apos);

                    if(isPatternComplete(p, potentialAnchor, claimedMap)){
                        addAnchor(p, potentialAnchor, claimedMap);
                    }
                }
            }
        });
    }

    private static void addAnchor(Patterned p, Tile anchor, ObjectMap<Block, Bits> claimedMap){
        Block pBlock = (Block)p;
        PatternAnchor pa = new PatternAnchor(anchor, p);
        anchorMap.put(anchor, pa);
        Shape shape = p.getShape();
        Bits claimed = getBits(claimedMap, pBlock);
        
        shape.each((x, y) -> {
            if(shape.get(x, y)){
                Tile member = world.tile(anchor.x + x, anchor.y + y);
                if(member != null){
                    int mpos = member.array();
                    claimed.set(mpos);
                    ObjectMap<Block, Tile> map = tileToAnchorMap.get(mpos);
                    if(map == null) tileToAnchorMap.put(mpos, map = new ObjectMap<>());
                    map.put(pBlock, anchor);
                }
            }
        });
    }

    private static boolean isPatternComplete(Patterned patterned, Tile anchor, ObjectMap<Block, Bits> claimedMap){
        Block pBlock = (Block)patterned;
        Shape shape = patterned.getShape();
        Bits claimed = claimedMap.get(pBlock);
        
        for(int x = 0; x < shape.width(); x++){
            for(int y = 0; y < shape.height(); y++){
                if(shape.get(x, y)){
                    Tile other = world.tile(anchor.x + x, anchor.y + y);
                    if(!hasPatterned(other, patterned)) return false;
                    if(claimed != null && claimed.get(other.array())) return false;
                }
            }
        }
        return true;
    }

    public static boolean isPatternComplete(Patterned patterned, Tile anchor){
        if(!(patterned instanceof Block pBlock)) return false;
        Shape shape = patterned.getShape();
        for(int x = 0; x < shape.width(); x++){
            for(int y = 0; y < shape.height(); y++){
                if(shape.get(x, y)){
                    Tile other = world.tile(anchor.x + x, anchor.y + y);
                    if(!hasPatterned(other, patterned)) return false;
                    if(getAnchor(other, patterned) != anchor) return false;
                }
            }
        }
        return true;
    }

    public static Tile getAnchor(Tile tile, Patterned p){
        if(tile == null || !(p instanceof Block pBlock)) return null;
        ObjectMap<Block, Tile> map = tileToAnchorMap.get(tile.array());
        return map == null ? null : map.get(pBlock);
    }

    public static boolean hasPatterned(Tile tile, Patterned p){
        if(tile == null || !(p instanceof Block pBlock)) return false;
        return tile.block() == pBlock || tile.floor() == pBlock || tile.overlay() == pBlock;
    }

    private static class PatternAnchor{
        public final Tile tile;
        public final Patterned patterned;
        public final Shape shape;

        public PatternAnchor(Tile tile, Patterned patterned){
            this.tile = tile;
            this.patterned = patterned;
            this.shape = patterned.getShape();
        }
    }
}