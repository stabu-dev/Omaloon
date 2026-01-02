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
    private static final ObjectMap<Block, ObjectMap<Tile, PatternAnchor>> anchorMap = new ObjectMap<>();
    private static final ObjectMap<Block, IntIntMap> blockToAnchorMap = new ObjectMap<>();
    private static final ObjectMap<Block, int[]> shapeOffsets = new ObjectMap<>();

    private static final ObjectMap<Block, IntSet> dirtyBlocks = new ObjectMap<>();
    private static final ObjectMap<Block, Bits> blocksToResolve = new ObjectMap<>();
    private static final Bits globalToRecache = new Bits();

    private static boolean updateQueued = false;
    private static boolean initialized = false;

    private static final ObjectMap<Block, Bits> visitedPool = new ObjectMap<>();
    private static final ObjectMap<Block, Bits> claimedPool = new ObjectMap<>();
    private static final ObjectMap<Block, Bits> processedPool = new ObjectMap<>();

    public static void register(){
        Events.on(TileOverlayChangeEvent.class, event -> {
            if(event.overlay instanceof Patterned p){
                updateAround(event.tile, p);
                for(int i = 0; i < 4; i++){
                    Tile near = event.tile.nearby(i);
                    if(near != null) updateAround(near, p);
                }
            }
            if(event.previous instanceof Patterned p){
                updateAround(event.tile, p);
                for(int i = 0; i < 4; i++){
                    Tile near = event.tile.nearby(i);
                    if(near != null) updateAround(near, p);
                }
            }
        });

        Events.on(WorldLoadEvent.class, event -> rebuild());
    }

    private static IntIntMap getAnchorMap(Block b){
        IntIntMap map = blockToAnchorMap.get(b);
        if(map == null) blockToAnchorMap.put(b, map = new IntIntMap());
        return map;
    }

    private static ObjectMap<Tile, PatternAnchor> getAnchorObjectMap(Block b){
        ObjectMap<Tile, PatternAnchor> map = anchorMap.get(b);
        if(map == null) anchorMap.put(b, map = new ObjectMap<>());
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
        anchorMap.each((b, map) -> map.clear());
        blockToAnchorMap.each((b, map) -> map.clear());
        shapeOffsets.clear();
        dirtyBlocks.clear();
        blocksToResolve.each((b, bits) -> bits.clear());
        globalToRecache.clear();
        updateQueued = false;
        initialized = true;

        int size = world.width() * world.height();
        for(int i = 0; i < size; i++){
            Tile tile = world.tiles.geti(i);
            if(tile.floor() instanceof Patterned p) getBits(blocksToResolve, (Block)p).set(i);
            if(tile.block() instanceof Patterned p) getBits(blocksToResolve, (Block)p).set(i);
            if(tile.overlay() instanceof Patterned p) getBits(blocksToResolve, (Block)p).set(i);
        }
        
        claimedPool.each((b, bits) -> bits.clear());
        resolveTiles();

        if(!headless && renderer != null && renderer.blocks != null){
            renderer.blocks.floor.reload();
        }
    }

    public static void updateAround(Tile tile, Patterned p){
        if(tile == null || p == null || world.isGenerating() || world.tiles == null) return;
        Block b = (Block)p;
        IntSet set = dirtyBlocks.get(b);
        if(set == null) dirtyBlocks.put(b, set = new IntSet());

        if(set.add(tile.array()) && !updateQueued){
            updateQueued = true;
            Core.app.post(PatternManager::processDirtyTiles);
        }
    }

    private static void processDirtyTiles(){
        if(!initialized) rebuild();
        if(dirtyBlocks.isEmpty()){
            updateQueued = false;
            return;
        }

        ObjectMap<Block, IntSeq> dirtyCopy = new ObjectMap<>();
        for(var entry : dirtyBlocks){
            IntSeq seq = new IntSeq();
            var it = entry.value.iterator();
            while(it.hasNext) seq.add(it.next());
            dirtyCopy.put(entry.key, seq);
        }
        dirtyBlocks.clear();
        updateQueued = false;

        visitedPool.each((b, bits) -> bits.clear());
        blocksToResolve.each((b, bits) -> bits.clear());
        globalToRecache.clear();

        ObjectSet<PatternAnchor> toRemove = new ObjectSet<>();

        for(var entry : dirtyCopy){
            Block pBlock = entry.key;
            IntSeq dirty = entry.value;
            Patterned p = (Patterned)pBlock;
            IntIntMap map = blockToAnchorMap.get(pBlock);
            ObjectMap<Tile, PatternAnchor> aMap = anchorMap.get(pBlock);
            Bits toResolve = getBits(blocksToResolve, pBlock);

            for(int i = 0; i < dirty.size; i++){
                int pos = dirty.get(i);
                
                if(map != null && aMap != null){
                    int anchorPos = map.get(pos, -1);
                    if(anchorPos != -1){
                        PatternAnchor anchor = aMap.get(world.tiles.geti(anchorPos));
                        if(anchor != null) toRemove.add(anchor);
                    }
                }

                Tile tile = world.tiles.geti(pos);
                if(tile == null) continue;

                handleDirty(tile, p, toRemove, toResolve);
            }
        }

        toRemove.each(anchor -> {
            int[] offsets = getShapeOffsets(anchor.patterned);
            Block pBlock = (Block)anchor.patterned;
            IntIntMap map = getAnchorMap(pBlock);
            ObjectMap<Tile, PatternAnchor> aMap = getAnchorObjectMap(pBlock);
            Bits toResolve = getBits(blocksToResolve, pBlock);
            
            for(int offset : offsets){
                int tx = anchor.tile.x + Point2.x(offset);
                int ty = anchor.tile.y + Point2.y(offset);
                Tile member = world.tile(tx, ty);
                if(member != null){
                    int mpos = member.array();
                    toResolve.set(mpos);
                    globalToRecache.set(mpos);
                    map.remove(mpos);
                }
            }
            aMap.remove(anchor.tile);
        });

        if(!blocksToResolve.isEmpty()){
            claimedPool.each((b, bits) -> bits.clear());
            resolveTiles();

            if(!headless && renderer != null && renderer.blocks != null){
                IntSet chunks = new IntSet();
                int width = world.width();
                for(int i = globalToRecache.nextSetBit(0); i >= 0; i = globalToRecache.nextSetBit(i + 1)){
                    int cx = (i % width) / 30;
                    int cy = (i / width) / 30;
                    if(chunks.add(Point2.pack(cx, cy))){
                        renderer.blocks.floor.recacheTile(i % width, i / width);
                    }
                }
            }
        }

        if(!dirtyBlocks.isEmpty() && !updateQueued){
            updateQueued = true;
            Core.app.post(PatternManager::processDirtyTiles);
        }
    }

    private static void handleDirty(Tile tile, Patterned p, ObjectSet<PatternAnchor> toRemove, Bits toResolve){
        Block pBlock = (Block)p;
        Bits visited = getBits(visitedPool, pBlock);
        if(visited.get(tile.array())) return;

        findAndMarkContiguous(tile, p, toRemove, toResolve);
    }

    private static void findAndMarkContiguous(Tile startTile, Patterned patterned, ObjectSet<PatternAnchor> toRemove, Bits toResolve){
        Block pBlock = (Block)patterned;
        Bits visited = getBits(visitedPool, pBlock);
        IntIntMap map = getAnchorMap(pBlock);
        ObjectMap<Tile, PatternAnchor> aMap = getAnchorObjectMap(pBlock);
        
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
                toResolve.set(pos);
                globalToRecache.set(pos);
                
                int anchorPos = map.get(pos, -1);
                if(anchorPos != -1){
                    PatternAnchor anchor = aMap.get(world.tiles.geti(anchorPos));
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

    private static void resolveTiles(){
        processedPool.each((b, bits) -> bits.clear());
        
        for(var entry : blocksToResolve){
            Block pBlock = entry.key;
            Bits toResolve = entry.value;
            Patterned p = (Patterned)pBlock;
            Bits processed = getBits(processedPool, pBlock);
            int[] offsets = getShapeOffsets(p);

            for(int i = toResolve.nextSetBit(0); i >= 0; i = toResolve.nextSetBit(i + 1)){
                Tile tile = world.tiles.geti(i);
                if(tile == null) continue;

                for(int offset : offsets){
                    int sx = Point2.x(offset);
                    int sy = Point2.y(offset);
                    Tile potentialAnchor = world.tile(tile.x - sx, tile.y - sy);
                    if(potentialAnchor != null){
                        int apos = potentialAnchor.array();
                        if(processed.get(apos)) continue;
                        processed.set(apos);

                        if(isPatternInternal(p, potentialAnchor)){
                            addAnchor(p, potentialAnchor);
                        }
                    }
                }
            }
        }
    }

    private static void addAnchor(Patterned p, Tile anchor){
        Block pBlock = (Block)p;
        PatternAnchor pa = new PatternAnchor(anchor, p);
        getAnchorObjectMap(pBlock).put(anchor, pa);
        int[] offsets = getShapeOffsets(p);
        Bits claimed = getBits(claimedPool, pBlock);
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

    private static boolean isPatternInternal(Patterned patterned, Tile anchor){
        Block pBlock = (Block)patterned;
        int[] offsets = getShapeOffsets(patterned);
        Bits claimed = claimedPool.get(pBlock);
        
        for(int offset : offsets){
            Tile other = world.tile(anchor.x + Point2.x(offset), anchor.y + Point2.y(offset));
            if(!hasPatterned(other, patterned)) return false;
            if(claimed != null && claimed.get(other.array())) return false;
        }
        return true;
    }

    public static boolean isPatternComplete(Patterned patterned, Tile anchor){
        if(!(patterned instanceof Block)) return false;
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