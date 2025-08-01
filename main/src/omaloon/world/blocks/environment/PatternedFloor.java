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

public class PatternedFloor extends Floor implements Patterned{
    public Shape shape = new RectanglePatternShape();
    public Block parent = Blocks.stone;
    public boolean drawPatternEdges = true;
    public boolean drawOnTop = false;

    public PatternedFloor(String name){
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

        if(anchor != null){
            if(parent instanceof Floor p) p.drawBase(tile);
        }else{
            if(parent instanceof Floor p) p.drawBase(tile);
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
        if(drawOnTop){
            Tile anchor = PatternManager.getAnchor(tile);
            if(isDrawAnchor(tile, anchor)){
                drawPattern(anchor);
            }
        }
        super.drawOverlay(tile);
    }

    private boolean isDrawAnchor(Tile tile, Tile anchor){
        if(anchor == null) return false;
        return tile.x == anchor.x + shape.width() - 1 && tile.y == anchor.y + shape.height() - 1;
    }

    @Override
    public Shape getShape(){
        return shape;
    }

    public void drawPattern(Tile anchor){
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
    }

    @Override
    public Block getParent(){
        return parent;
    }

    @Override
    public boolean drawOnTop(){
        return drawOnTop;
    }
}