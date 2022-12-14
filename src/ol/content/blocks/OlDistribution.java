package ol.content.blocks;

import mindustry.type.*;
import mindustry.world.*;

import ol.world.blocks.pressure.*;

public class OlDistribution {
    public static Block
            //pipes
            pressurePipe,
            improvedPressurePipe,
            reinforcedPressurePipe,
            //counters
            pressureCounter,
            improvedPressureCounter,
            reinforcedPressureCounter,
            //other
            pressureJunction,
            //bridges
            pressureBridge,
            improvedPressureBridge,
            reinforcedPressureBridge,
    end; //end?

    public static void load(){
        //pipes
        pressurePipe = new PressurePipe("pressure-pipe") {{
            requirements(Category.power, ItemStack.with());
            maxPressure = 50;
            tier = 1;
        }};

        improvedPressurePipe = new PressurePipe("improved-pressure-pipe"){{
            requirements(Category.power, ItemStack.with());
            maxPressure = 125;
            tier = 2;
        }};

        reinforcedPressurePipe = new PressurePipe("reinforced-pressure-pipe"){{
            requirements(Category.power, ItemStack.with());
            maxPressure = 240;
            tier = 3;
        }};

        //end pipes
        //counters

        pressureCounter = new PressureCounter("pressure-counter") {{
            requirements(Category.power, ItemStack.with());
            maxPressure = 50;
            dangerPressure = 44;
            tier = 1;
        }};

        improvedPressureCounter = new PressureCounter("improved-pressure-counter"){{
            requirements(Category.power, ItemStack.with());
            maxPressure = 125;
            dangerPressure = 119;
            tier = 2;
        }};

        reinforcedPressureCounter = new PressureCounter("reinforced-pressure-counter"){{
            requirements(Category.power, ItemStack.with());
            maxPressure = 240;
            dangerPressure = 234;
            tier = 3;
        }};

        //end counters
        //other

        pressureJunction = new PressureJunction("pressure-junction") {{
            requirements(Category.power, ItemStack.with());
        }};

        //end other
        //bridges

        pressureBridge = new PressureBridge("pressure-bridge") {{
            requirements(Category.power, ItemStack.with());
            maxPressure = 50;
            range = 40;
            tier = 1;
        }};

        improvedPressureBridge = new PressureBridge("improved-pressure-bridge"){{
            requirements(Category.power, ItemStack.with());
            maxPressure = 125;
            range = 40;
            tier = 2;
        }};

        reinforcedPressureBridge = new PressureBridge("reinforced-pressure-bridge"){{
            requirements(Category.power, ItemStack.with());
            maxPressure = 240;
            range = 40;
            tier = 3;
        }};

        //end bridges
    }
}
