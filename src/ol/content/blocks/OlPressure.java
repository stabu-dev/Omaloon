package ol.content.blocks;

import mindustry.content.Items;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.BuildVisibility;
import ol.world.blocks.pressure.*;

public class OlPressure {
    public static Block
            //pipes
            pressurePipe,
            improvedPressurePipe,
            reinforcedPressurePipe,
            //counters
            pressureCounter,
            improvedPressureCounter,
            reinforcedPressureCounter,
            //bridges
            pressureBridge,
            improvedPressureBridge,
            reinforcedPressureBridge,
            //other
            pressureJunction,
            //sandbox
            sandboxCompresor, test,
    end;

    public static void load(){
        //pipes

        pressurePipe = new PressurePipe("pressure-pipe") {{
            requirements(Category.distribution, ItemStack.with());
            junctionReplacement = pressureJunction;
            maxPressure = 50;
            tier = 1;
        }};

        improvedPressurePipe = new PressurePipe("improved-pressure-pipe"){{
            requirements(Category.distribution, ItemStack.with());
            junctionReplacement = pressureJunction;
            maxPressure = 125;
            tier = 2;
        }};

        reinforcedPressurePipe = new PressurePipe("reinforced-pressure-pipe"){{
            requirements(Category.distribution, ItemStack.with());
            junctionReplacement = pressureJunction;
            maxPressure = 240;
            tier = 3;
        }};

        //end pipes
        //counters

        pressureCounter = new PressureCounter("pressure-counter") {{
            requirements(Category.distribution, ItemStack.with());
            maxPressure = 50;
            dangerPressure = 44;
            tier = 1;
        }};

        improvedPressureCounter = new PressureCounter("improved-pressure-counter"){{
            requirements(Category.distribution, ItemStack.with());
            maxPressure = 125;
            dangerPressure = 119;
            tier = 2;
        }};

        reinforcedPressureCounter = new PressureCounter("reinforced-pressure-counter"){{
            requirements(Category.distribution, ItemStack.with());
            maxPressure = 240;
            dangerPressure = 234;
            tier = 3;
        }};

        //end counters
        //bridges

        pressureBridge = new PressureBridge("pressure-bridge") {{
            requirements(Category.distribution, ItemStack.with());
            maxPressure = 50;
            range = 40;
            tier = 1;
        }};

        improvedPressureBridge = new PressureBridge("improved-pressure-bridge"){{
            requirements(Category.distribution, ItemStack.with());
            maxPressure = 125;
            range = 40;
            tier = 2;
        }};

        reinforcedPressureBridge = new PressureBridge("reinforced-pressure-bridge"){{
            requirements(Category.distribution, ItemStack.with());
            maxPressure = 240;
            range = 40;
            tier = 3;
        }};

        //end bridges
        //other

        pressureJunction = new PressureJunction("pressure-junction") {{
            requirements(Category.distribution, ItemStack.with());
        }};

        //end other
        //sandbox

        sandboxCompresor = new SandboxCompresor("sandbox-compresor") {{
            requirements(Category.distribution, BuildVisibility.sandboxOnly, ItemStack.with());
            maxPressure = 1000;
        }};

        //end sandbox
        test = new PressureCrafter("test"){{
            requirements(Category.crafting, BuildVisibility.sandboxOnly, ItemStack.with());
            pressureConsume = 40;
            consumeItem(Items.coal, 2);
            outputItems = ItemStack.with(Items.graphite, 1);
            craftTime = 40f;
            tier = 1;
            size = 2;
            maxPressure = 50;
        }};
    }
}
