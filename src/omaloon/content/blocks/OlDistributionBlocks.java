package omaloon.content.blocks;

import mindustry.content.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.draw.*;
import omaloon.content.*;
import omaloon.world.blocks.distribution.*;
import omaloon.world.blocks.liquid.*;

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
            requirements(Category.distribution, with(
              OlItems.cobalt, 1
            ));
            health = 65;
            speed = 0.03f;
            displayedSpeed = 4.2f;
        }};

        tubeDistributor = new TubeDistributor("tube-distributor"){{
            requirements(Category.distribution, with(
              OlItems.cobalt, 3
            ));
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
            requirements(Category.distribution, with(
              OlItems.cobalt, 3
            ));
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
            requirements(Category.distribution, with(
              OlItems.cobalt, 3,
              Items.beryllium, 2
            ));
            health = 65;
        }};

        tubeGate = new TubeGate("tube-gate") {{
            requirements(Category.distribution, with(
              OlItems.cobalt, 3,
              Items.beryllium, 2
            ));
            health = 65;
        }};

        tubeBridge = new TubeItemBridge("tube-bridge-conveyor"){{
            requirements(Category.distribution, with(
              OlItems.cobalt, 3,
              Items.beryllium, 2
            ));
            fadeIn = moveArrows = false;
            range = 4;
            speed = 74f;
            arrowSpacing = 6f;
            bufferCapacity = 14;
        }};
        //endregion

        //region liquids
        liquidTube = new PressureLiquidConduit("liquid-tube") {{
            requirements(Category.liquid, with(
              OlItems.cobalt, 2
            ));
        }};

        liquidJunction = new PressureLiquidJunction("liquid-junction") {{
            requirements(Category.liquid, with(
              OlItems.cobalt, 5
            ));
        }};

        liquidBridge = new PressureLiquidBridge("liquid-bridge") {{
            requirements(Category.liquid, with(
              OlItems.cobalt, 2,
              Items.beryllium, 3
            ));
            range = 4;
        }};

        liquidPump = new PressureLiquidPump("liquid-pump") {{
            requirements(Category.liquid, with(
              OlItems.cobalt, 4
            ));
            pressureTransfer = 0.1f;
            pressureDifference = 5f;
        }};

        liquidValve = new PressureLiquidValve("liquid-valve") {{
            requirements(Category.liquid, with(
              OlItems.cobalt, 2,
              Items.beryllium, 2
            ));
            pressureLoss = 0.3f;
        }};
        //endregion
    }
}
