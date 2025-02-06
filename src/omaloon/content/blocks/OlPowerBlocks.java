package omaloon.content.blocks;

import arc.graphics.*;
import arc.math.geom.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.power.*;
import mindustry.world.draw.*;
import omaloon.content.*;
import omaloon.world.blocks.power.*;
import omaloon.world.draw.*;

import static mindustry.type.ItemStack.with;

public class OlPowerBlocks{
    public static Block
    windTurbine, coalGenerator, impulseNode,

    end;

    public static void load(){
        windTurbine = new WindGenerator("wind-turbine"){{
            requirements(Category.power, with(
            Items.beryllium, 7
            ));
            researchCostMultiplier = 0.5f;
            drawer = new DrawMulti(
            new DrawDefault(),
            new Draw3dSpin("-holder", "-rotator"){{
                baseOffset.x = Vars.tilesize / 2f;
                axis = Vec3.Y;
                rotationProvider(WindGeneratorBuild::baseRotation);
                rotationAroundAxis = -55f;
                rotateSpeed = baseRotateSpeed = 3.3f;
                scale.set(0.5f, 1f, 0f);
            }}
            );
            size = 1;
            powerProduction = 0.2f;
        }};

        coalGenerator = new ConsumeGenerator("coal-generator"){{
            requirements(Category.power, with(
            OlItems.cobalt, 15,
            Items.beryllium, 10, Items.graphite, 5
            ));
            powerProduction = 1f;
            itemDuration = 120f;

            ambientSound = Sounds.smelter;
            ambientSoundVolume = 0.03f;
            effectChance = 0.06f;
            generateEffect = Fx.fireSmoke;

            consumeItem(Items.coal, 1);

            drawer = new DrawMulti(new DrawDefault(),
            new DrawFlame(Color.valueOf("ffcd66")){{
                flameRadius = 2f;
                flameRadiusIn = 1f;
                flameRadiusScl = 4f;
                flameRadiusMag = 1f;
                flameRadiusInMag = 0.5f;
            }}
            );
        }};

        impulseNode = new ImpulseNode("impulse-node"){{
            requirements(Category.power, with(
            Items.beryllium, 5
            ));
            researchCostMultiplier = 0.5f;
            maxNodes = 10;
            laserRange = 6;
        }};
    }
}
