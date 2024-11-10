package omaloon.content.blocks;

import mindustry.content.*;
import mindustry.type.*;
import mindustry.world.*;
import omaloon.content.*;
import omaloon.world.blocks.production.*;
import omaloon.world.consumers.*;

import static mindustry.type.ItemStack.*;

public class OlCraftingBlocks {
	public static Block carborundumPress;

	public static void load() {
		carborundumPress = new PressureCrafter("carborundum-press") {{
			requirements(Category.crafting, with(
				OlItems.cobalt, 30,
				Items.beryllium, 30
			));
			size = 2;
			craftTime = 120f;
			outputsLiquid = true;

			craftEffect = OlFx.carborundumCraft;

			consumeItems(with(Items.beryllium, 1, OlItems.cobalt, 1));
			consume(new ConsumePressure(10f, false));
			consume(new PressureEfficiencyRange(20f, 100f, 1.6f, false));

			outputItems = with(OlItems.carborundum, 1);
		}};
	}
}
