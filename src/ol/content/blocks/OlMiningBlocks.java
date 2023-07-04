package ol.content.blocks;

import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import ol.world.blocks.OlDrill;
import ol.world.blocks.storage.UnloadPoint;

public class OlMiningBlocks {
    public static Block unitDrill, unloadPoint;

    public static void load() {
        unitDrill = new OlDrill("unit-drill") {{
            tier = 3;
            drillTime = 150;
            size = 1;
            itemCapacity = 10;
            health = 100;
            isUnitDrill = true;
        }};

        unloadPoint = new UnloadPoint("unloading-point") {{
            requirements(Category.production, BuildVisibility.sandboxOnly, ItemStack.empty);
            range = 8;
            health = 100;
            itemCapacity = 2;
        }};
    }
}