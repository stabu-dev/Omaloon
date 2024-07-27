package omaloon.content.blocks;

import mindustry.entities.effect.*;
import mindustry.type.*;
import mindustry.world.*;
import omaloon.content.*;
import omaloon.world.blocks.production.*;
import omaloon.world.consumers.*;

import static mindustry.type.ItemStack.*;

public class OlProductionBlocks {
    public static Block
            hammerDrill,

    end;

    public static void load(){
        hammerDrill = new HammerDrill("hammer-drill"){{
            requirements(Category.production, with(
              OlItems.cobalt, 10
            ));
            drillTime = 920f;
            tier = 3;
            size = 2;
            shake = 1f;
            drillEffect = new RadialEffect(OlFx.drillHammerHit, 4, 90, 2);

            consume(new ConsumePressure(-6, false));
            consume(new PressureEfficiencyRange(-45f, -1f, 2f, true));
        }};
    }
}
