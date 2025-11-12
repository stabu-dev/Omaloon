package omaloon.content.blocks;

import mindustry.type.*;
import mindustry.world.*;
import omaloon.content.*;
import omaloon.world.blocks.distribution.*;
import omaloon.world.meta.*;

import static mindustry.type.ItemStack.*;

public class OlDistributionBlocks{
    public static Block
    // items
    tubeConveyor,

    // liquids
    liquidTube, liquidJunction, liquidBridge, liquidPump, liquidOutlet;

    public static void load(){
        //region items
        tubeConveyor = new TubeConveyor("tube-conveyor"){{
            requirements(Category.distribution, with(
            OlItems.cobalt, 1
            ));
            researchCost = empty;
            health = 65;
            speed = 0.03f;
            displayedSpeed = 4.2f;
        }};
        //endregion

        //region liquids
        liquidTube = new PressureLiquidConduit("liquid-tube"){{
            requirements(Category.liquid, with(
            OlItems.cobalt, 2
            ));
            researchCost = with(
            OlItems.cobalt, 10
            );

            pressureConfig = new PressureConfig(){{
                // TODO generify this for transportation blocks or disable it on non transportation blocks?
                fluidReacts = true;
            }};
        }};

        liquidJunction = new PressureLiquidJunction("liquid-junction"){{
            requirements(Category.liquid, with(
            OlItems.cobalt, 5
            ));
            researchCostMultiplier = 0.3f;
        }};

        liquidBridge = new PressureLiquidBridge("liquid-bridge"){{
            requirements(Category.liquid, with(
            OlItems.cobalt, 2,
            OlItems.nickel, 3
            ));
            range = 32f;

            pressureConfig = new PressureConfig(){{
                fluidReacts = true;
            }};
        }};

        liquidPump = new PressureLiquidPump("liquid-pump"){{
            requirements(Category.liquid, with(
            OlItems.cobalt, 4
            ));
            researchCost = with(
            OlItems.cobalt, 25
            );
        }};

        liquidOutlet = new PressureLiquidOutlet("liquid-outlet"){{
            requirements(Category.liquid, with(
            OlItems.cobalt, 2,
            OlItems.nickel, 1
            ));

            pressureConfig = new PressureConfig(){{
                fluidReacts = true;
            }};
        }};
        //endregion
    }
}
