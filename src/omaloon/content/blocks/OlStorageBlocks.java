package omaloon.content.blocks;

import mindustry.content.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import omaloon.content.*;
import omaloon.world.blocks.storage.*;

import static mindustry.type.ItemStack.*;

public class OlStorageBlocks{
    public static Block
    landingCapsule, coreFloe,
    end;

    public static void load(){
        landingCapsule = new GlassmoreCoreBlock("landing-capsule"){{
            requirements(Category.effect, BuildVisibility.editorOnly, with(
            OlItems.cobalt, 600,
            Items.beryllium, 300, Items.coal, 50
            ));

            isFirstTier = true;
            alwaysUnlocked = true;

            size = 2;
            health = 1200;

            itemCapacity = 450;
            unitCapModifier = 6;

            unitType = OlUnitTypes.discovery;
        }};

        coreFloe = new GlassmoreCoreBlock("core-floe"){{
            requirements(Category.effect, with(
            OlItems.carborundum, 250,
            OlItems.cobalt, 450,
            Items.beryllium, 350
            ));
            researchCost = empty;

            isFirstTier = true;

            unitType = OlUnitTypes.walker;
            health = 1200;
            itemCapacity = 3500;
            size = 3;

            unitCapModifier = 20;
        }};
    }
}
