package omaloon.world.patterns;

import arc.*;
import arc.math.geom.*;
import arc.struct.*;
import mindustry.game.EventType.*;
import mindustry.world.*;

import java.util.*;

import static mindustry.Vars.*;

public class PatternManager{
    private static final ObjectMap<Block, int[]> tileToAnchorMap = new ObjectMap<>();
    private static final ObjectMap<Block, byte[]> tileDataMap = new ObjectMap<>();
    private static final ObjectMap<Block, Bits> dirtyBlocks = new ObjectMap<>();
    private static final ObjectMap<Pattern, int[]> patternOffsets = new ObjectMap<>();
    private static final IntSet dirtyChunks = new IntSet();

    private static final Bits tempVisited = new Bits();
    private static final Bits tempClaimed = new Bits();
    private static final Bits tempProcessed = new Bits();
    private static final Bits tempRemoved = new Bits();
    private static int[] tempOldMap = new int[0];
    private static final IntSeq floodStack = new IntSeq();

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

    private static byte[] getDataMap(Block b){
        byte[] map = tileDataMap.get(b);
        int size = world.width() * world.height();
        if(map == null || map.length < size){
            tileDataMap.put(b, map = new byte[size]);
            Arrays.fill(map, (byte)-128);
        }
        return map;
    }

    private static int[] getShapeOffsets(Pattern p){
        int[] offsets = patternOffsets.get(p);
        if(offsets != null) return offsets;

        Seq<Point2> points = new Seq<>();
        p.shape.each((x, y) -> {
            if(p.shape.get(x, y)) points.add(new Point2(x, y));
        });

        int width = p.shape.width();
        points.sort(Comparator.comparingInt(p2 -> p2.x + p2.y * width));

        int[] result = new int[points.size];
        for(int i = 0; i < points.size; i++){
            Point2 p2 = points.get(i);
            result[i] = Point2.pack(p2.x, p2.y);
        }

        patternOffsets.put(p, result);
        return result;
    }

    private static Bits getDirtyBits(Block b){
        Bits bits = dirtyBlocks.get(b);
        int size = world.width() * world.height();
        if(bits == null || bits.numBits() < size){
            dirtyBlocks.put(b, bits = new Bits(size));
        }
        return bits;
    }

    public static void rebuild(){
        if(world.tiles == null) return;
        tileToAnchorMap.each((b, map) -> Arrays.fill(map, -1));
        tileDataMap.each((b, map) -> Arrays.fill(map, (byte)-128));
        patternOffsets.clear();
        dirtyBlocks.each((b, bits) -> bits.clear());
        dirtyChunks.clear();
        updateQueued = false;
        initialized = true;

        int size = world.width() * world.height();
        for(int i = 0; i < size; i++){
            Tile tile = world.tiles.geti(i);
            if(tile != null){
                if(tile.floor() instanceof Patterned p) getDirtyBits((Block)p).set(i);
                if(tile.block() instanceof Patterned p) getDirtyBits((Block)p).set(i);
                if(tile.overlay() instanceof Patterned p) getDirtyBits((Block)p).set(i);
            }
        }

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
        getDirtyBits(b).set(tile.array());
    }

