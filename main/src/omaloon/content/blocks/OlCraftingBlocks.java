package omaloon.content.blocks;

import mindustry.type.*;
import mindustry.world.*;
import omaloon.content.*;
import omaloon.world.blocks.production.*;
import omaloon.world.meta.*;
import omaloon.world.meta.PressureTank.*;

import static mindustry.type.ItemStack.with;

public class OlCraftingBlocks{
    public static Block compositePress;

    public static void load() {
        compositePress = new PressureCrafter("composite-press") {{
            requirements(Category.crafting, with(
                OlItems.cobalt, 30,
                OlItems.nickel, 30
            ));
            researchCostMultiplier = 0.3f;
            size = 2;
            craftTime = 120f;

            pressureConfig = new PressureConfig() {{
                hasPressure = true;

                group = TankGroup.production;
            }};
        }};
    }
}
