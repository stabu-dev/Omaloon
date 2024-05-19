package omaloon.content.blocks;

import mindustry.content.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.storage.*;
import mindustry.world.meta.*;
import omaloon.content.*;

import static mindustry.type.ItemStack.*;

public class OlStorageBlocks {
    public static Block
            landingCapsule, coreFloe,
    end;

    public static void load(){
        landingCapsule = new CoreBlock("landing-capsule"){{
            requirements(Category.effect, BuildVisibility.editorOnly, with(OlItems.cobalt, 600, Items.beryllium, 300));

            isFirstTier = true;
            alwaysUnlocked = true;

            size = 2;
            health = 1200;

            itemCapacity = 450;
            unitCapModifier = 6;

            unitType = OlUnitTypes.discovery;
        }};

        coreFloe = new CoreBlock("core-floe"){{
            requirements(Category.effect, empty);
            alwaysUnlocked = true;

            isFirstTier = true;
            unitType = OlUnitTypes.beginner;
            health = 1200;
            itemCapacity = 3500;
            size = 3;

            unitCapModifier = 20;
        }};
    }
}