    private static void processDirtyTiles(){
        if(!initialized) rebuild();
        updateQueued = false;
        if(dirtyBlocks.isEmpty()) return;

        dirtyChunks.clear();
        tempVisited.clear();

        for(var entry : dirtyBlocks){
            Block pBlock = entry.key;
            Bits dirtyBits = entry.value;
            if(dirtyBits.isEmpty()) continue;
            Patterned p = (Patterned)pBlock;

            for(int i = dirtyBits.nextSetBit(0); i >= 0; i = dirtyBits.nextSetBit(i + 1)){
                Tile tile = world.tiles.geti(i);
                if(tile != null) handleDirty(tile, p, dirtyBits);
            }
        }

        resolveTiles();

        if(!headless && renderer != null && renderer.blocks != null){
            dirtyChunks.each(packed -> renderer.blocks.floor.recacheTile(Point2.x(packed) * 30, Point2.y(packed) * 30));
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
                    Pattern active = p.getPattern(anchorTile);
                    if(active != null){
                        int[] offsets = getShapeOffsets(active);
                        for(int offset : offsets){
                            int mpos = (anchorTile.x + Point2.x(offset)) + (anchorTile.y + Point2.y(offset)) * width;
                            toResolve.set(mpos);
                        }
                    }
                }
            }
        }

        if(!tempVisited.get(pos) && hasPatterned(tile, p)){
            findAndMarkContiguous(tile, p, toResolve);
        }
    }

    private static void findAndMarkContiguous(Tile startTile, Patterned patterned, Bits toResolve){
        floodStack.clear();
        floodStack.add(startTile.array());
        int width = world.width(), height = world.height();

        while(floodStack.size > 0){
            int popped = floodStack.pop();
            int x = popped % width, y = popped / width;
            if(tempVisited.get(popped)) continue;

            int x1 = x;
            while(x1 >= 0 && hasPatterned(world.tiles.geti(x1 + y * width), patterned) && !tempVisited.get(x1 + y * width)) x1--;
            x1++;
            boolean spanAbove = false, spanBelow = false;
            while(x1 < width && hasPatterned(world.tiles.geti(x1 + y * width), patterned) && !tempVisited.get(x1 + y * width)){
                int pos = x1 + y * width;
                tempVisited.set(pos);
                toResolve.set(pos);

                if(!spanAbove && y > 0 && hasPatterned(world.tiles.geti(x1 + (y - 1) * width), patterned) && !tempVisited.get(x1 + (y - 1) * width)){
                    floodStack.add(x1 + (y - 1) * width);
                    spanAbove = true;
                }else if(spanAbove && !(hasPatterned(world.tiles.geti(x1 + (y - 1) * width), patterned) && !tempVisited.get(x1 + (y - 1) * width))){
                    spanAbove = false;
                }
                if(!spanBelow && y < height - 1 && hasPatterned(world.tiles.geti(x1 + (y + 1) * width), patterned) && !tempVisited.get(x1 + (y + 1) * width)){
                    floodStack.add(x1 + (y + 1) * width);
                    spanBelow = true;
                }else if(spanBelow && y < height - 1 && !(hasPatterned(world.tiles.geti(x1 + (y + 1) * width), patterned) && !tempVisited.get(x1 + (y + 1) * width))){
                    spanBelow = false;
                }
                x1++;
            }
        }
    }

    private static void resolveTiles(){
        int size = world.width() * world.height();
        if(tempOldMap.length < size) tempOldMap = new int[size];

        for(var entry : dirtyBlocks){
            Block pBlock = entry.key;
            Bits toResolve = entry.value;
            if(toResolve.isEmpty()) continue;
            Patterned p = (Patterned)pBlock;

            Pattern topPattern = p.getPattern();
            Seq<Pattern> availablePatterns = (topPattern instanceof MultiPattern mp) ? mp.patterns : Seq.with(topPattern);
            if(availablePatterns.isEmpty()) continue;

            tempVisited.clear();
            tempClaimed.clear();
            tempProcessed.clear();
            tempRemoved.clear();

            int width = world.width();
            int[] map = getAnchorMap(pBlock);
            byte[] dataMap = getDataMap(pBlock);

            for(int i = toResolve.nextSetBit(0); i >= 0; i = toResolve.nextSetBit(i + 1)){
                tempOldMap[i] = map[i];
            }

            for(int i = toResolve.nextSetBit(0); i >= 0; i = toResolve.nextSetBit(i + 1)){
                int oldAnchorPos = map[i];
                map[i] = -1;

                if(oldAnchorPos != -1 && !tempRemoved.get(oldAnchorPos)){
                    tempRemoved.set(oldAnchorPos);
                    Tile oldAnchorTile = world.tiles.geti(oldAnchorPos);
                    if(oldAnchorTile != null){
                        Pattern active = p.getPattern(oldAnchorTile);
                        if(active != null){
                            int[] offsets = getShapeOffsets(active);
                            for(int offset : offsets){
                                int mpos = (oldAnchorTile.x + Point2.x(offset)) + (oldAnchorTile.y + Point2.y(offset)) * width;
                                if(mpos >= 0 && mpos < map.length){
                                    map[mpos] = -1;
                                    tempClaimed.clear(mpos);
                                }
                            }
                        }
                    }
                }
            }

            if(topPattern instanceof MultiPattern mp){
                for(int i = toResolve.nextSetBit(0); i >= 0; i = toResolve.nextSetBit(i + 1)){
                    if(tempClaimed.get(i)) continue;
                    Tile tile = world.tiles.geti(i);
                    if(tile == null || tile.data < 0 || tile.data >= mp.patterns.size) continue;

                    int apos = tile.array();
                    if(!tempProcessed.get(apos)){
                        tempProcessed.set(apos);
                        Pattern forcedPat = mp.get(tile.data);
                        if(isPatternInternal(p, forcedPat, tile)){
                            addAnchor(p, forcedPat, tile);
                        }
                    }
                }
            }

            for(int i = toResolve.nextSetBit(0); i >= 0; i = toResolve.nextSetBit(i + 1)){
                if(tempClaimed.get(i)) continue;

                Tile tile = world.tiles.geti(i);
                if(tile == null || p.getPattern(tile) == null) continue;

                boolean foundAnchor = false;
                for(Pattern pat : availablePatterns){
                    int[] offsets = getShapeOffsets(pat);
                    for(int offset : offsets){
                        int ox = Point2.x(offset);
                        int oy = Point2.y(offset);
                        Tile potentialAnchor = world.tile(tile.x - ox, tile.y - oy);
                        if(potentialAnchor != null){
                            int apos = potentialAnchor.array();
                            if(!tempProcessed.get(apos)){
                                tempProcessed.set(apos);
                                if(isPatternInternal(p, pat, potentialAnchor)){
                                    addAnchor(p, pat, potentialAnchor);
                                    foundAnchor = true;
                                    break;
                                }
                            }
                        }
                    }
                    if(foundAnchor) break;
                }
            }

            for(int i = toResolve.nextSetBit(0); i >= 0; i = toResolve.nextSetBit(i + 1)){
                if(tempOldMap[i] != map[i]){
                    markChunkDirty(i % width, i / width);
                }
                Tile t = world.tiles.geti(i);
                if(t != null) dataMap[i] = t.data;
            }

            toResolve.clear();
        }
    }

    private static void addAnchor(Patterned p, Pattern pat, Tile anchor){
        Block pBlock = (Block)p;
        int anchorPos = anchor.array();
        int[] offsets = getShapeOffsets(pat);
        int[] map = getAnchorMap(pBlock);

        Pattern topPattern = p.getPattern();
        if(topPattern instanceof MultiPattern mp && anchor.data < 0){
            int patIdx = mp.patterns.indexOf(pat);
            if(patIdx >= 0){
                anchor.data = (byte)patIdx;
            }
        }

        int width = world.width();
        for(int offset : offsets){
            int tx = anchor.x + Point2.x(offset);
            int ty = anchor.y + Point2.y(offset);
            int mpos = tx + ty * width;
            tempClaimed.set(mpos);
            map[mpos] = anchorPos;
        }
    }

    private static boolean isPatternInternal(Patterned patterned, Pattern pat, Tile anchor){
        if(patterned.getPattern(anchor) == null) return false;
        int[] offsets = getShapeOffsets(pat);

        Pattern topPattern = patterned.getPattern();
        int patIdx = -1;
        if(topPattern instanceof MultiPattern mp){
            patIdx = mp.patterns.indexOf(pat);
        }

        int width = world.width(), height = world.height();
        for(int offset : offsets){
            int tx = anchor.x + Point2.x(offset);
            int ty = anchor.y + Point2.y(offset);
            if(tx < 0 || tx >= width || ty < 0 || ty >= height) return false;
            int pos = tx + ty * width;
            Tile other = world.tiles.geti(pos);
            if(!hasPatterned(other, patterned)) return false;
            if(patterned.getPattern(other) == null) return false;
            if(tempClaimed.get(pos)) return false;

            if(patIdx >= 0 && other.data >= 0 && other.data != patIdx){
                return false;
            }
        }
        return true;
    }

    public static boolean isPatternComplete(Patterned patterned, Tile anchor){
        if(!(patterned instanceof Block)) return false;
        Pattern pat = patterned.getPattern(anchor);
        if(pat == null) return false;
        int[] offsets = getShapeOffsets(pat);
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

        byte[] dataMap = getDataMap(pBlock);
        if(tile.data != dataMap[pos]){
            markBlockDirty(tile, pBlock);
            queueUpdate();
        }

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