package ol.content.blocks;

import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.world.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;

import ol.content.OlLiquids;
import ol.world.blocks.power.*;

public class OlPowerBlocks {
    public static Block
            omalitePanel;

    public static void load(){
        omalitePanel = new LiquidSolarPanel("omalite-panel"){{
            requirements(Category.power, ItemStack.empty, BuildVisibility.sandboxOnly.visible());
            size = 4;
            drawer = new DrawMulti(
                    new DrawRegion("-bottom"),
                    new DrawLiquidTile(),
                    new DrawDefault()
            );

            powerProduction = 3.2f;
            liquidCapacity = 56;
            consumeLiquid(OlLiquids.liquidOmalite, 12f / 920f);
        }};
    }
}
