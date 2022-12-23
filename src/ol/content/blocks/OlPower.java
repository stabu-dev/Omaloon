package ol.content.blocks;

import mindustry.content.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.draw.*;

import ol.content.*;
import ol.world.blocks.power.*;

import static mindustry.type.ItemStack.with;

public class OlPower {
    public static Block
            hyperReceiver;

    public static void load() {
        hyperReceiver = new OlPanel("hyper-receiver") {{
            requirements(Category.power, with(
                    Items.titanium, 200,
                    Items.surgeAlloy, 110,
                    OlItems.omaliteAlloy, 40
            ));

            size = 4;
            drawer = new DrawMulti(
                    new DrawRegion("-bottom"),
                    new DrawLiquidTile(),
                    new DrawDefault(),
                    new DrawRegion("-top")
            );

            hasLiquids = true;
            ambientSound = Sounds.none;
            powerProduction = 3.2f;
            liquidCapacity = 56;

            consumeLiquid(OlLiquids.liquidOmalite, 12f / 920f);
        }};
    }
}
