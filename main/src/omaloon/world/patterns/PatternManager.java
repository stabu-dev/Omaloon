package omaloon.world.patterns;

import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.world.*;
import omaloon.type.shape.*;

import static mindustry.Vars.*;

public class PatternManager{
    private static final ObjectMap<Tile, PatternAnchor> anchorMap = new ObjectMap<>();
    private static final IntMap<Tile> tileToAnchorMap = new IntMap<>();
    // Core data structures - these must be static and persistent.
    private static QuadTree<PatternAnchor> anchorTree;

    public static void init(){
        anchorTree = new QuadTree<>(new Rect(0, 0, world.unitWidth(), world.unitHeight()));
        anchorMap.clear();
        tileToAnchorMap.clear();
        resolveRegion(0, 0, world.width(), world.height());
    }

    public static void updateAround(Tile tile){
        if(tile == null) return;

        Tile startTile = null;
        Block typeToSearch = null;

        if(tile.floor() instanceof Patterned p){
            startTile = tile;
            typeToSearch = (Block)p;
        }else{
            for(int i = 0; i < 4; i++){
                Tile n = tile.nearby(i);
                if(n != null && n.floor() instanceof Patterned p){
                    startTile = n;
                    typeToSearch = (Block)p;
                    break;
                }
            }
        }

        Rect dirtyRect;
        if(startTile != null){
            dirtyRect = findContiguousRegion(startTile, typeToSearch);
        }else{
            Tile oldAnchor = getAnchor(tile);
            if(oldAnchor != null){
                PatternAnchor pa = anchorMap.get(oldAnchor);
                if(pa != null){
                    dirtyRect = Tmp.r1.set(pa.tile.x, pa.tile.y, pa.shape.width(), pa.shape.height());
                }else{
                    return;
                }
            }else{
                return;
            }
        }

        // --- Main Cleanup and Resolve Logic ---
        // These collections are now local to prevent memory leaks.
        ObjectMap<Tile, Shape> toRemove = new ObjectMap<>();
        IntSet toRecache = new IntSet();

        // Find all anchors that overlap the dirty rect.
        anchorTree.intersect(dirtyRect, anchor -> {
            toRemove.put(anchor.tile, anchor.shape);
        });

        // The resolve region must be expanded to contain the full area of all affected patterns.
        Rect resolveRect = new Rect(dirtyRect);
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
        // These collections are local to the method.
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
        // This collection is local to the method.
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
        if(!(anchor.floor() instanceof Patterned p)) return; // Safety check
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

    /** A wrapper for a Tile that implements QuadTreeObject to use the pattern's bounds. */
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