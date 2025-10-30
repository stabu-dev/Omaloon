package omaloon.world.patterns;

import arc.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.world.*;
import omaloon.type.shape.*;

import static mindustry.Vars.*;

public class PatternManager{

    private static final ObjectMap<Tile, PatternAnchor> anchorMap = new ObjectMap<>();
    private static final IntMap<Tile> tileToAnchorMap = new IntMap<>();

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
        resolveTiles(allTiles, new IntSet());
    }

    /** This is a lightweight method that just queues a tile to be processed. */
    public static void updateAround(Tile tile){
        if(tile == null || world.isGenerating()) return;
        if(!dirtyTiles.contains(tile)) dirtyTiles.add(tile);
        Core.app.post(PatternManager::processDirtyTiles);
    }

    /** This processes all queued changes, using a background thread for heavy computation. */
    private static void processDirtyTiles(){
        if(dirtyTiles.isEmpty()) return;
        if(runningThread != null && runningThread.isAlive()){
            return;
        }

        final Seq<Tile> dirty = new Seq<>(dirtyTiles);
        dirtyTiles.clear();

        final IntMap<Tile> tileToAnchorMapCopy = new IntMap<>(tileToAnchorMap);
        final ObjectMap<Tile, PatternAnchor> anchorMapCopy = new ObjectMap<>(anchorMap);

        runningThread = Threads.thread("Pattern-Resolver", () -> {
            IntSet visited = new IntSet();
            IntSet toResolve = new IntSet();
            ObjectSet<PatternAnchor> toRemove = new ObjectSet<>();

            for(Tile tile : dirty){
                if(visited.contains(tile.pos())) continue;

                Patterned p = getPatterned(tile);
                if(p != null){
                    IntSet contiguous = findContiguousTiles(tile, p, visited);
                    toResolve.addAll(contiguous);

                    contiguous.each(pos -> {
                        Tile anchorTile = tileToAnchorMapCopy.get(pos);
                        if(anchorTile != null){
                            PatternAnchor anchor = anchorMapCopy.get(anchorTile);
                            if(anchor != null) toRemove.add(anchor);
                        }
                    });
                }
            }

            toRemove.each(anchor -> {
                anchor.shape.each((x, y) -> {
                    if(anchor.shape.get(x, y)){
                        Tile member = world.tile(anchor.tile.x + x, anchor.tile.y + y);
                        if(member != null){
                            toResolve.add(member.pos());
                        }
                    }
                });
            });

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
                                tileToAnchorMap.remove(member.pos());
                            }
                        }
                    });
                });

                for(PatternAnchor pa : anchorsToAdd){
                    addAnchor(pa.patterned, pa.tile, new IntSet());
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

    private static void resolveTilesAsync(IntSet toResolve, Seq<PatternAnchor> anchorsToAdd){
        IntSet resolved = new IntSet();
        IntSet processedAnchors = new IntSet(); // to avoid duplicate work
        if(toResolve.isEmpty()) return;
        Rect bounds = Tmp.r1.set(Point2.x(toResolve.first()), Point2.y(toResolve.first()), 0, 0);
        toResolve.each(pos -> bounds.merge(Point2.x(pos), Point2.y(pos)));

        for(int y = (int)bounds.y; y < (int)(bounds.y + bounds.height + 1); y++){
            for(int x = (int)bounds.x; x < (int)(bounds.x + bounds.width + 1); x++){
                int pos = Point2.pack(x, y);

                if(toResolve.contains(pos) && !resolved.contains(pos)){
                    Tile tile = world.tile(x, y);
                    Patterned p = getPatterned(tile);
                    if(tile != null && p != null){
                        Shape shape = p.getShape();
                        shape.each((sx, sy) -> {
                            if(shape.get(sx, sy)){
                                Tile potentialAnchor = world.tile(tile.x - sx, tile.y - sy);
                                if(potentialAnchor != null && !processedAnchors.contains(potentialAnchor.pos())){
                                    processedAnchors.add(potentialAnchor.pos());
                                    if(isPatternComplete(p, potentialAnchor, resolved)){
                                        anchorsToAdd.add(new PatternAnchor(potentialAnchor, p));
                                        p.getShape().each((ssx, ssy) -> {
                                            if(p.getShape().get(ssx, ssy)){
                                                Tile member = world.tile(potentialAnchor.x + ssx, potentialAnchor.y + ssy);
                                                if(member != null){
                                                    resolved.add(member.pos());
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    private static IntSet findContiguousTiles(Tile startTile, Patterned patterned, IntSet visited){
        IntSet contiguous = new IntSet();
        Seq<Tile> floodFillQueue = new Seq<>();

        floodFillQueue.add(startTile);
        visited.add(startTile.pos());
        contiguous.add(startTile.pos());

        while(floodFillQueue.size > 0){
            Tile current = floodFillQueue.pop();
            for(int i = 0; i < 4; i++){
                Tile next = current.nearby(i);
                if(next != null && getPatterned(next) == patterned && !visited.contains(next.pos())){
                    visited.add(next.pos());
                    contiguous.add(next.pos());
                    floodFillQueue.add(next);
                }
            }
        }
        return contiguous;
    }

    private static void resolveTiles(IntSet toResolve, IntSet resolved){
        if(toResolve.isEmpty()) return;

        IntSet processedAnchors = new IntSet(); // to avoid duplicate work
        Rect bounds = Tmp.r1.set(Point2.x(toResolve.first()), Point2.y(toResolve.first()), 0, 0);
        toResolve.each(pos -> bounds.merge(Point2.x(pos), Point2.y(pos)));

        for(int y = (int)bounds.y; y < (int)(bounds.y + bounds.height + 1); y++){
            for(int x = (int)bounds.x; x < (int)(bounds.x + bounds.width + 1); x++){
                int pos = Point2.pack(x, y);

                if(toResolve.contains(pos) && !resolved.contains(pos)){
                    Tile tile = world.tile(x, y);
                    Patterned p = getPatterned(tile);
                    if(tile != null && p != null){
                        Shape shape = p.getShape();
                        shape.each((sx, sy) -> {
                            if(shape.get(sx, sy)){
                                Tile potentialAnchor = world.tile(tile.x - sx, tile.y - sy);
                                if(potentialAnchor != null && !processedAnchors.contains(potentialAnchor.pos())){
                                    processedAnchors.add(potentialAnchor.pos());
                                    if(isPatternComplete(p, potentialAnchor, resolved)){
                                        addAnchor(p, potentialAnchor, resolved);
                                    }
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    private static void addAnchor(Patterned p, Tile anchor, IntSet localClaimed){
        Shape shape = p.getShape();

        PatternAnchor pa = new PatternAnchor(anchor, p);
        anchorTree.insert(pa);
        anchorMap.put(anchor, pa);

        shape.each((x, y) -> {
            if(shape.get(x, y)){
                Tile member = world.tile(anchor.x + x, anchor.y + y);
                if(member != null){
                    tileToAnchorMap.put(member.pos(), anchor);
                    localClaimed.add(member.pos());
                }
            }
        });
    }

    private static boolean isPatternComplete(Patterned patterned, Tile anchor, IntSet localClaimed){
        for(int x = 0; x < patterned.getShape().width(); x++){
            for(int y = 0; y < patterned.getShape().height(); y++){
                if(patterned.getShape().get(x, y)){
                    Tile other = world.tile(anchor.x + x, anchor.y + y);
                    if(other == null || getPatterned(other) != patterned || (localClaimed != null && localClaimed.contains(other.pos()))){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static boolean isPatternComplete(Patterned patterned, Tile anchor){
        return isPatternComplete(patterned, anchor, null);
    }

    public static Tile getAnchor(Tile tile){
        return tileToAnchorMap.get(tile.pos());
    }

    public static Patterned getPatterned(Tile tile){
        if(tile == null) return null;

        if(tile.block() instanceof Patterned){
            return (Patterned)tile.block();
        }
        if(tile.floor() instanceof Patterned){
            return (Patterned)tile.floor();
        }
        if(tile.overlay() instanceof Patterned){
            return (Patterned)tile.overlay();
        }

        return null;
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
