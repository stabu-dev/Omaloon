package ol.content.blocks;

import arc.graphics.*;

import mindustry.content.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.production.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;

import ol.content.*;
import ol.world.draw.*;

public class OlProductionBlocks {
    public static Block
            zariniBoiler, omaliteMixer;

    public static void load(){
        zariniBoiler = new GenericCrafter("zarini-boiler") {{
            requirements(Category.crafting, ItemStack.empty, BuildVisibility.sandboxOnly.visible());
            size = 3;
            drawer = new DrawMulti(
                    new DrawRegion("-bottom"),
                    new DrawGlowRegion("-bottom-heat") {{
                        color = Color.valueOf("a2e1aa");
                    }},
                    new DrawLiquidTile(OlLiquids.dalanii),
                    new DrawLiquidTile(Liquids.water),
                    new DrawBoiling() {{
                        bubblesColor = Color.valueOf("5e929d");
                        bubblesSize = 0.8f;
                        bubblesAmount = 55;
                    }},
                    new DrawDefault(),
                    new DrawGlowRegion("-heat") {{
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
                    Liquids.water, 14/60f
            );
            outputItem = new ItemStack(
                    OlItems.zarini, 1
            );
            craftTime = 85f;
            ambientSound = OlSounds.boil;
            ambientSoundVolume = 0.7f;
            itemCapacity = 20;
            liquidCapacity = 50;
        }};

        omaliteMixer = new GenericCrafter("omalite-mixer"){{
            requirements(Category.crafting, ItemStack.empty, BuildVisibility.sandboxOnly.visible());
            craftTime = 180f;
            size = 3;
            drawer = new DrawMulti(
                    new DrawRegion("-bottom"),
                    new DrawLiquidTile(Liquids.water),
                    new DrawLiquidTile(OlLiquids.liquidOmalite) {{
                        drawLiquidLight = true;
                    }},
                    new DrawRegion("-rotor"){{
                        spinSprite = true;
                        rotateSpeed = 0.7f;
                    }},
                    new DrawDefault(),
                    new DrawRegion("-top")
            );
            itemCapacity = 35;
            liquidCapacity = 45;
            consumeLiquid(Liquids.water, 20f / 60f);
            consumeItems(new ItemStack(
                    OlItems.omalite, 1
            ));
            outputLiquid = new LiquidStack(
                    OlLiquids.liquidOmalite,  14.5f / 60f
            );
            consumePower(2.4f);
        }};
    }
}
