package omaloon.world.patterns;

import arc.*;
import arc.math.geom.*;
import arc.struct.*;
import mindustry.game.EventType.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import omaloon.type.shape.*;

import static mindustry.Vars.*;

public class PatternManager{
    private static final ObjectMap<Tile, PatternAnchor> anchorMap = new ObjectMap<>();
    private static final ObjectMap<Block, IntIntMap> blockToAnchorMap = new ObjectMap<>();
    private static final ObjectMap<Block, int[]> shapeOffsets = new ObjectMap<>();

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

    private static IntIntMap getAnchorMap(Block b){
        IntIntMap map = blockToAnchorMap.get(b);
        if(map == null){
            blockToAnchorMap.put(b, map = new IntIntMap());
        }
        return map;
    }

    private static int[] getShapeOffsets(Patterned p){
        Block b = (Block)p;
        int[] offsets = shapeOffsets.get(b);
        if(offsets != null) return offsets;

        IntSeq seq = new IntSeq();
        p.getShape().each((x, y) -> {
            if(p.getShape().get(x, y)) seq.add(Point2.pack(x, y));
        });
        offsets = seq.toArray();
        shapeOffsets.put(b, offsets);
        return offsets;
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
        for(IntIntMap map : blockToAnchorMap.values()){
            map.clear();
        }
        shapeOffsets.clear();
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
            
            for(var entry : blockToAnchorMap.entries()){
                int anchorPos = entry.value.get(pos, -1);
                if(anchorPos != -1){
                    PatternAnchor anchor = anchorMap.get(world.tiles.geti(anchorPos));
                    if(anchor != null) toRemove.add(anchor);
                }
            }

            Tile tile = world.tiles.geti(pos);
            if(tile == null) continue;

            if(tile.floor() instanceof Patterned p) handleDirty(tile, p, toRemove);
            if(tile.block() instanceof Patterned p) handleDirty(tile, p, toRemove);
            if(tile.overlay() instanceof Patterned p) handleDirty(tile, p, toRemove);
        }

        toRemove.each(anchor -> {
            int[] offsets = getShapeOffsets(anchor.patterned);
            Block pBlock = (Block)anchor.patterned;
            IntIntMap map = getAnchorMap(pBlock);
            
            for(int offset : offsets){
                int tx = anchor.tile.x + Point2.x(offset);
                int ty = anchor.tile.y + Point2.y(offset);
                Tile member = world.tile(tx, ty);
                if(member != null){
                    int mpos = member.array();
                    globalToResolve.set(mpos);
                    map.remove(mpos);
                }
            }
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
        IntIntMap map = getAnchorMap(pBlock);
        
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
                
                int anchorPos = map.get(pos, -1);
                if(anchorPos != -1){
                    PatternAnchor anchor = anchorMap.get(world.tiles.geti(anchorPos));
                    if(anchor != null) toRemove.add(anchor);
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
        int[] offsets = getShapeOffsets(p);
        
        for(int offset : offsets){
            int sx = Point2.x(offset);
            int sy = Point2.y(offset);
            Tile potentialAnchor = world.tile(tile.x - sx, tile.y - sy);
            if(potentialAnchor != null){
                int apos = potentialAnchor.array();
                if(processed.get(apos)) continue;
                processed.set(apos);

                if(isPatternComplete(p, potentialAnchor, claimedMap)){
                    addAnchor(p, potentialAnchor, claimedMap);
                }
            }
        }
    }

    private static void addAnchor(Patterned p, Tile anchor, ObjectMap<Block, Bits> claimedMap){
        Block pBlock = (Block)p;
        PatternAnchor pa = new PatternAnchor(anchor, p);
        anchorMap.put(anchor, pa);
        int[] offsets = getShapeOffsets(p);
        Bits claimed = getBits(claimedMap, pBlock);
        IntIntMap map = getAnchorMap(pBlock);
        int anchorPos = anchor.array();
        
        for(int offset : offsets){
            int tx = anchor.x + Point2.x(offset);
            int ty = anchor.y + Point2.y(offset);
            Tile member = world.tile(tx, ty);
            if(member != null){
                int mpos = member.array();
                claimed.set(mpos);
                map.put(mpos, anchorPos);
            }
        }
    }

    private static boolean isPatternComplete(Patterned patterned, Tile anchor, ObjectMap<Block, Bits> claimedMap){
        Block pBlock = (Block)patterned;
        int[] offsets = getShapeOffsets(patterned);
        Bits claimed = claimedMap.get(pBlock);
        
        for(int offset : offsets){
            Tile other = world.tile(anchor.x + Point2.x(offset), anchor.y + Point2.y(offset));
            if(!hasPatterned(other, patterned)) return false;
            if(claimed != null && claimed.get(other.array())) return false;
        }
        return true;
    }

    public static boolean isPatternComplete(Patterned patterned, Tile anchor){
        if(!(patterned instanceof Block pBlock)) return false;
        int[] offsets = getShapeOffsets(patterned);
        for(int offset : offsets){
            Tile other = world.tile(anchor.x + Point2.x(offset), anchor.y + Point2.y(offset));
            if(!hasPatterned(other, patterned)) return false;
            if(getAnchor(other, patterned) != anchor) return false;
        }
        return true;
    }

    public static Tile getAnchor(Tile tile, Patterned p){
        if(tile == null || !(p instanceof Block pBlock)) return null;
        IntIntMap map = blockToAnchorMap.get(pBlock);
        if(map == null) return null;
        int anchorPos = map.get(tile.array(), -1);
        return anchorPos == -1 ? null : world.tiles.geti(anchorPos);
    }

    public static boolean hasPatterned(Tile tile, Patterned p){
        if(tile == null) return false;
        Block b = (Block)p;
        if(b instanceof OverlayFloor) return tile.overlay() == b;
        if(b instanceof Floor) return tile.floor() == b;
        return tile.block() == b;
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