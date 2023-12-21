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
    windTurbine, impulseNode,

    end;

    public static void load(){
        windTurbine = new WindGenerator("wind-turbine"){{
            requirements(Category.power, empty);
            drawer = new DrawMulti(
            new DrawDefault(),
            new Draw3dSpin(){{
                suffix = "-rotator";
                holderSuffix = "-holder";
                baseOffset.x = Vars.tilesize / 2f;
                this.<WindGeneratorBuild>rotationProvider(WindGeneratorBuild::baseRotation);
                axis = Vec3.Y;
                rotationAroundAxis = -45f;
                scale.set(0.5f, 1f, 1f);
            }});
            size = 1;
            powerProduction = 0.2f;
        }};

        impulseNode = new ImpulseNode("impulse-node"){{
            requirements(Category.power, empty);
            maxNodes = 10;
            laserRange = 6;
        }};
    }
}
