package ol.content.blocks;

import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import ol.content.OlItems;
import ol.pressure.block.PressureController;
import ol.pressure.block.PressurePipe;

public class OlPressureBlocks {
    public static Block controller, pipe, junction, bridge, factoryA, factoryB;

    public static void load() {
        controller = new PressureController("pressure-controller") {{
            requirements(Category.power, ItemStack.with(OlItems.zarini, 25, OlItems.valkon, 10));
            health = 500;
            size = 2;
        }};

        pipe = new PressurePipe("pipe") {{
            requirements(Category.power, BuildVisibility.sandboxOnly, ItemStack.empty);
        }};
    }
}