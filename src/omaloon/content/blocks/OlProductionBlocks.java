package omaloon.content.blocks;

import mindustry.content.Fx;
import mindustry.entities.effect.RadialEffect;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import omaloon.content.OlFx;
import omaloon.world.blocks.production.*;

import static mindustry.type.ItemStack.*;

public class OlProductionBlocks {
    public static Block
            hammerDrill,

    end;

    public static void load(){
        hammerDrill = new HammerDrill("hammer-drill"){{
            requirements(Category.production, BuildVisibility.sandboxOnly, with());
            drillTime = 440f;
            tier = 3;
            size = 2;
            shake = 1f;
            drillEffect = new RadialEffect(OlFx.hammerHit, 4, 90, 2);
        }};
    }
}
