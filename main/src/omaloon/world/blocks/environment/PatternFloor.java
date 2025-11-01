package omaloon.world.blocks.environment;

import arc.graphics.g2d.*;
import mindustry.content.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import omaloon.type.shape.*;
import omaloon.world.patterns.*;

import static mindustry.Vars.tilesize;

public class PatternFloor extends Floor implements Patterned{
    public Shape shape = new RectanglePatternShape();
    public Block parent = Blocks.stone;
    public boolean drawPatternEdges = true;
    public boolean drawOnTop = false;

    private transient TextureRegion[][][] slicedRegions;

    public PatternFloor(String name){
        super(name);
        variants = 0;
        blendGroup = this.parent;
    }

    @Override
    public void load(){
        super.load();

        int tilePixelSize = (int)(tilesize / Draw.scl);

        if(variants > 0){
            slicedRegions = new TextureRegion[variants][][];
            for(int i = 0; i < variants; i++){
                slicedRegions[i] = variantRegions[i].split(tilePixelSize, tilePixelSize);
            }
        }else{
            slicedRegions = new TextureRegion[1][][];
            slicedRegions[0] = region.split(tilePixelSize, tilePixelSize);
        }

        shape.load();
    }

    @Override
    public void floorChanged(Tile tile){
        super.floorChanged(tile);
        PatternManager.updateAround(tile);
        for(int i = 0; i < 4; i++){
            Tile near = tile.nearby(i);
            if(near != null){
                PatternManager.updateAround(near);
            }
        }
    }

    @Override
    public void createIcons(MultiPacker packer){
        super.createIcons(packer);
        shape.load();
    }

    @Override
    public void drawMain(Tile tile){
        if(parent instanceof Floor p){
            p.drawMain(tile);
        }
        drawPatternTile(tile);
    }

    private void drawPatternTile(Tile tile){
        Tile anchor = PatternManager.getAnchor(tile, this);

        if(anchor != null && slicedRegions != null){
            if(PatternManager.isPatternComplete(this, anchor)){
                int relativeX = tile.x - anchor.x;
                int relativeY = tile.y - anchor.y;

                if(shape.get(relativeX, relativeY)){
                    int textureY = (shape.height() - 1) - relativeY;

                    int variant = 0;
                    if(variants > 0){
                        variant = variant(anchor.x, anchor.y, variants);
                    }
                    TextureRegion[][] regions = slicedRegions[variant];

                    if(relativeX >= 0 && relativeX < regions.length &&
                    textureY >= 0 && textureY < regions[relativeX].length){

                        Draw.rect(regions[relativeX][textureY], tile.worldx(), tile.worldy());
                    }
                }
            }else{
                PatternManager.updateAround(tile);
            }
        }
    }

    @Override
    public void drawBase(Tile tile){
        Tile anchor = PatternManager.getAnchor(tile, this);

        if(anchor == null){
            if(parent instanceof Floor p){
                p.drawMain(tile);
            }
            drawEdges(tile);

            Floor overlay = tile.overlay();
            if(overlay != Blocks.air && overlay != this){
                overlay.drawBase(tile);
            }
            return;
        }

        if(parent instanceof Floor p){
            p.drawMain(tile);
        }

        if(!drawOnTop){
            drawPatternTile(tile);
        }

        Floor overlay = tile.overlay();
        if(overlay != Blocks.air && overlay != this){
            overlay.drawBase(tile);
        }

        if(drawOnTop){
            drawPatternTile(tile);
        }

        if(drawPatternEdges){
            drawEdges(tile);
        }
    }

    @Override
    public Shape getShape(){
        return shape;
    }
}