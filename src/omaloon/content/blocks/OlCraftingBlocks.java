package omaloon.content.blocks;

import mindustry.content.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import omaloon.content.*;
import omaloon.world.blocks.production.*;
import omaloon.world.consumers.*;

import static mindustry.type.ItemStack.with;

public class OlCraftingBlocks{
    public static Block carborundumPress, graphitePress, glaciumBoiler;

    public static void load(){
        carborundumPress = new PressureCrafter("carborundum-press"){{
            requirements(Category.crafting, with(
            OlItems.cobalt, 30,
            Items.beryllium, 30
            ));
            researchCostMultiplier = 0.3f;
            size = 2;
            craftTime = 120f;
            outputsLiquid = true;

            craftEffect = OlFx.carborundumCraft;

            consumeItems(with(Items.beryllium, 1, OlItems.cobalt, 1));
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

            outputItems = with(OlItems.carborundum, 1);
        }};

        graphitePress = new PressureCrafter("graphite-press"){{
            requirements(Category.crafting, with(
            OlItems.cobalt, 15,
            Items.beryllium, 25,
            OlItems.carborundum, 2
            ));
            size = 2;
            craftTime = 140f;
            outputsLiquid = true;

            craftEffect = Fx.pulverizeMedium;
            consumeItem(Items.coal, 4);
            consume(new ConsumeFluid(null, 8f){{
                startRange = 10f;
                endRange = 50f;
                efficiencyMultiplier = 1.5f;
                curve = t -> Math.min(
                8f * (1f - t),
                8f / 7f * t
                );
                optimalPressure = 45f;
                hasOptimalPressure = true;
            }});

            outputItem = new ItemStack(Items.graphite, 2);
        }};

        glaciumBoiler = new PressureCrafter("glacium-boiler"){{
            requirements(Category.crafting, BuildVisibility.sandboxOnly, with());
            size = 2;

            useVanillaLiquids = false;
            ignoreLiquidFullness = true;
            dumpExtraLiquid = true;

            craftTime = 120f;

            consumeItem(Items.coal, 1);
            consume(new ConsumeFluid(OlLiquids.glacium, 1f / 60f){{
                continuous = true;
            }});

            outputAir = 4f / 6f;
        }};
    }
}
