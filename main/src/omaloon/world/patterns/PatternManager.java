package omaloon.world.patterns;

import arc.*;
import arc.func.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.world.*;
import omaloon.type.shape.*;

import static mindustry.Vars.*;

public class PatternManager{

    private static final ObjectMap<Tile, PatternAnchor> anchorMap = new ObjectMap<>();
    private static final IntMap<ObjectMap<Block, Tile>> tileToAnchorMap = new IntMap<>();

    private static final Seq<Tile> dirtyTiles = new Seq<>();
    private static QuadTree<PatternAnchor> anchorTree;
    private static @Nullable Thread runningThread;

    public static void init(){
        anchorTree = new QuadTree<>(new Rect(0, 0, world.unitWidth(), world.unitHeight()));
        anchorMap.clear();
        tileToAnchorMap.clear();
        dirtyTiles.clear();
        IntSet allTiles = new IntSet();
        for(int y = 0; y < world.height(); y++){
            for(int x = 0; x < world.width(); x++){
                allTiles.add(Point2.pack(x, y));
            }
        }
        resolveTiles(allTiles, new IntMap<>());
    }

    public static void updateAround(Tile tile){
        if(tile == null || world.isGenerating()) return;
        if(!dirtyTiles.contains(tile)) dirtyTiles.add(tile);
        Core.app.post(PatternManager::processDirtyTiles);
    }

    private static void processDirtyTiles(){
        if (anchorTree == null) {
            init();
        }
        if(dirtyTiles.isEmpty()) return;
        if(runningThread != null && runningThread.isAlive()){
            return;
        }

        final Seq<Tile> dirty = new Seq<>(dirtyTiles);
        dirtyTiles.clear();

        final IntMap<ObjectMap<Block, Tile>> tileToAnchorMapCopy = new IntMap<>();
        for(IntMap.Entry<ObjectMap<Block, Tile>> entry : tileToAnchorMap.entries()){
            tileToAnchorMapCopy.put(entry.key, new ObjectMap<>(entry.value));
        }
        final ObjectMap<Tile, PatternAnchor> anchorMapCopy = new ObjectMap<>(anchorMap);

        runningThread = Threads.thread("Pattern-Resolver", () -> {
            IntMap<ObjectSet<Block>> visited = new IntMap<>();
            IntSet toResolve = new IntSet();
            ObjectSet<PatternAnchor> toRemove = new ObjectSet<>();

            for(Tile tile : dirty){
                for(Patterned p : getPatternedBlocks(tile)){
                    if(!(p instanceof Block pBlock)) continue;

                    ObjectSet<Block> visitedBlocks = visited.get(tile.pos());
                    if(visitedBlocks != null && visitedBlocks.contains(pBlock)) continue;

                    IntSet contiguous = findContiguousTiles(tile, p, visited);
                    toResolve.addAll(contiguous);

                    contiguous.each(pos -> {
                        ObjectMap<Block, Tile> map = tileToAnchorMapCopy.get(pos);
                        if(map != null){
                            Tile anchorTile = map.get(pBlock);
                            if(anchorTile != null){
                                PatternAnchor anchor = anchorMapCopy.get(anchorTile);
                                if(anchor != null) toRemove.add(anchor);
                            }
                        }
                    });
                }
            }

            toRemove.each(anchor ->
                anchor.shape.each((x, y) -> {
                    if(anchor.shape.get(x, y)){
                        Tile member = world.tile(anchor.tile.x + x, anchor.tile.y + y);
                        if(member != null){
                            toResolve.add(member.pos());
                        }
                    }
                })
            );

            final Seq<PatternAnchor> anchorsToAdd = new Seq<>();
            if(!toResolve.isEmpty()){
                resolveTilesAsync(toResolve, anchorsToAdd);
            }

            Core.app.post(() -> {
                toRemove.each(anchor -> {
                    anchorTree.remove(anchor);
                    anchorMap.remove(anchor.tile);
                    anchor.shape.each((x, y) -> {
                        if(anchor.shape.get(x, y)){
                            Tile member = world.tile(anchor.tile.x + x, anchor.tile.y + y);
                            if(member != null){
                                ObjectMap<Block, Tile> map = tileToAnchorMap.get(member.pos());
                                if(map != null){
                                    map.remove((Block)anchor.patterned);
                                    if(map.isEmpty()){
                                        tileToAnchorMap.remove(member.pos());
                                    }
                                }
                            }
                        }
                    });
                });

                for(PatternAnchor pa : anchorsToAdd){
                    addAnchor(pa.patterned, pa.tile, new IntMap<>());
                }

                toResolve.each(pos -> {
                    Tile t = world.tile(pos);
                    if(t != null) renderer.blocks.floor.recacheTile(t);
                });

                if(!dirtyTiles.isEmpty()){
                    Core.app.post(PatternManager::processDirtyTiles);
                }
            });
        });
    }

