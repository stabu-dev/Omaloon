package omaloon.world.patterns;

import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.world.*;
import omaloon.type.shape.*;

import static mindustry.Vars.*;

public class PatternManager{
    private static final ObjectMap<Tile, Shape> anchorShapes = new ObjectMap<>();
    private static final IntMap<Tile> tileToAnchorMap = new IntMap<>();

    private static final ObjectMap<Tile, Shape> toRemove = new ObjectMap<>();
    private static final IntSet toRecache = new IntSet();
    private static final IntSet localClaimed = new IntSet();

    private static final Seq<Tile> floodFillQueue = new Seq<>();
    private static final IntSet visitedTiles = new IntSet();

    public static void init(){
        anchorShapes.clear();
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
        } else {
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
        } else {
            Tile oldAnchor = getAnchor(tile);
            if(oldAnchor != null){
                Shape oldShape = anchorShapes.get(oldAnchor);
                if(oldShape != null){
                    dirtyRect = Tmp.r1.set(oldAnchor.x, oldAnchor.y, oldShape.width(), oldShape.height());
                } else {
                    return;
                }
            } else {
                return;
            }
        }

        toRemove.clear();
        toRecache.clear();
        Rect shapeRect = Tmp.r2;

        for(var entry : anchorShapes.entries()){
            Tile anchor = entry.key;
            Shape shape = entry.value;
            shapeRect.set(anchor.x, anchor.y, shape.width(), shape.height());

            if(shapeRect.overlaps(dirtyRect)){
                toRemove.put(anchor, shape);
            }
        }

        for(var entry : toRemove.entries()){
            anchorShapes.remove(entry.key);
            removeTilesFromMap(entry.key, entry.value);
        }

        resolveRegion((int)dirtyRect.x, (int)dirtyRect.y, (int)dirtyRect.width, (int)dirtyRect.height);

        toRecache.each(pos -> {
            Tile t = world.tile(pos);
            if(t != null) renderer.blocks.floor.recacheTile(t);
        });
    }

    private static void performCleanup(Rect dirtyRect){
        toRemove.clear();
        toRecache.clear();
        Rect shapeRect = Tmp.r2;

        for(var entry : anchorShapes.entries()){
            Tile anchor = entry.key;
            Shape shape = entry.value;
            shapeRect.set(anchor.x, anchor.y, shape.width(), shape.height());

            if(shapeRect.overlaps(dirtyRect)){
                toRemove.put(anchor, shape);
                dirtyRect.merge(shapeRect);
            }
        }

        for(var entry : toRemove.entries()){
            anchorShapes.remove(entry.key);
            removeTilesFromMap(entry.key, entry.value);
        }

        resolveRegion((int)dirtyRect.x, (int)dirtyRect.y, (int)dirtyRect.width, (int)dirtyRect.height);

        toRecache.each(pos -> {
            Tile t = world.tile(pos);
            if(t != null) renderer.blocks.floor.recacheTile(t);
        });
    }

    private static Rect findContiguousRegion(Tile startTile, Block type){
        Rect rect = Tmp.r1.set(startTile.x, startTile.y, 1, 1);

        floodFillQueue.clear();
        visitedTiles.clear();

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
        localClaimed.clear();

        Rect resolveRect = Tmp.r1.set(startX, startY, width, height);
        Rect shapeRect = Tmp.r2;

        for(var entry : anchorShapes.entries()){
            Tile anchor = entry.key;
            Shape shape = entry.value;
            shapeRect.set(anchor.x, anchor.y, shape.width(), shape.height());
            if(!resolveRect.overlaps(shapeRect)){
                shape.each((x, y) -> {
                    if(shape.get(x, y)){
                        Tile member = world.tile(anchor.x + x, anchor.y + y);
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

                if(isPatternComplete(p, tile)){
                    addAnchor(tile);
                }
            }
        }
    }

    private static void addAnchor(Tile anchor){
        if(!(anchor.floor() instanceof Patterned p)) return; // Safety check
        Shape shape = p.getShape();
        anchorShapes.put(anchor, shape);

        shape.each((x, y) -> {
            if(shape.get(x, y)){
                Tile member = world.tile(anchor.x + x, anchor.y + y);
                if(member != null){
                    tileToAnchorMap.put(member.pos(), anchor);
                    localClaimed.add(member.pos());
                    toRecache.add(member.pos());
                }
            }
        });
    }

    private static void removeTilesFromMap(Tile anchor, Shape shape){
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

    public static boolean isPatternComplete(Patterned patterned, Tile anchor){
        for(int x = 0; x < patterned.getShape().width(); x++){
            for(int y = 0; y < patterned.getShape().height(); y++){
                if(patterned.getShape().get(x, y)){
                    Tile other = world.tile(anchor.x + x, anchor.y + y);
                    if(other == null || other.floor() != patterned || PatternManager.localClaimed.contains(other.pos())){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static Tile getAnchor(Tile tile){
        return tileToAnchorMap.get(tile.pos());
    }
}