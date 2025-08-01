package omaloon.world.patterns;

import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.world.*;

import static mindustry.Vars.*;

public class PatternManager{
    private static final Seq<Tile> anchors = new Seq<>();
    private static final IntMap<Tile> tileToAnchorMap = new IntMap<>();

    private static final Seq<Tile> toRemove = new Seq<>();
    private static final IntSet toRecache = new IntSet();
    private static final IntSet localClaimed = new IntSet();

    private static final Seq<Tile> floodFillQueue = new Seq<>();
    private static final IntSet visitedTiles = new IntSet();

    public static void init(){
        anchors.clear();
        tileToAnchorMap.clear();
        resolveRegion(0, 0, world.width(), world.height());
    }

    public static void updateAround(Tile tile){
        if(tile == null || !(tile.floor() instanceof Patterned)) return;

        Rect dirtyRect = findContiguousRegion(tile);

        toRemove.clear();
        toRecache.clear();
        Rect shapeRect = Tmp.r2;

        for(Tile anchor : anchors){
            Patterned p = (Patterned)anchor.floor();
            shapeRect.set(anchor.x, anchor.y, p.getShape().width(), p.getShape().height());

            if(shapeRect.overlaps(dirtyRect)){
                toRemove.add(anchor);
                dirtyRect.merge(shapeRect);
            }
        }

        for(Tile anchor : toRemove){
            anchors.remove(anchor, true);
            removeTilesFromMap(anchor);
        }

        resolveRegion((int)dirtyRect.x, (int)dirtyRect.y, (int)dirtyRect.width, (int)dirtyRect.height);

        toRecache.each(pos -> {
            Tile t = world.tile(pos);
            if(t != null) renderer.blocks.floor.recacheTile(t);
        });
    }

    private static Rect findContiguousRegion(Tile startTile){
        Block type = startTile.floor();
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

        for(Tile anchor : anchors){
            Patterned p = (Patterned)anchor.floor();
            shapeRect.set(anchor.x, anchor.y, p.getShape().width(), p.getShape().height());
            if(!resolveRect.overlaps(shapeRect)){
                p.getShape().each((x, y) -> {
                    if(p.getShape().get(x, y)){
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

                if(isPatternComplete(p, tile, localClaimed)){
                    addAnchor(tile);
                }
            }
        }
    }

    private static void addAnchor(Tile anchor){
        anchors.add(anchor);
        Patterned p = (Patterned)anchor.floor();
        p.getShape().each((x, y) -> {
            if(p.getShape().get(x, y)){
                Tile member = world.tile(anchor.x + x, anchor.y + y);
                if(member != null){
                    tileToAnchorMap.put(member.pos(), anchor);
                    localClaimed.add(member.pos());
                    toRecache.add(member.pos());
                }
            }
        });
    }

    private static void removeTilesFromMap(Tile anchor){
        Patterned p = (Patterned)anchor.floor();
        p.getShape().each((x, y) -> {
            if(p.getShape().get(x, y)){
                Tile member = world.tile(anchor.x + x, anchor.y + y);
                if(member != null){
                    tileToAnchorMap.remove(member.pos());
                    toRecache.add(member.pos());
                }
            }
        });
    }

    private static boolean isPatternComplete(Patterned patterned, Tile anchor, IntSet claimed){
        for(int x = 0; x < patterned.getShape().width(); x++){
            for(int y = 0; y < patterned.getShape().height(); y++){
                if(patterned.getShape().get(x, y)){
                    Tile other = world.tile(anchor.x + x, anchor.y + y);
                    if(other == null || other.floor() != patterned || claimed.contains(other.pos())){
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