    private static void resolve(IntSet toResolve, IntMap<ObjectSet<Block>> resolved, Cons<PatternAnchor> onPatternFound){
        if(toResolve.isEmpty()) return;

        IntMap<ObjectSet<Block>> processedAnchors = new IntMap<>();

        IntSeq sortedToResolve = new IntSeq(toResolve.size);
        IntSet.IntSetIterator it = toResolve.iterator();
        while(it.hasNext){
            sortedToResolve.add(it.next());
        }
        sortedToResolve.sort();

        sortedToResolve.each(pos -> {
            ObjectSet<Block> resolvedBlocks = resolved.get(pos);

            Tile tile = world.tile(pos);
            if(tile == null) return;

            for(Patterned p : getPatternedBlocks(tile)){
                if (!(p instanceof Block pBlock)) continue;
                if(resolvedBlocks != null && resolvedBlocks.contains(pBlock)) continue;

                Shape shape = p.getShape();
                shape.each((sx, sy) -> {
                    if(shape.get(sx, sy)){
                        Tile potentialAnchor = world.tile(tile.x - sx, tile.y - sy);
                        if(potentialAnchor != null){
                            ObjectSet<Block> checked = processedAnchors.get(potentialAnchor.pos());
                            if(checked != null && checked.contains(pBlock)) return;

                            if(checked == null){
                                checked = new ObjectSet<>();
                                processedAnchors.put(potentialAnchor.pos(), checked);
                            }
                            checked.add(pBlock);

                            if(isPatternComplete(p, potentialAnchor, resolved)){
                                onPatternFound.get(new PatternAnchor(potentialAnchor, p));
                            }
                        }
                    }
                });
            }
        });
    }

    private static void resolveTilesAsync(IntSet toResolve, Seq<PatternAnchor> anchorsToAdd){
        IntMap<ObjectSet<Block>> resolved = new IntMap<>();
        resolve(toResolve, resolved, anchor -> {
            anchorsToAdd.add(anchor);
            anchor.patterned.getShape().each((ssx, ssy) -> {
                if(anchor.patterned.getShape().get(ssx, ssy)){
                    Tile member = world.tile(anchor.tile.x + ssx, anchor.tile.y + ssy);
                    if(member != null){
                        ObjectSet<Block> resolvedSet = resolved.get(member.pos());
                        if(resolvedSet == null){
                            resolvedSet = new ObjectSet<>();
                            resolved.put(member.pos(), resolvedSet);
                        }
                        resolvedSet.add((Block)anchor.patterned);
                    }
                }
            });
        });
    }
    
    private static void resolveTiles(IntSet toResolve, IntMap<ObjectSet<Block>> resolved){
        resolve(toResolve, resolved, anchor -> addAnchor(anchor.patterned, anchor.tile, resolved));
    }

