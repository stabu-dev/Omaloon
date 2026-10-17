package omaloon.content.blocks;

import mindustry.content.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.storage.*;
import mindustry.world.meta.*;
import omaloon.annotations.Annotations.*;
import omaloon.content.*;
import omaloon.gen.*;
import omaloon.world.blocks.storage.*;
import omaloon.world.patterns.*;
import omaloon.world.patterns.shape.*;

import static mindustry.type.ItemStack.with;

public class OlStorageBlocks{
    //core
    public static Block landingCapsule, coreFloe;
    //storage
    public static @Merge(base = StorageBlock.class, value = {PatternTileBlockc.class}) Block storageContainer;

    public static void load(){
        //core
        landingCapsule = new GlassmoreCoreBlock("landing-capsule"){{
            requirements(Category.effect, BuildVisibility.editorOnly, with(
            OlItems.cobalt, 600,
            OlItems.nickel, 300, Items.coal, 50
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
            OlItems.composite, 250,
            OlItems.cobalt, 450, OlItems.nickel, 350
            ));
            researchCost = with(
            OlItems.composite, 75,
            OlItems.cobalt, 100, OlItems.nickel, 100
            );
            isFirstTier = true;
            unitType = OlUnitTypes.walker;
            health = 1200;
            itemCapacity = 3500;
            size = 3;
            unitCapModifier = 20;
        }};
        //storage
        storageContainer = new PatternTileBlockStorageBlock("storage-container"){{
            requirements(Category.effect, with(
            OlItems.cobalt, 25,
            OlItems.nickel, 25
            ));
            researchCostMultiplier = 0.3f;

            size = 1;
            itemCapacity = 50;

            for(int w = 1; w <= 4; w++){
                for(int h = 1; h <= 4; h++){
                    int area = w * h;
                    if(area == 1 || area > 8) continue;

                    patterns.add(new Pattern(name + "-" + w + "x" + h, new RectangleShape(w, h), new Patch(){{
                        itemCapacity = area * 80 - 40;
                    }}));
                }
            }

            usePatternIcon = true;
        }};
    }
}
