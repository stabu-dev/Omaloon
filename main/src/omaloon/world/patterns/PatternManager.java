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
        resolveRegion(0, 0, world.width(), world.height());
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
        if(dirtyTiles.isEmpty()) return;

        IntSet processed = new IntSet();
        Rect totalDirtyRect = Tmp.r1;
        boolean first = true;

        for(Tile tile : dirtyTiles){
            if(processed.contains(tile.pos())) continue;

            Block type = tile.floor();
            if(type instanceof Patterned){
                Rect contiguousRect = findContiguousRegion(tile, type);

                if(first){
                    totalDirtyRect.set(contiguousRect);
                    first = false;
                }else{
                    totalDirtyRect.merge(contiguousRect);
                }

                for(int y = (int)contiguousRect.y; y < (int)(contiguousRect.y + contiguousRect.height); y++){
                    for(int x = (int)contiguousRect.x; x < (int)(contiguousRect.x + contiguousRect.width); x++){
                        processed.add(Point2.pack(x, y));
                    }
                }
            }
        }
        dirtyTiles.clear();

        if(first) return;

        ObjectMap<Tile, Shape> toRemove = new ObjectMap<>();
        IntSet toRecache = new IntSet();

        anchorTree.intersect(totalDirtyRect, anchor -> toRemove.put(anchor.tile, anchor.shape));

        Rect resolveRect = new Rect(totalDirtyRect);
        for(var entry : toRemove.entries()){
            resolveRect.merge(Tmp.r2.set(entry.key.x, entry.key.y, entry.value.width(), entry.value.height()));
        }

        for(var entry : toRemove.entries()){
            PatternAnchor pa = anchorMap.get(entry.key);
            if(pa != null){
                anchorTree.remove(pa);
                anchorMap.remove(entry.key);
                removeTilesFromMap(entry.key, entry.value, toRecache);
            }
        }

        resolveRegion((int)resolveRect.x, (int)resolveRect.y, (int)resolveRect.width, (int)resolveRect.height);

        toRecache.each(pos -> {
            Tile t = world.tile(pos);
            if(t != null) renderer.blocks.floor.recacheTile(t);
        });
    }

    private static Rect findContiguousRegion(Tile startTile, Block type){
        Seq<Tile> floodFillQueue = new Seq<>();
        IntSet visitedTiles = new IntSet();
        Rect rect = Tmp.r1.set(startTile.x, startTile.y, 1, 1);

        floodFillQueue.add(startTile);
        visitedTiles.add(startTile.pos());

        while(floodFillQueue.size > 0){
            Tile current = floodFillQueue.pop();
            rect.merge(current.x, current.y);
            for(int i = 0; i < 4; i++){
                Tile next = current.nearby(i);
                if(next != null && next.floor() == type && !visitedTiles.contains(next.pos())){
                    visitedTiles.add(next.pos());
                    floodFillQueue.add(next);
                }
            }
        }
        return rect;
    }

    private static void resolveRegion(int startX, int startY, int width, int height){
        IntSet localClaimed = new IntSet();
        Rect resolveRect = Tmp.r1.set(startX, startY, width, height);

        for(PatternAnchor pa : anchorTree.objects){
            if(!pa.bounds.overlaps(resolveRect)){
                pa.shape.each((x, y) -> {
                    if(pa.shape.get(x, y)){
                        Tile member = world.tile(pa.tile.x + x, pa.tile.y + y);
                        if(member != null && resolveRect.contains(member.x, member.y)){
                            localClaimed.add(member.pos());
                        }
                    }
                });
            }
        }

        for(int y = startY; y < startY + height; y++){
            for(int x = startX; x < startX + width; x++){
                Tile tile = world.tile(x, y);
                if(tile == null || !(tile.floor() instanceof Patterned p) || localClaimed.contains(tile.pos())) continue;

                if(isPatternComplete(p, tile, localClaimed)){
                    addAnchor(tile, localClaimed);
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

    private static void removeTilesFromMap(Tile anchor, Shape shape, IntSet toRecache){
        shape.each((x, y) -> {
            if(shape.get(x, y)){
                Tile member = world.tile(anchor.x + x, anchor.y + y);
                if(member != null){
                    tileToAnchorMap.remove(member.pos());
                    toRecache.add(member.pos());
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