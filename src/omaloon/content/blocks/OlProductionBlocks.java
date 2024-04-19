package omaloon.content.blocks;

import mindustry.entities.effect.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import omaloon.content.OlFx;
import omaloon.world.blocks.production.*;
import omaloon.world.consumers.*;

import static mindustry.type.ItemStack.*;

public class OlProductionBlocks {
    public static Block
            hammerDrill,

    end;

    public static void load(){
        hammerDrill = new HammerDrill("hammer-drill"){{
            requirements(Category.production, BuildVisibility.sandboxOnly, with());
            drillTime = 520f;
            tier = 3;
            size = 2;
            shake = 1f;
            drillEffect = new RadialEffect(OlFx.hammerHit, 4, 90, 2);

            consume(new ConsumePressure(-5, false));
            consume(new PressureEfficiencyRange(-100f, -20f, 1.6f, true));
        }};
    }
}
