package omaloon.world.draw;

import arc.func.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.draw.*;
import omaloon.world.interfaces.*;

public class DrawMaskedLiquidTiles extends DrawBlock{
    public float padLeft, padRight, padTop, padBottom;

    public Intf<Building> index;
    public Floatf<Building> alpha = a -> 1f;

    public DrawMaskedLiquidTiles(Intf<Building> index) {
        this.index = index;
    }

    @Override
    public void draw(Building build){
        int index = this.index.get(build);
        @Nullable Liquid drawn = ((HasPressure) build).pressure().getMain();
        if (drawn != null) LiquidBlock.drawTiledFrames(
            build.block.size,
            build.x, build.y,
            (index & 4) == 0 ? padLeft : 0,
            (index & 1) == 0 ? padRight : 0,
            (index & 2) == 0 ? padTop : 0,
            (index & 8) == 0 ? padBottom : 0,
            drawn,
            alpha.get(build)
        );
    }
}
