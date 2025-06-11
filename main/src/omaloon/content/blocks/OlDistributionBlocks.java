package omaloon.content.blocks;

import mindustry.type.*;
import mindustry.world.*;
import omaloon.content.*;
import omaloon.world.blocks.distribution.*;

import static mindustry.type.ItemStack.with;

public class OlDistributionBlocks{
    public static Block
    // items

    // liquids
    liquidTube;

    public static void load(){
        liquidTube = new PressureLiquidConduit("liquid-tube"){{
            requirements(Category.liquid, with(
            OlItems.cobalt, 2
            ));
            researchCost = with(
            OlItems.cobalt, 10
            );
        }};
    }
}
