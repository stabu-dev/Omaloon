package omaloon.content.blocks;

import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import omaloon.world.blocks.distribution.*;
import omaloon.world.blocks.liquid.*;
import omaloon.world.meta.*;

import static mindustry.type.ItemStack.*;

public class OlDistributionBlocks {
    public static Block
    //item
    tubeConveyor, tubeDistributor, tubeJunction, tubeSorter, tubeGate, tubeBridge,

    //liquid
    liquidTube, liquidJunction, liquidBridge, liquidPump, liquidValve,

    end;

    public static void load() {
        // region items
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
            speed = 25;
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

        tubeBridge = new TubeItemBridge("tube-bridge-conveyor"){{
            requirements(Category.distribution, BuildVisibility.sandboxOnly, with());
            fadeIn = moveArrows = false;
            range = 4;
            speed = 74f;
            arrowSpacing = 6f;
            bufferCapacity = 14;
        }};
        //endregion

        //region liquids
        liquidTube = new PressureLiquidDuct("liquid-tube") {{
            requirements(Category.liquid, BuildVisibility.sandboxOnly, with());
        }};
        liquidJunction = new PressureLiquidJunction("liquid-junction") {{
            requirements(Category.liquid, BuildVisibility.sandboxOnly, with());
        }};
        liquidBridge = new PressureLiquidBridge("liquid-bridge") {{
            requirements(Category.liquid, BuildVisibility.sandboxOnly, with());
            range = 4;
        }};
        liquidPump = new PressureLiquidPump("liquid-pump") {{
            requirements(Category.liquid, BuildVisibility.sandboxOnly, with());
        }};
        liquidValve = new PressureLiquidValve("liquid-valve") {{
            requirements(Category.liquid, BuildVisibility.sandboxOnly, with());
            pressureConfig = new PressureConfig() {{
                minPressure = -90;
                maxPressure = 90;
            }};
        }};
        //endregion
    }
}
