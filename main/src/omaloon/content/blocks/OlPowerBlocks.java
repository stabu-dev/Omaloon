package omaloon.content.blocks;

import arc.math.geom.*;
import mindustry.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.draw.*;
import omaloon.world.blocks.power.*;
import omaloon.world.draw.*;

import static mindustry.type.ItemStack.empty;

public class OlPowerBlocks{
    public static Block
    windTurbine,

    end;

    public static void load(){
        windTurbine = new WindGenerator("wind-turbine"){{
            requirements(Category.power, empty);
            drawer = new DrawMulti(
            new DrawDefault(),
            new Draw3dSpin(){{
                suffix="-rotator";
                regionOffset.x = Vars.tilesize / 6f;
                transformation.idt();
                transformation.rotate(Vec3.Y,-75);
                transformation.scale(0.5f,1f,1f);
            }}

            );
            size = 1;
            powerProduction = 0.2f;
        }};
    }
}
