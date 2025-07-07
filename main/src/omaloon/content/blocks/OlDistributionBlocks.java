package omaloon.content.blocks;

import mindustry.content.*;
import mindustry.type.*;
import mindustry.world.*;
import omaloon.content.*;
import omaloon.world.blocks.distribution.*;

import static mindustry.type.ItemStack.with;

public class OlDistributionBlocks{
    public static Block
    // items

    // liquids
    liquidTube, liquidBridge, liquidPump;

    public static void load(){
        liquidTube = new PressureLiquidConduit("liquid-tube"){{
            requirements(Category.liquid, with(
            OlItems.cobalt, 2
            ));
            researchCost = with(
            OlItems.cobalt, 10
            );
        }};

        liquidBridge = new PressureLiquidBridge("liquid-bridge"){{
            requirements(Category.liquid, with(
            OlItems.cobalt, 2,
            Items.beryllium, 3
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
    }
}
