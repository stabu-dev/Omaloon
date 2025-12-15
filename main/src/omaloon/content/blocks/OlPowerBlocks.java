package omaloon.content.blocks;

import arc.graphics.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.power.*;
import mindustry.world.draw.*;
import omaloon.content.*;
import omaloon.world.blocks.power.*;
import omaloon.world.consumers.*;
import omaloon.world.draw.*;

import static mindustry.type.ItemStack.with;

public class OlPowerBlocks{
    public static Block
    windTurbine,
    coalGenerator,
    impulseNode;

    public static void load(){
        windTurbine = new AreaGenerator("wind-turbine"){{
            requirements(Category.power, with(
            OlItems.nickel, 7
            ));
            researchCostMultiplier = 0.5f;
            drawer = new DrawMulti(
            new DrawDefault(),
            new DrawWindTurbine(){{
                rotateSpeed = 1.4f;
            }}
            );
            distance = 11;
            powerProduction = 0.2f;

            consume(new ConsumeWeather(){{
                multipliers.put(OlWeathers.hailStorm, 0.25f);
            }}).boost();
        }};

        coalGenerator = new ConsumeGenerator("coal-generator"){{
            requirements(Category.power, with(
            OlItems.cobalt, 15,
            OlItems.nickel, 10, Items.graphite, 5
            ));
            powerProduction = 1f;
            itemDuration = 120f;

            ambientSound = Sounds.smelter;
            ambientSoundVolume = 0.03f;
            effectChance = 0.06f;
            generateEffect = Fx.fireSmoke;

            consumeItem(Items.coal, 1);

            drawer = new DrawMulti(
            new DrawDefault(),
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
            OlItems.nickel, 5
            ));
            researchCostMultiplier = 0.5f;
            maxNodes = 10;
            laserRange = 6;
        }};
    }
}
