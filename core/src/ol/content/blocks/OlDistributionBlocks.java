package ol.content.blocks;

import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.meta.*;

import ol.world.blocks.distribution.*;

import static mindustry.type.ItemStack.with;

public class OlDistributionBlocks {
    public static Block tubeConveyor, tubeJunction, tubeSorter;

    public static void load() {
        tubeConveyor = new TubeConveyor("tube-conveyor") {{
            requirements(Category.distribution, BuildVisibility.sandboxOnly, with());
            health = 65;
            speed = 0.03f;
            displayedSpeed = 4.2f;
        }};

        tubeJunction = new Junction("tube-junction"){{
            requirements(Category.distribution, BuildVisibility.sandboxOnly, with());
            speed = 26;
            itemCapacity = 6;
            health = 65;
        }};

        tubeSorter = new TubeSorter("tube-sorter") {{
            requirements(Category.distribution, BuildVisibility.sandboxOnly, with());
            health = 65;
        }};
    }
}