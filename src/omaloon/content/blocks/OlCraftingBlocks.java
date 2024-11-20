package omaloon.content.blocks;

import mindustry.content.*;
import mindustry.type.*;
import mindustry.world.*;
import omaloon.content.*;
import omaloon.world.blocks.production.*;
import omaloon.world.consumers.*;

import static mindustry.type.ItemStack.*;

public class OlCraftingBlocks {
	public static Block carborundumPress, graphitePress;

	public static void load() {
		carborundumPress = new PressureCrafter("carborundum-press") {{
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
			consume(new ConsumePressure(6f, false));
			consume(new PressureEfficiencyRange(5f, 50f, 1.6f, false));

			outputItems = with(OlItems.carborundum, 1);
		}};

		graphitePress = new PressureCrafter("graphite-press") {{
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
			consume(new ConsumePressure(8f, false));
			consume(new PressureEfficiencyRange(10f, 50f, 1.5f, false));

			outputItem = new ItemStack(Items.graphite, 2);
		}};
	}
}
