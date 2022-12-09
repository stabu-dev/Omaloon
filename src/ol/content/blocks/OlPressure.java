package ol.content.blocks;

import arc.math.Mathf;
import mindustry.gen.Sounds;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.draw.DrawDefault;
import mindustry.world.draw.DrawFrames;
import mindustry.world.draw.DrawMulti;
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
            //compressors
            mechanicalCompressor,
            sandboxCompressor,
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
        //compressors

        mechanicalCompressor = new PressureCrafter("mechanical-compressor") {{
            requirements(Category.distribution, ItemStack.with());
            drawer = new DrawMulti(
                    new DrawDefault(),
                    new DrawFrames(){{
                        frames = 2;
                        interval = 5f;
                    }}
            );
            ambientSound = Sounds.none;
            squareSprite = false;
            pressureProduce = 5;
            maxPressure = 50;
            tier = 1;
        }};

        sandboxCompressor = new SandboxCompressor("sandbox-compressor") {{
            requirements(Category.distribution, BuildVisibility.sandboxOnly, ItemStack.with());
            maxPressure = 1000;
        }};

        //end compressors
    }
}
