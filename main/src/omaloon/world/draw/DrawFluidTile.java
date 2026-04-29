package omaloon.world.draw;

import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.draw.*;
import omaloon.world.interfaces.*;

public class DrawFluidTile extends DrawLiquidTile{
    public DrawFluidTile(Liquid liquid){
        super(liquid);
    }

    public HasPressure cast(Building build){
        try{
            return (HasPressure)build;
        }catch(Exception e){
            throw new RuntimeException("This drawer should be used on a building that implements HasPressure", e);
        }
    }

    @Override
    public void draw(Building build){
        Liquid drawn = drawLiquid == null ? cast(build).pressure().getMain() : drawLiquid;
        LiquidBlock.drawTiledFrames(
            build.block.size,
            build.x, build.y,
            padLeft, padRight, padTop, padBottom,
            drawn,
            (cast(build).getFluid(drawn) / Math.max(1f, cast(build).getFluid(drawn) + Math.abs(cast(build).getFluid(null)))) * alpha
        );

    }
}
