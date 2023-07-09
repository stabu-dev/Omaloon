package ol.content.blocks;

import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import ol.world.blocks.pressure.PressureCrafter;
import ol.world.blocks.pressure.PressurePipe;

public class OlPressureBlocks {
    public static Block pipe, junction, bridge, factoryA, factoryB;

    public static void load() {
        factoryA = new PressureCrafter("factory-a") {{
            requirements(Category.power, BuildVisibility.sandboxOnly, ItemStack.empty);
            consumePressureBuffered(1000);
            consumeItem(Items.coal, 2);
            pressureProduce = 100;
            size = 2;
        }};

        factoryB = new PressureCrafter("factory-b") {{
            requirements(Category.power, BuildVisibility.sandboxOnly, ItemStack.empty);
            outputItems = ItemStack.with(Items.copper, 2);
            consumePressure(100, 1000);
            size = 2;
        }};

        pipe = new PressurePipe("pipe") {{
            requirements(Category.power, BuildVisibility.sandboxOnly, ItemStack.empty);
        }};
    }
}