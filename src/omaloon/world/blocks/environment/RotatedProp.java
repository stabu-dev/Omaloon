package omaloon.world.blocks.environment;

import arc.graphics.g2d.*;
import arc.math.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;

public class RotatedProp extends Prop{
    public RotatedProp(String name){
        super(name);
        breakable = true;
        alwaysReplace = true;
        instantDeconstruct = true;
        breakEffect = Fx.breakProp;
        breakSound = Sounds.rockBreak;
    }

    @Override
    public void drawBase(Tile tile){
        float rot = Mathf.randomSeed(tile.pos(), 0, 360);

        Draw.z(Layer.blockProp);
        if(customShadowRegion.found()){
            Draw.z(Layer.blockProp - 1);
            Draw.rect(customShadowRegion, tile.worldx(), tile.worldy(), rot);
        }
        Draw.rect(variants > 0 ? variantRegions[Mathf.randomSeed(tile.pos(), 0, Math.max(0, variantRegions.length - 1))] : region, tile.worldx(), tile.worldy(), rot);
    }

    @Override
    public TextureRegion[] icons(){
        return new TextureRegion[]{region};
    }
}
