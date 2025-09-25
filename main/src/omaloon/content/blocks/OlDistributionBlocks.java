package omaloon.content.blocks;

import mindustry.type.*;
import mindustry.world.*;
import omaloon.content.*;
import omaloon.world.*;
import omaloon.world.blocks.distribution.*;

import static mindustry.type.ItemStack.with;

public class OlDistributionBlocks{
    public static Block
    // items

    // liquids
    liquidTube, liquidJunction, liquidBridge, liquidPump, liquidOutlet;

    public static void load(){
        liquidTube = new PressureLiquidConduit("liquid-tube"){{
            requirements(Category.liquid, with(
            OlItems.cobalt, 2
            ));
            researchCost = with(
            OlItems.cobalt, 10
            );
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
        }};
    }
}
