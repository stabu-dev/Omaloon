package omaloon.content.blocks;

import mindustry.type.*;
import mindustry.world.*;
import omaloon.content.*;
import omaloon.world.blocks.production.*;
import omaloon.world.consumers.*;
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
            craftEffect = OlFx.compositeCraft;

            consumeItems(with(OlItems.nickel, 1, OlItems.cobalt, 1));
            consume(new ConsumeFluid(null, 5){{
                startRange = 5f;
                endRange = 50f;
                efficiencyMultiplier = 1.6f;
                curve = t -> Math.min(
                9f / 2f * (1f - t),
                9f / 7f * t
                );
                optimalPressure = 40f;
                hasOptimalPressure = true;
            }});

            outputItems = with(OlItems.composite, 1);

            pressureConfig = new PressureConfig() {{
                hasPressure = true;

                group = TankGroup.production;
            }};
        }};
    }
}
