package omaloon.content.blocks;

import mindustry.content.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import omaloon.content.*;
import omaloon.world.blocks.storage.*;

import static mindustry.type.ItemStack.with;

public class OlStorageBlocks{
    public static Block landingCapsule;

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
    }
}
