package omaloon.content.blocks;

import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;

import omaloon.world.blocks.distribution.*;

import static mindustry.type.ItemStack.*;

public class OlDistributionBlocks {
    public static Block
            tubeConveyor,

        end;

    public static void load() {
        tubeConveyor = new TubeConveyor("tube-conveyor") {{
            requirements(Category.distribution, BuildVisibility.sandboxOnly, with());
            health = 65;
            speed = 0.03f;
            displayedSpeed = 4.2f;
        }};
    }
}
