package omaloon.content.blocks;

import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.draw.*;
import omaloon.world.blocks.power.*;
import omaloon.world.draw.*;

import static mindustry.type.ItemStack.*;

public class OlPowerBlocks{
    public static Block
    windTurbine, impulseNode,

    end;

    public static void load(){
        windTurbine = new WindGenerator("wind-turbine"){{
            requirements(Category.power, empty);
            drawer = new DrawMulti(
            new DrawDefault(),
//            new Draw3dSpin(){{
//                suffix = "-rotator";
//                holderSuffix = "-holder";
//                baseOffset.x = Vars.tilesize / 2f;
//                this.<WindGeneratorBuild>rotationProvider(WindGeneratorBuild::baseRotation);
//                axis = Vec3.Y;
//                rotationAroundAxis = -45f;
//                scale.set(0.5f, 1f, 1f);
//            }}
                new Draw3DTurbine("-holder", "-rotator") {{
                    baseRotationProv = build -> ((WindGeneratorBuild) build).baseRotation();
                    rotorRotationProv = build -> ((WindGeneratorBuild) build).baseRotation() * 3f;
                    shadowOffset.set(8f, 8f);
                }}
            );
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
