package omaloon.world.patterns;

import arc.*;
import arc.math.geom.*;
import arc.struct.*;
import mindustry.game.EventType.*;
import mindustry.world.*;
import omaloon.type.shape.*;

import java.util.Arrays;

import static mindustry.Vars.*;

public class PatternManager{
    private static final ObjectMap<Block, int[]> tileToAnchorMap = new ObjectMap<>();
    private static final ObjectMap<Block, int[]> oldMapPool = new ObjectMap<>();
    private static final ObjectMap<Block, int[]> shapeOffsets = new ObjectMap<>();

    private static final ObjectMap<Block, IntSet> dirtyBlocks = new ObjectMap<>();
    private static final ObjectMap<Block, Bits> blocksToResolve = new ObjectMap<>();
    private static final IntSet dirtyChunks = new IntSet();
    private static final ObjectMap<Block, Bits> visitedPool = new ObjectMap<>();
    private static final ObjectMap<Block, Bits> claimedPool = new ObjectMap<>();
    private static final ObjectMap<Block, Bits> processedPool = new ObjectMap<>();
    private static final ObjectMap<Block, Bits> removedAnchorsPool = new ObjectMap<>();

    private static final IntSeq floodStack = new IntSeq();
    private static final ObjectMap<Block, IntSeq> dirtyCopy = new ObjectMap<>();

    private static boolean updateQueued = false;
    private static boolean initialized = false;

    public static void register(){
        Events.on(WorldLoadEvent.class, event -> rebuild());
    }

    private static int[] getAnchorMap(Block b){
        int[] map = tileToAnchorMap.get(b);
        int size = world.width() * world.height();
        if(map == null || map.length < size){
            tileToAnchorMap.put(b, map = new int[size]);
            Arrays.fill(map, -1);
        }
        return map;
    }

    private static int[] getOldMap(Block b){
        int[] map = oldMapPool.get(b);
        int size = world.width() * world.height();
        if(map == null || map.length < size){
            oldMapPool.put(b, map = new int[size]);
            Arrays.fill(map, -1);
        }
        return map;
    }

    private static int[] getShapeOffsets(Patterned p){
        Block b = (Block)p;
        int[] offsets = shapeOffsets.get(b);
        if(offsets != null) return offsets;

        Seq<Point2> points = new Seq<>();
        p.getPattern().shape.each((x, y) -> {
            if(p.getPattern().shape.get(x, y)) points.add(new Point2(x, y));
        });

        int width = p.getPattern().shape.width();
        points.sort((a, b1) -> {
            int i1 = a.x + a.y * width;
            int i2 = b1.x + b1.y * width;
            return Integer.compare(i1, i2);
        });

        int[] result = new int[points.size];
        for(int i = 0; i < points.size; i++){
            Point2 p2 = points.get(i);
            result[i] = Point2.pack(p2.x, p2.y);
        }

        shapeOffsets.put(b, result);
        return result;
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
        tileToAnchorMap.each((b, map) -> Arrays.fill(map, -1));
        oldMapPool.each((b, map) -> Arrays.fill(map, -1));
        shapeOffsets.clear();
        dirtyBlocks.clear();
        blocksToResolve.each((b, bits) -> bits.clear());
        dirtyChunks.clear();
        dirtyCopy.clear();
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
        if(tile == null || world.isGenerating() || world.tiles == null) return;
        if(p != null){
            markBlockDirty(tile, (Block)p);
            for(int i = 0; i < 4; i++){
                Tile near = tile.nearby(i);
                if(near != null) markBlockDirty(near, (Block)p);
            }
        }
        updateAround(tile);
    }

    public static void updateAround(Tile tile){
        if(tile == null || world.isGenerating() || world.tiles == null) return;
        markTileDirty(tile);
        for(int i = 0; i < 4; i++){
            Tile near = tile.nearby(i);
            if(near != null) markTileDirty(near);
        }
        queueUpdate();
    }

    private static void queueUpdate(){
        if(!updateQueued){
            updateQueued = true;
            Core.app.post(PatternManager::processDirtyTiles);
        }
    }

    private static void markTileDirty(Tile tile){
        if(tile == null) return;
        int pos = tile.array();

        if(tile.floor() instanceof Patterned p) markBlockDirty(tile, (Block)p);
        if(tile.overlay() instanceof Patterned p) markBlockDirty(tile, (Block)p);
        if(tile.block() instanceof Patterned p) markBlockDirty(tile, (Block)p);

        for(var entry : tileToAnchorMap){
            Block b = entry.key;
            int[] map = entry.value;
            if(map != null && pos < map.length && map[pos] != -1){
                markBlockDirty(tile, b);
            }
        }
    }

