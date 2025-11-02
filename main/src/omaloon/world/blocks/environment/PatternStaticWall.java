package omaloon.world.blocks.environment;

import arc.graphics.g2d.*;
import mindustry.content.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import omaloon.type.shape.*;
import omaloon.world.patterns.*;

import static mindustry.Vars.tilesize;

public class PatternStaticWall extends StaticWall implements Patterned{
    public Shape shape = new RectanglePatternShape();
    public Block parent = Blocks.stoneWall;
    public boolean drawOnTop = true;

    private transient TextureRegion[][][] slicedRegions;

    public PatternStaticWall(String name){
        super(name);
        this.autotile = false;
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
    public void createIcons(MultiPacker packer){
        super.createIcons(packer);
        shape.load();
    }

    @Override
    public void blockChanged(Tile tile){
        super.blockChanged(tile);
        PatternManager.updateAround(tile);
        for(int i = 0; i < 4; i++){
            Tile near = tile.nearby(i);
            if(near != null){
                PatternManager.updateAround(near);
            }
        }
    }

    @Override
    public void drawBase(Tile tile){
        Tile anchor = PatternManager.getAnchor(tile, this);

        if(anchor == null){
            if(parent instanceof StaticWall p && p != Blocks.air){
                p.drawBase(tile);
            }else{
                super.drawBase(tile);
            }
            return;
        }

        StaticWall wallToDraw = (parent instanceof StaticWall p && p != Blocks.air) ? p : this;
        Draw.rect(wallToDraw.region, tile.worldx(), tile.worldy());

        if(!drawOnTop){
            drawPatternTile(tile);
        }

        if(tile.overlay().wallOre){
            tile.overlay().drawBase(tile);
        }

        if(drawOnTop){
            drawPatternTile(tile);
        }
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
                    if(this.variants > 0){
                        variant = variant(anchor.x, anchor.y, this.variants);
                    }
                    TextureRegion[][] regions = slicedRegions[variant];
                    if(relativeX >= 0 && relativeX < regions.length && textureY >= 0 && textureY < regions[relativeX].length){
                        Draw.rect(regions[relativeX][textureY], tile.worldx(), tile.worldy());
                    }
                }
            }else{
                PatternManager.updateAround(tile);
            }
        }
    }

    @Override
    public Shape getShape(){
        return shape;
    }
}
