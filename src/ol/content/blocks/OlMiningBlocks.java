package ol.content.blocks;

import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import ol.world.blocks.OlDrill;
import ol.world.blocks.storage.MiningUnloadPoint;

public class OlMiningBlocks {
    public static Block unitDrill, unloadPoint;

    public static void load() {
        unitDrill = new OlDrill("unit-drill") {{
            tier = 3;
            drillTime = 150;
            size = 1;
            itemCapacity = 20;
            health = 100;
            isUnitDrill = true;
        }};

        unloadPoint = new MiningUnloadPoint("unloading-point") {{
            requirements(Category.effect, BuildVisibility.sandboxOnly, ItemStack.empty);
            health = 100;
            itemCapacity = 20;
        }};
    }
}