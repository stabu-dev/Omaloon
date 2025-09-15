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
    private static boolean updateScheduled = false;

    public static void init(){
        anchorTree = new QuadTree<>(new Rect(0, 0, world.unitWidth(), world.unitHeight()));
        anchorMap.clear();
        tileToAnchorMap.clear();
        dirtyTiles.clear();
        updateScheduled = false;
        IntSet allTiles = new IntSet();
        for(int y = 0; y < world.height(); y++){
            for(int x = 0; x < world.width(); x++){
                allTiles.add(Point2.pack(x, y));
            }
        }
        resolveTiles(allTiles, new IntSet());
    }

    /** This is a lightweight method that just queues a tile to be processed in the next frame. */
    public static void updateAround(Tile tile){
        if(tile == null || world.isGenerating()) return;
        if(!dirtyTiles.contains(tile)) dirtyTiles.add(tile);

        if(!updateScheduled){
            updateScheduled = true;
            Core.app.post(() -> {
                processDirtyTiles();
                updateScheduled = false;
            });
        }
    }

    /** This runs once per frame, processing all queued changes in a single batch. */
    private static void processDirtyTiles(){
        if (dirtyTiles.isEmpty()) return;

        IntSet visited = new IntSet();
        IntSet toRecache = new IntSet();
        IntSet toResolve = new IntSet();
        ObjectSet<PatternAnchor> toRemove = new ObjectSet<>();

        for (Tile tile : dirtyTiles) {
            if (visited.contains(tile.pos())) continue;

            Block type = tile.floor();
            if (type instanceof Patterned) {
                IntSet contiguous = findContiguousTiles(tile, type, visited);
                toResolve.addAll(contiguous);

                contiguous.each(pos -> {
                    Tile anchorTile = tileToAnchorMap.get(pos);
                    if (anchorTile != null) {
                        PatternAnchor anchor = anchorMap.get(anchorTile);
                        if(anchor != null) toRemove.add(anchor);
                    }
                });
            }
        }

        toRemove.each(anchor -> {
            anchorTree.remove(anchor);
            anchorMap.remove(anchor.tile);
            anchor.shape.each((x, y) -> {
                if (anchor.shape.get(x, y)) {
                    Tile member = world.tile(anchor.tile.x + x, anchor.tile.y + y);
                    if (member != null) {
                        tileToAnchorMap.remove(member.pos());
                        toResolve.add(member.pos());
                        toRecache.add(member.pos());
                    }
                }
            });
        });

        dirtyTiles.clear();

        resolveTiles(toResolve, new IntSet());

        toRecache.each(pos -> {
            Tile t = world.tile(pos);
            if (t != null) renderer.blocks.floor.recacheTile(t);
        });
    }

    private static IntSet findContiguousTiles(Tile startTile, Block type, IntSet visited) {
        IntSet contiguous = new IntSet();
        Seq<Tile> floodFillQueue = new Seq<>();

        floodFillQueue.add(startTile);
        visited.add(startTile.pos());
        contiguous.add(startTile.pos());

        while (floodFillQueue.size > 0) {
            Tile current = floodFillQueue.pop();
            for (int i = 0; i < 4; i++) {
                Tile next = current.nearby(i);
                if (next != null && next.floor() == type && !visited.contains(next.pos())) {
                    visited.add(next.pos());
                    contiguous.add(next.pos());
                    floodFillQueue.add(next);
                }
            }
        }
        return contiguous;
    }

    private static void resolveTiles(IntSet toResolve, IntSet resolved) {
        if (toResolve.isEmpty()) return;

        Rect bounds = Tmp.r1.set(Point2.x(toResolve.first()), Point2.y(toResolve.first()), 0, 0);
        toResolve.each(pos -> bounds.merge(Point2.x(pos), Point2.y(pos)));

        for (int y = (int)bounds.y; y < (int)(bounds.y + bounds.height + 1); y++) {
            for (int x = (int)bounds.x; x < (int)(bounds.x + bounds.width + 1); x++) {
                int pos = Point2.pack(x, y);

                if (toResolve.contains(pos) && !resolved.contains(pos)) {
                    Tile tile = world.tile(x, y);
                    if (tile != null && tile.floor() instanceof Patterned p) {
                        if (isPatternComplete(p, tile, resolved)) {
                            addAnchor(tile, resolved);
                        }
                    }
                }
            }
        }
    }

    private static void addAnchor(Tile anchor, IntSet localClaimed){
        if(!(anchor.floor() instanceof Patterned p)) return;
        Shape shape = p.getShape();

        PatternAnchor pa = new PatternAnchor(anchor, shape);
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
                    if(other == null || other.floor() != patterned || (localClaimed != null && localClaimed.contains(other.pos()))){
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

    private static class PatternAnchor implements QuadTree.QuadTreeObject{
        public final Tile tile;
        public final Shape shape;
        public final Rect bounds = new Rect();

        public PatternAnchor(Tile tile, Shape shape){
            this.tile = tile;
            this.shape = shape;
            this.bounds.set(tile.x, tile.y, shape.width(), shape.height());
        }

        @Override
        public void hitbox(Rect rect){
            rect.set(this.bounds);
        }
    }
}
