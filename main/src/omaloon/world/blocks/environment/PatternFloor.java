package omaloon.world.blocks.environment;

import arc.graphics.g2d.*;
import arc.math.*;
import mindustry.content.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import omaloon.type.shape.*;
import omaloon.world.patterns.*;

import static mindustry.Vars.*;

public class PatternFloor extends Floor implements Patterned{
    public Shape shape = new RectanglePatternShape();
    public Block parent = Blocks.stone;
    public boolean drawPatternEdges = true;
    public boolean drawOnTop = false;

    public PatternFloor(String name){
        super(name);
        variants = 0;
        blendGroup = this.parent;
    }

    @Override
    public void floorChanged(Tile tile){
        super.floorChanged(tile);
        PatternManager.updateAround(tile);
    }

    @Override
    public void createIcons(MultiPacker packer){
        super.createIcons(packer);
        shape.load();
    }

    @Override
    public void drawBase(Tile tile){
        Tile anchor = PatternManager.getAnchor(tile);

        if(anchor != null && !PatternManager.isPatternComplete(this, anchor)){
            arc.Core.app.post(() -> PatternManager.updateAround(tile));
        }

        if(anchor == null){
            if(parent instanceof Floor p) p.drawMain(tile);
            drawEdges(tile);
        }

        if(!drawOnTop){
            if(isDrawAnchor(tile, anchor)){
                drawPattern(anchor);
            }
        }

        drawOverlay(tile);
    }

    @Override
    public void drawOverlay(Tile tile){
        Tile anchor = PatternManager.getAnchor(tile);

        if(drawOnTop){
            if(anchor == null){
                super.drawOverlay(tile);
            }

            if(isDrawAnchor(tile, anchor)){
                drawPattern(anchor);
            }
        }else{
            super.drawOverlay(tile);
        }
    }

    private boolean isDrawAnchor(Tile tile, Tile anchor){
        if(anchor == null) return false;

        int bestX = -1, bestY = -1;
        // rightmost column index that is part of the shape
        for(int y = 0; y < shape.height(); y++){
            for(int x = 0; x < shape.width(); x++){
                if(shape.get(x, y)){
                    if(x > bestX){
                        bestX = x;
                    }
                }
            }
        }

        if(bestX == -1) return false; // empty shape

        // topmost tile within that rightmost column
        for(int y = 0; y < shape.height(); y++){
            if(shape.get(bestX, y)){
                if(y > bestY){
                    bestY = y;
                }
            }
        }

        return tile.x == anchor.x + bestX && tile.y == anchor.y + bestY;
    }

    @Override
    public Shape getShape(){
        return shape;
    }

    public void drawPattern(Tile anchor){
        if(parent instanceof Floor p){
            shape.each((x, y) -> {
                if(shape.get(x, y)){
                    Tile tileInPattern = world.tile(anchor.x + x, anchor.y + y);
                    if(tileInPattern != null){
                        p.drawMain(tileInPattern);
                    }
                }
            });
        }

        if(drawOnTop){
            drawPatternOverlays(anchor);
        }

        if(variantRegions == null || variantRegions.length == 0) return;

        Mathf.rand.setSeed(anchor.pos());
        Draw.rect(variantRegions[Mathf.randomSeed(anchor.pos(), 0, Math.max(0, variantRegions.length - 1))],
        anchor.worldx() + (shape.width() - 1) * tilesize / 2f,
        anchor.worldy() + (shape.height() - 1) * tilesize / 2f);

        if(drawPatternEdges){
            shape.each((x, y) -> {
                if(shape.get(x, y)){
                    Tile tileInPattern = world.tile(anchor.x + x, anchor.y + y);
                    if(tileInPattern != null){
                        drawEdges(tileInPattern);
                    }
                }
            });
        }

        if(!drawOnTop){
            drawPatternOverlays(anchor);
        }
    }

    private void drawPatternOverlays(Tile anchor){
        shape.each((x, y) -> {
            if(shape.get(x, y)){
                Tile tileInPattern = world.tile(anchor.x + x, anchor.y + y);
                if(tileInPattern != null && tileInPattern.overlay() != Blocks.air){
                    tileInPattern.overlay().drawBase(tileInPattern);
                }
            }
        });
    }
}