    private static void markBlockDirty(Tile tile, Block b){
        IntSet set = dirtyBlocks.get(b);
        if(set == null) dirtyBlocks.put(b, set = new IntSet());
        set.add(tile.array());
    }

    private static void processDirtyTiles(){
        if(!initialized) rebuild();
        updateQueued = false;
        if(dirtyBlocks.isEmpty()) return;

        dirtyCopy.each((b, seq) -> seq.clear());

        for(var entry : dirtyBlocks){
            IntSeq seq = dirtyCopy.get(entry.key);
            if(seq == null) dirtyCopy.put(entry.key, seq = new IntSeq());
            var it = entry.value.iterator();
            while(it.hasNext) seq.add(it.next());
        }
        dirtyBlocks.clear();

        visitedPool.each((b, bits) -> bits.clear());
        blocksToResolve.each((b, bits) -> bits.clear());
        dirtyChunks.clear();

        for(var entry : dirtyCopy){
            Block pBlock = entry.key;
            IntSeq dirty = entry.value;
            if(dirty.isEmpty()) continue;
            Patterned p = (Patterned)pBlock;
            Bits toResolve = getBits(blocksToResolve, pBlock);

            for(int i = 0; i < dirty.size; i++){
                Tile tile = world.tiles.geti(dirty.get(i));
                if(tile != null) handleDirty(tile, p, toResolve);
            }
        }

        if(!blocksToResolve.isEmpty()){
            resolveTiles();

            if(!headless && renderer != null && renderer.blocks != null){
                dirtyChunks.each(packed -> renderer.blocks.floor.recacheTile(Point2.x(packed) * 30, Point2.y(packed) * 30));
            }
        }
    }

    private static void markChunkDirty(int x, int y){
        dirtyChunks.add(Point2.pack(x / 30, y / 30));
    }

    private static void handleDirty(Tile tile, Patterned p, Bits toResolve){
        Block pBlock = (Block)p;
        int width = world.width();
        int pos = tile.array();
        toResolve.set(pos);

        int[] map = tileToAnchorMap.get(pBlock);
        if(map != null && pos < map.length){
            int anchorPos = map[pos];
            if(anchorPos != -1){
                Tile anchorTile = world.tiles.geti(anchorPos);
                if(anchorTile != null){
                    int[] offsets = getShapeOffsets(p);
                    for(int offset : offsets){
                        int mpos = (anchorTile.x + Point2.x(offset)) + (anchorTile.y + Point2.y(offset)) * width;
                        toResolve.set(mpos);
                    }
                }
            }
        }

        Bits visited = getBits(visitedPool, pBlock);
        if(!visited.get(pos) && hasPatterned(tile, p)){
            findAndMarkContiguous(tile, p, toResolve);
        }
    }

    private static void findAndMarkContiguous(Tile startTile, Patterned patterned, Bits toResolve){
        Block pBlock = (Block)patterned;
        Bits visited = getBits(visitedPool, pBlock);

        floodStack.clear();
        floodStack.add(startTile.array());
        int width = world.width(), height = world.height();

        while(floodStack.size > 0){
            int popped = floodStack.pop();
            int x = popped % width, y = popped / width;
            if(visited.get(popped)) continue;

            int x1 = x;
            while(x1 >= 0 && hasPatterned(world.tiles.geti(x1 + y * width), patterned) && !visited.get(x1 + y * width)) x1--;
            x1++;
            boolean spanAbove = false, spanBelow = false;
            while(x1 < width && hasPatterned(world.tiles.geti(x1 + y * width), patterned) && !visited.get(x1 + y * width)){
                int pos = x1 + y * width;
                visited.set(pos);
                toResolve.set(pos);

                if(!spanAbove && y > 0 && hasPatterned(world.tiles.geti(x1 + (y - 1) * width), patterned) && !visited.get(x1 + (y - 1) * width)){
                    floodStack.add(x1 + (y - 1) * width);
                    spanAbove = true;
                }else if(spanAbove && !(hasPatterned(world.tiles.geti(x1 + (y - 1) * width), patterned) && !visited.get(x1 + (y - 1) * width))){
                    spanAbove = false;
                }
                if(!spanBelow && y < height - 1 && hasPatterned(world.tiles.geti(x1 + (y + 1) * width), patterned) && !visited.get(x1 + (y + 1) * width)){
                    floodStack.add(x1 + (y + 1) * width);
                    spanBelow = true;
                }else if(spanBelow && y < height - 1 && !(hasPatterned(world.tiles.geti(x1 + (y + 1) * width), patterned) && !visited.get(x1 + (y + 1) * width))){
                    spanBelow = false;
                }
                x1++;
            }
        }
    }

