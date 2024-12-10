package omaloon.content.blocks;

import arc.math.*;
import mindustry.entities.effect.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.draw.*;
import omaloon.content.*;
import omaloon.world.blocks.production.*;
import omaloon.world.consumers.*;

import static mindustry.type.ItemStack.*;

public class OlProductionBlocks {
    public static Block
            hammerDrill, pressurizer,

    end;

    public static void load(){
        hammerDrill = new HammerDrill("hammer-drill"){{
            requirements(Category.production, with(
              OlItems.cobalt, 10
            ));
            researchCost = with(
              OlItems.cobalt, 30
            );
            drillTime = 920f;
            tier = 3;
            size = 2;
            shake = 1f;
            drillEffect = new RadialEffect(OlFx.drillHammerHit, 4, 90, 2);

            pressureConfig.linkList.add(this);

            consume(new ConsumePressure(-6, false));
            consume(new PressureEfficiencyRange(-45f, -1f, 2f, true));
        }};

        pressurizer = new Pressurizer("air-well") {{
            requirements(Category.production, with(
              OlItems.cobalt, 10
            ));
            outputPressure = 5f;

            drawer = new DrawMulti(
                new DrawDefault(),
                new DrawTransition() {{
                    suffix = "-cap";
                    interp = a -> Interp.sine.apply(Interp.slope.apply(a));
                }}
            );
        }};
    }
}
