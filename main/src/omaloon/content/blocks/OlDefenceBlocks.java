package omaloon.content.blocks;

import arc.math.*;
import mindustry.type.*;
import mindustry.world.*;
import omaloon.content.*;
import omaloon.gen.*;
import omaloon.world.blocks.defense.*;
import omaloon.world.consumers.*;
import omaloon.world.meta.*;

import static mindustry.type.ItemStack.with;

public class OlDefenceBlocks{
    public static Block
    smallShelter;

    public static void load(){
        smallShelter = new Shelter("small-shelter"){{
            requirements(Category.effect, with(
            OlItems.cobalt, 25,
            OlItems.nickel, 30
            ));
            researchCostMultiplier = 0.3f;
            size = 2;
//            rechargeStandard = 2f;
//            shieldHealth = 260f;
            range = 170f;

            ambientSound = OlSounds.shelter;
            ambientSoundVolume = 0.8f;

            consumePower(0.2f);
            consume(new ConsumeFluid(null, 5f / 60f){{
                continuous = true;
                hasOptimalPressure = true;

                startRange = 15f;
                endRange = 50f;
                efficiencyMultiplier = 2f;
                optimalPressure = 46.5f;

                curve = t -> Math.max(0f, Mathf.slope(t - 0.25f) * 2f - 1f);
            }});

            pressureConfig = new PressureConfig(){{
                acceptsPressure = true;
            }};
        }};
        //endregion
    }
}