    private static IntSet findContiguousTiles(Tile startTile, Patterned patterned, IntMap<ObjectSet<Block>> visited){
        if(!(patterned instanceof Block pBlock)) return new IntSet();

        IntSet contiguous = new IntSet();
        Seq<Tile> floodFillQueue = new Seq<>();

        floodFillQueue.add(startTile);

        ObjectSet<Block> visitedBlocks = visited.get(startTile.pos());
        if(visitedBlocks == null){
            visitedBlocks = new ObjectSet<>();
            visited.put(startTile.pos(), visitedBlocks);
        }
        visitedBlocks.add(pBlock);

        contiguous.add(startTile.pos());

        while(floodFillQueue.size > 0){
            Tile current = floodFillQueue.pop();
            for(int i = 0; i < 4; i++){
                Tile next = current.nearby(i);
                if(hasPatterned(next, patterned)){
                    ObjectSet<Block> nextVisited = visited.get(next.pos());
                    if(nextVisited == null || !nextVisited.contains(pBlock)){

                        if(nextVisited == null){
                            nextVisited = new ObjectSet<>();
                            visited.put(next.pos(), nextVisited);
                        }
                        nextVisited.add(pBlock);

                        contiguous.add(next.pos());
                        floodFillQueue.add(next);
                    }
                }
            }
        }
        return contiguous;
    }

    private static void addAnchor(Patterned p, Tile anchor, IntMap<ObjectSet<Block>> localClaimed){
        if (!(p instanceof Block pBlock)) return;
        
        PatternAnchor pa = new PatternAnchor(anchor, p);
        anchorTree.insert(pa);
        anchorMap.put(anchor, pa);

        Shape shape = p.getShape();
        shape.each((x, y) -> {
            if(shape.get(x, y)){
                Tile member = world.tile(anchor.x + x, anchor.y + y);
                if(member != null){
                    ObjectMap<Block, Tile> map = tileToAnchorMap.get(member.pos());
                    if(map == null){
                        map = new ObjectMap<>();
                        tileToAnchorMap.put(member.pos(), map);
                    }
                    map.put(pBlock, anchor);

                    ObjectSet<Block> claimedSet = localClaimed.get(member.pos());
                    if(claimedSet == null){
                        claimedSet = new ObjectSet<>();
                        localClaimed.put(member.pos(), claimedSet);
                    }
                    claimedSet.add(pBlock);
                }
            }
        });
    }

    private static boolean isPatternComplete(Patterned patterned, Tile anchor, IntMap<ObjectSet<Block>> localClaimed){
        if (!(patterned instanceof Block pBlock)) return false;

        for(int x = 0; x < patterned.getShape().width(); x++){
            for(int y = 0; y < patterned.getShape().height(); y++){
                if(patterned.getShape().get(x, y)){
                    Tile other = world.tile(anchor.x + x, anchor.y + y);
                    if(!hasPatterned(other, patterned)){
                        return false;
                    }
                    if(localClaimed != null){
                         ObjectSet<Block> claimed = localClaimed.get(other.pos());
                         if(claimed != null && claimed.contains(pBlock)){
                             return false;
                         }
                    }
                }
            }
        }
        return true;
    }

    public static boolean isPatternComplete(Patterned patterned, Tile anchor){
        return isPatternComplete(patterned, anchor, null);
    }

    public static Tile getAnchor(Tile tile, Patterned p){
        if(tile == null || !(p instanceof Block pBlock)) return null;
        ObjectMap<Block, Tile> map = tileToAnchorMap.get(tile.pos());
        if(map == null) return null;
        return map.get(pBlock);
    }

    public static Seq<Patterned> getPatternedBlocks(Tile tile){
        Seq<Patterned> result = new Seq<>(3);
        if (tile == null) return result;

        if (tile.block() instanceof Patterned p) result.add(p);
        if (tile.floor() instanceof Patterned p) result.add(p);
        if (tile.overlay() instanceof Patterned p) result.add(p);
        return result;
    }

    public static boolean hasPatterned(Tile tile, Patterned p){
        if (tile == null || !(p instanceof Block pBlock)) return false;
        return tile.block() == pBlock || tile.floor() == pBlock || tile.overlay() == pBlock;
    }
    
    private static class PatternAnchor implements QuadTree.QuadTreeObject{
        public final Tile tile;
        public final Patterned patterned;
        public final Shape shape;
        public final Rect bounds = new Rect();

        public PatternAnchor(Tile tile, Patterned patterned){
            this.tile = tile;
            this.patterned = patterned;
            this.shape = patterned.getShape();
            this.bounds.set(tile.x, tile.y, shape.width(), shape.height());
        }

        @Override
        public void hitbox(Rect rect){
            rect.set(this.bounds);
        }
    }
}
