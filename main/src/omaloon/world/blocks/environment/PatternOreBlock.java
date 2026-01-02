package omaloon.world.blocks.environment;

import arc.graphics.g2d.*;
import mindustry.content.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import omaloon.type.shape.*;
import omaloon.world.patterns.*;

import static mindustry.Vars.tilesize;

public class PatternOreBlock extends OreBlock implements Patterned{
    public Shape shape = new RectanglePatternShape();
    public Block parent = Blocks.oreCopper;
    public boolean drawParentUnder = false;

    private transient TextureRegion[][][] slicedRegions;

    public PatternOreBlock(String name, Item ore){
        super(name, ore);
        variants = 0;
    }

    public PatternOreBlock(Item ore){
        this(ore.name + "-pattern-ore", ore);
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
        PatternManager.updateAround(tile, this);
        for(int i = 0; i < 4; i++){
            Tile near = tile.nearby(i);
            if(near != null){
                PatternManager.updateAround(near, this);
            }
        }
    }

    @Override
    public void drawBase(Tile tile){
        Tile anchor = PatternManager.getAnchor(tile, this);

        if(anchor == null){
            if(parent instanceof Floor p && p != Blocks.air){
                p.drawMain(tile);
            }else{
                super.drawBase(tile);
            }
            return;
        }

        if(drawParentUnder){
            if(parent instanceof Floor p && p != Blocks.air){
                p.drawMain(tile);
            }else{
                super.drawBase(tile);
            }
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
                    if(relativeX >= 0 && relativeX < regions.length && textureY >= 0 && textureY < regions[relativeX].length){
                        Draw.rect(regions[relativeX][textureY], tile.worldx(), tile.worldy());
                    }
                }
            }else{
                PatternManager.updateAround(tile, this);
            }
        }
    }

    @Override
    public Shape getShape(){
        return shape;
    }
}
