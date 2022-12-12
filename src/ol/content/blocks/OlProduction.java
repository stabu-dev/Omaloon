package ol.content.blocks;

import arc.graphics.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.effect.RadialEffect;
import mindustry.gen.Sounds;
import mindustry.graphics.Layer;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import mindustry.world.meta.BuildVisibility;
import ol.content.*;
import ol.graphics.*;
import ol.world.blocks.crafting.*;
import ol.world.blocks.pressure.PressureCrafter;
import ol.world.blocks.pressure.SandboxCompressor;
import ol.world.draw.*;

import static mindustry.type.ItemStack.*;

public class OlProduction {
    public static Block
            //compressors
            mechanicalCompressor,
            electricCompressor,
            sandboxCompressor,
            //crafters
            multiFactory,
            zariniBoiler,
            valconPress,
            fuser,
            centrifuge,
    end; //end?

    public static void load() {

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

            drawer = new DrawMulti(new DrawDefault());

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

        sandboxCompressor = new SandboxCompressor("sandbox-compressor") {{
            requirements(Category.power, BuildVisibility.sandboxOnly, ItemStack.with());
            maxPressure = 1000;
        }};

        //end compressors
        //crafters

        multiFactory = new MultiCrafter("multi-factory") {{
            requirements(Category.crafting, ItemStack.with(
                    OlItems.grumon, 12,
                    Items.titanium, 11,
                    Items.silicon, 5
            ));

            size = 2;
            itemCapacity = 20;
            liquidCapacity = 20;
            outputsLiquid = true;
            hasItems = true;
            health = 310;

            crafts = crafts.add(
                    //Magnetic Combination Craft
                    new Craft() {{
                        outputItems = ItemStack.with(
                                OlItems.magneticCombination, 1
                        );

                        consumeItems = ItemStack.with(
                                Items.titanium, 1,
                                OlItems.grumon, 1
                        );

                        consumePower = 1.2f;
                        craftTime = 65f;
                        warmupSpeed = 0.02f;
                    }},
                    //Zarini Craft
                    new Craft() {{
                        outputItems = ItemStack.with(
                                OlItems.zarini, 1
                        );

                        outputLiquids = LiquidStack.with(
                                Liquids.water, 8/60f
                        );

                        consumeItems = ItemStack.with(
                                OlItems.grumon, 1
                        );

                        consumeLiquids = LiquidStack.with(
                                OlLiquids.dalanii, 12/60f
                        );

                        consumePower = 1.1f;
                        craftTime = 75f;
                        warmupSpeed = 0.2f;
                    }},
                    //Valcon Craft
                    new Craft() {{
                        outputItems = ItemStack.with(
                                OlItems.valkon, 1
                        );

                        consumeItems = ItemStack.with(
                                Items.tungsten, 1,
                                OlItems.zarini, 1
                        );

                        consumePower = 1.6f;
                        craftTime = 82f;
                        warmupSpeed = 0.2f;
                    }}
            );
        }};

        zariniBoiler = new GenericCrafter("zarini-boiler") {{
            requirements(Category.crafting, with(
                    Items.surgeAlloy, 20,
                    OlItems.omalite, 50,
                    Items.titanium, 80,
                    Items.thorium, 65
            ));

            size = 3;
            drawer = new DrawMulti(
                    new DrawRegion("-bottom"),
                    new DrawLiquidTile(OlLiquids.dalanii),
                    new DrawLiquidTile(Liquids.water),

                    new DrawBoiling() {{
                        bubblesColor = Color.valueOf("5e929d");
                        bubblesSize = 0.8f;
                        bubblesAmount = 55;
                    }},

                    new DrawDefault(),
                    new DrawGlowRegion("-light") {{
                        color = Color.valueOf("a2e1aa");
                    }}
            );

            consumePower(4.6f);
            consumeItems(new ItemStack(
                    OlItems.grumon, 2
            ));

            consumeLiquids(new LiquidStack(
                    OlLiquids.dalanii, 30/60f
            ));

            outputLiquid = new LiquidStack(
                    Liquids.water, 17/60f
            );

            outputItem = new ItemStack(
                    OlItems.zarini, 2
            );

            craftTime = 170f;
            ambientSound = OlSounds.boiler;

            ambientSoundVolume = 1f;
            itemCapacity = 10;
            liquidCapacity = 50;

            hasPower = hasLiquids = hasItems = true;
        }};

        valconPress = new PressureCrafter("valcon-press") {{
            requirements(Category.crafting, with(
                    Items.surgeAlloy, 20,
                    OlItems.omalite, 50,
                    Items.titanium, 80,
                    Items.thorium, 65
            ));

            size = 3;
            tier = 2;
            maxPressure = 125;
            craftTime = 60f;
            pressureConsume = 110;

            downPressure = true;
            downPercent = 0.15f;

            consumeItems(with(
                    Items.tungsten, 1,
                    OlItems.zarini, 1
            ));

            outputItem = new ItemStack(
                    OlItems.valkon, 2
            );
        }};

        fuser = new GenericCrafter("fuser") {{
            requirements(Category.crafting, with(
                    Items.surgeAlloy, 20,
                    OlItems.omalite, 50,
                    Items.titanium, 80,
                    Items.thorium, 65
            ));

            craftTime = 185f;
            size = 3;

            drawer = new DrawMulti(
                    new DrawRegion("-bottom"),
                    new DrawLiquidTile(Liquids.water),

                    new DrawLiquidTile(OlLiquids.liquidOmalite) {{
                        drawLiquidLight = true;
                    }},

                    new DrawRegion("-rotator"){{
                        spinSprite = true;
                        rotateSpeed = 1f;
                    }},

                    new DrawDefault(),
                    new DrawRegion("-top")
            );

            itemCapacity = 35;
            liquidCapacity = 45;
            hasPower = hasLiquids = hasItems = true;

            consumeLiquid(Liquids.water, 22f / 60f);

            consumeItems(new ItemStack(
                    OlItems.omalite, 2
            ));

            outputLiquid = new LiquidStack(
                    OlLiquids.liquidOmalite,  19.5f / 60f
            );

            consumePower(2.4f);
        }};

        /*centrifuge = new OlImpactCrafter("centrifuge") {{
            size = 4;
            health = 540;
            requirements(Category.crafting, ItemStack.with(OlItems.omalite, 80, Items.thorium, 80, Items.titanium, 100));
            craftTime = 270f;
            craftEffect = Fx.shieldBreak;
            updateEffectChance = 0.08f;
            ambientSound = OlSounds.centrifuge;
            ambientSoundVolume = 0.5f;
            accelerationSpeed = 0.0003f;
            decelerationSpeed = 0.006125f;
            powerProduction = 22f;
            drawer = new DrawMulti(
                    new DrawRegion("-bottom"),
                    new DrawCentryfuge(){{
                        plasma1 = Items.titanium.color;
                        plasma2 = OlPal.oLDarkBlue;
                    }}
            );
            onCraft = tile -> {
                Tmp.v1.setToRandomDirection().setLength(27f / 3.4f);
                Fx.pulverize.at(tile.x + Tmp.v1.x, tile.y + Tmp.v1.y);
                Fx.hitLancer.at(tile.x + Tmp.v1.x, tile.y + Tmp.v1.y);
            };
            consumePower(7);
            consumeItems(with(Items.titanium, 4, OlItems.omalite, 2));
            consumeLiquid(OlLiquids.liquidOmalite, 0.18f);
            outputItems = with(OlItems.omaliteAlloy, 5);
            itemCapacity = 30;
        }};*/
        //end crafters
    }
}
