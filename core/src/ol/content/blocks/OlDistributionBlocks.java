package ol.content.blocks;

import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.meta.BuildVisibility;
import ol.world.blocks.distribution.TubeConveyor;

import static mindustry.type.ItemStack.with;

public class OlDistributionBlocks {
    public static Block tubeConveyor;

    public static void load() {
        tubeConveyor = new TubeConveyor("tube-conveyor") {{
            requirements(Category.distribution, BuildVisibility.sandboxOnly, with());
            health = 65;
            speed = 0.03f;
            displayedSpeed = 4.2f;
        }};
    }
}