package ol.content.blocks;

import mindustry.entities.effect.RadialEffect;
import mindustry.gen.Sounds;
import mindustry.graphics.Layer;
import mindustry.type.*;
import mindustry.world.*;

import mindustry.world.draw.DrawDefault;
import mindustry.world.draw.DrawFrames;
import mindustry.world.draw.DrawMulti;
import mindustry.world.meta.BuildVisibility;
import ol.content.OlFx;
import ol.world.blocks.crafting.PressureCrafter;
import ol.world.blocks.pressure.*;
import ol.world.blocks.sandbox.SandboxCompressor;

import static mindustry.Vars.tilesize;

public class OlPressure {
    public static Block
            //compressors
            mechanicalCompressor,
            electricCompressor,
            //improvedCompressor,
            sandboxCompressor,
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
            reinforcedPressureBridge;

    public static void load(){

        //compressors
        mechanicalCompressor = new PressureCrafter("mechanical-compressor") {{
            requirements(Category.power, ItemStack.with());

            drawer = new DrawMulti(
                    new DrawDefault(),
                    new DrawFrames(){{
                        frames = 3;
                        interval = 5f;
                    }}
            );

            ambientSound = Sounds.none;
            pressureProduce = 5;
            maxPressure = 50;
            showPressure = true;
            hasItems = false;
            hasLiquids= false;
            tier = 1;
        }};

        electricCompressor = new PressureCrafter("electric-compressor") {{
            requirements(Category.power, ItemStack.with());

            craftEffect = new RadialEffect() {{
                effect = OlFx.psh;
                layer = Layer.blockUnder;
                amount = 4;
                lengthOffset = 8;
            }};

            drawer = new DrawMulti(
                    new DrawDefault()
            );

            craftTime = 100f;
            ambientSound = Sounds.none;
            pressureProduce = 15;

            consumePower(2f);

            maxPressure = 50;
            showPressure = true;
            hasItems = false;
            hasLiquids= false;

            size = 2;
            tier = 1;
        }};

        /*
        improvedCompressor = new PressureCrafter("improved-compressor"){{
            requirements(Category.power, ItemStack.with());

            drawer = new DrawMulti(
                    new DrawDefault()
            );

            craftTime = 100f;
            ambientSound = Sounds.none;
            pressureProduce = 25;

            consumePower(2.6f);
            consumeLiquid(OlLiquids.angeirum, 14f/60f);

            maxPressure = 125;
            showPressure = true;

            size = 2;
            tier = 2;
        }};
        */

        sandboxCompressor = new SandboxCompressor("sandbox-compressor") {{
            requirements(Category.power, BuildVisibility.sandboxOnly, ItemStack.with());
            maxPressure = 1000;
        }};

        //end compressors
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
            range = 5.0f*tilesize;
            tier = 1;
        }};

        improvedPressureBridge = new PressureBridge("improved-pressure-bridge"){{
            requirements(Category.power, ItemStack.with());
            maxPressure = 125;
            range = 5.0f*tilesize;
            tier = 2;
        }};

        reinforcedPressureBridge = new PressureBridge("reinforced-pressure-bridge"){{
            requirements(Category.power, ItemStack.with());
            maxPressure = 240;
            range = 5.0f*tilesize;
            tier = 3;
        }};
    }
}
