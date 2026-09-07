package omaloon.content.blocks;

import mindustry.content.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.draw.*;
import omaloon.annotations.Annotations.*;
import omaloon.content.*;
import omaloon.gen.*;
import omaloon.world.blocks.production.*;
import omaloon.world.consumers.*;
import omaloon.world.draw.*;
import omaloon.world.meta.*;
import omaloon.world.meta.PressureTank.*;
import omaloon.world.patterns.*;
import omaloon.world.patterns.shape.*;

import static mindustry.type.ItemStack.with;

public class OlCraftingBlocks{
    public static @Merge(base = PressureCrafter.class, value = {PatternTileBlockc.class}) Block compositePress;
    public static Block graphitePress, lenser;

    public static void load(){
        compositePress = new PatternTileBlockPressureCrafter("composite-press"){{
            PatternTileBlockPressureCrafter self = this;

            requirements(Category.crafting, with(
            OlItems.cobalt, 30,
            OlItems.nickel, 30
            ));
            researchCostMultiplier = 0.3f;
            size = 1;

            for(int len : new int[]{2, 3}){
                patterns.addAll(
                new Pattern(name + "-1x" + len, new RectangleShape(len, 1)),
                new Pattern(name + "-" + len + "x1", new RectangleShape(1, len))
                );
            }

            craftTime = 120f;
            craftEffect = OlFx.compositeCraft;

            consumeItems(with(OlItems.nickel, 1, OlItems.cobalt, 1));
            consume(new ConsumeFluid(null, 5){{
                startRange = 5f;
                endRange = 50f;
                efficiencyMultiplier = 1.6f;
                curve = t -> Math.min(
                9f / 2f * (1f - t),
                9f / 7f * t
                );
                optimalPressure = 40f;
                hasOptimalPressure = true;
            }});

            outputItems = with(OlItems.composite, 1);

            pressureConfig = new PressureConfig(){{
                hasPressure = true;
                acceptsPressure = outputsPressure = true;

                fluidCapacity = 16f;

                blockFilter = block -> block != self;

                group = TankGroup.production;
            }};
        }};

        graphitePress = new PressureCrafter("graphite-press"){{
            PressureCrafter self = this;

            requirements(Category.crafting, with(
            OlItems.cobalt, 15,
            OlItems.nickel, 25,
            OlItems.composite, 10
            ));
            size = 2;
            craftTime = 140f;

            craftEffect = Fx.pulverizeMedium;
            consumeItem(Items.coal, 4);
            consume(new ConsumeFluid(null, 10f){{
                startRange = 10f;
                endRange = 50f;
                efficiencyMultiplier = 1.5f;
                curve = t -> Math.min(
                8f * (1f - t),
                8f / 7f * t
                );
                optimalPressure = 45f;
                hasOptimalPressure = true;
            }});

            outputItem = new ItemStack(Items.graphite, 2);

            pressureConfig = new PressureConfig(){{
                hasPressure = true;
                acceptsPressure = outputsPressure = true;

                fluidCapacity = 16f;

                blockFilter = block -> block != self;

                group = TankGroup.production;
            }};
        }};

        lenser = new PressureCrafter("lenser"){{
            requirements(Category.crafting, with());

            size = 2;

            drawer = new DrawMulti(
                new DrawRegion("-bottom"),
                new DrawFluidTile(OlLiquids.tiredGlacium),
                new DrawFluidTile(OlLiquids.glacium),
                new DrawRegion()
            );

            craftTime = 30f;
            consumeItem(OlItems.quartzSand, 2);
            consume(new ConsumeFluid(OlLiquids.glacium, 0.2f) {{
                continuous = true;

                startRange = 10f;
                endRange = 50f;
            }});
            outputItems = with(OlItems.quartzLens, 1);
            outputLiquids = LiquidStack.with(OlLiquids.tiredGlacium, 0.1f);

            pressureConfig = new PressureConfig() {{
                hasPressure = true;
                acceptsPressure = outputsPressure = true;

                fluidCapacity = 16f;
            }};
        }};
    }
}