    private static void resolveTiles(){
        processedPool.each((b, bits) -> bits.clear());
        removedAnchorsPool.each((b, bits) -> bits.clear());

        for(var entry : blocksToResolve){
            Block pBlock = entry.key;
            Bits toResolve = entry.value;
            Patterned p = (Patterned)pBlock;
            Bits processed = getBits(processedPool, pBlock);
            Bits claimed = getBits(claimedPool, pBlock);
            Bits removedAnchors = getBits(removedAnchorsPool, pBlock);
            int[] map = getAnchorMap(pBlock);
            int[] oldMap = getOldMap(pBlock);
            int[] offsets = getShapeOffsets(p);

            if(offsets.length == 0) continue;

            int width = world.width();

            for(int i = toResolve.nextSetBit(0); i >= 0; i = toResolve.nextSetBit(i + 1)){
                oldMap[i] = map[i];
            }

            for(int i = toResolve.nextSetBit(0); i >= 0; i = toResolve.nextSetBit(i + 1)){
                claimed.clear(i);
                int oldAnchorPos = map[i];
                if(oldAnchorPos != -1 && !removedAnchors.get(oldAnchorPos)){
                    removedAnchors.set(oldAnchorPos);
                    Tile oldAnchorTile = world.tiles.geti(oldAnchorPos);
                    if(oldAnchorTile != null){
                        for(int offset : offsets){
                            int tx = oldAnchorTile.x + Point2.x(offset);
                            int ty = oldAnchorTile.y + Point2.y(offset);
                            int mpos = tx + ty * width;
                            if(mpos >= 0 && mpos < map.length){
                                map[mpos] = -1;
                                claimed.clear(mpos);
                            }
                        }
                    }
                }
            }

            for(int i = toResolve.nextSetBit(0); i >= 0; i = toResolve.nextSetBit(i + 1)){
                if(claimed.get(i)) continue;

                Tile tile = world.tiles.geti(i);
                if(tile == null) continue;

                for(int offset : offsets){
                    int ox = Point2.x(offset);
                    int oy = Point2.y(offset);
                    Tile potentialAnchor = world.tile(tile.x - ox, tile.y - oy);
                    if(potentialAnchor != null){
                        int apos = potentialAnchor.array();
                        if(!processed.get(apos)){
                            processed.set(apos);
                            if(isPatternInternal(p, potentialAnchor)){
                                addAnchor(p, potentialAnchor);
                                break;
                            }
                        }
                    }
                }
            }

            for(int i = toResolve.nextSetBit(0); i >= 0; i = toResolve.nextSetBit(i + 1)){
                if(!claimed.get(i)){
                    map[i] = -1;
                }
                if(oldMap[i] != map[i]){
                    markChunkDirty(i % width, i / width);
                }
            }
        }
    }

    private static void addAnchor(Patterned p, Tile anchor){
        Block pBlock = (Block)p;
        int anchorPos = anchor.array();
        int[] offsets = getShapeOffsets(p);
        Bits claimed = getBits(claimedPool, pBlock);
        int[] map = getAnchorMap(pBlock);

        int width = world.width();
        for(int offset : offsets){
            int tx = anchor.x + Point2.x(offset);
            int ty = anchor.y + Point2.y(offset);
            int mpos = tx + ty * width;
            claimed.set(mpos);
            map[mpos] = anchorPos;
        }
    }

    private static boolean isPatternInternal(Patterned patterned, Tile anchor){
        Block pBlock = (Block)patterned;
        int[] offsets = getShapeOffsets(patterned);
        Bits claimed = claimedPool.get(pBlock);

        int width = world.width(), height = world.height();
        for(int offset : offsets){
            int tx = anchor.x + Point2.x(offset);
            int ty = anchor.y + Point2.y(offset);
            if(tx < 0 || tx >= width || ty < 0 || ty >= height) return false;
            int pos = tx + ty * width;
            Tile other = world.tiles.geti(pos);
            if(!hasPatterned(other, patterned)) return false;
            if(claimed.get(pos)) return false;
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
        int[] map = tileToAnchorMap.get(pBlock);
        if(map == null) return null;
        int pos = tile.array();
        if(pos < 0 || pos >= map.length) return null;
        int anchorPos = map[pos];
        return anchorPos == -1 ? null : world.tiles.geti(anchorPos);
    }

    public static boolean hasPatterned(Tile tile, Patterned p){
        if(tile == null) return false;
        Block b = (Block)p;
        if(tile.overlay() == b) return true;
        if(tile.floor() == b) return true;
        return tile.block() == b;
    }
}