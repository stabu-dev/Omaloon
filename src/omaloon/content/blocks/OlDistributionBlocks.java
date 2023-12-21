package omaloon.content.blocks;

import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;

import omaloon.world.blocks.distribution.*;

import static mindustry.type.ItemStack.*;

public class OlDistributionBlocks {
    public static Block
            tubeConveyor, tubeDistributor, tubeJunction, tubeSorter, tubeGate,

        end;

    public static void load() {
        tubeConveyor = new TubeConveyor("tube-conveyor") {{
            requirements(Category.distribution, BuildVisibility.sandboxOnly, with());
            health = 65;
            speed = 0.03f;
            displayedSpeed = 4.2f;
        }};

        tubeDistributor = new TubeDistributor("tube-distributor"){{
            requirements(Category.distribution, BuildVisibility.sandboxOnly, with());
            speed = 10f;
            buildCostMultiplier = 4f;
            health = 65;
            drawer = new DrawMulti(
                    new DrawRegion("-bottom"){{
                        layer = Layer.blockUnder;
                    }}
            );
        }};

        tubeJunction = new TubeJunction("tube-junction"){{
            requirements(Category.distribution, BuildVisibility.sandboxOnly, with());
            speed = 24;
            capacity = 4;
            health = 65;
            drawer = new DrawMulti(
                    new DrawRegion("-bottom"){{
                        layer = Layer.blockUnder;
                    }},
                    new DrawDefault()
            );
        }};

        tubeSorter = new TubeSorter("tube-sorter"){{
            requirements(Category.distribution, BuildVisibility.sandboxOnly, with());
            health = 65;
        }};

        tubeGate = new TubeGate("tube-gate") {{
            requirements(Category.distribution, BuildVisibility.sandboxOnly, with());
            health = 65;
        }};
    }
}
