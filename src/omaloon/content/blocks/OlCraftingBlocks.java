package omaloon.content.blocks;

import mindustry.content.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import omaloon.content.*;
import omaloon.world.blocks.production.*;
import omaloon.world.consumers.*;

import static mindustry.type.ItemStack.*;

public class OlCraftingBlocks {
	public static Block carborundumPress;

	public static void load() {
		carborundumPress = new OlGenericCrafter("carborundum-press") {{
			requirements(Category.crafting, BuildVisibility.sandboxOnly, with());
			size = 2;
			craftTime = 60f;

			consumeItems(with(Items.beryllium, 1, OlItems.cobalt, 1));
			consume(new ConsumePressure(10f, false));
			consume(new PressureEfficiencyRange(50f, 100f, 4f, false));

			outputItems = with(OlItems.carborundum, 1);
		}};
	}
}
