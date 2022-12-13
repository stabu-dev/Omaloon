package ol.content.blocks;

import mindustry.content.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import ol.content.*;
import ol.graphics.*;

public class OlEnvironment {
    public static Block
            oreOmalite,
            gravelDalanii, dalanii, deepDalanii;
    public static void load() {
        //region Ores
        oreOmalite = new OreBlock("omalite-ore"){{
            oreDefault = true;
            variants = 3;
            oreThreshold = 25.4F;
            oreScale = 0.3F;
            itemDrop = OlItems.omalite;
            localizedName = itemDrop.localizedName;
            mapColor.set(itemDrop.color);
            useColor = true;
        }};
        //endregion Ores
        //region Environment
        gravelDalanii = new Floor("gravel-dalanii"){{
            itemDrop = Items.sand;
            playerUnmineable = true;
            speedMultiplier = 0.8f;
            variants = 3;
            status = OlStatusEffects.slime;
            statusDuration = 6f;
            supportsOverlay = true;
            albedo = 0.9f;
            isLiquid = true;
            liquidDrop = OlLiquids.dalanii;
            liquidMultiplier = 1.5f;
            cacheLayer = OlShaders.dalaniteLayer;
        }};
        dalanii = new Floor("flor-dalanii"){{
            speedMultiplier = 0.5f;
            variants = 0;
            status = OlStatusEffects.slime;
            statusDuration = 6f;
            supportsOverlay = true;
            albedo = 0.9f;
            isLiquid = true;
            liquidDrop = OlLiquids.dalanii;
            liquidMultiplier = 1.5f;
            cacheLayer = OlShaders.dalaniteLayer;
        }};
        deepDalanii = new Floor("deep-dalanii"){{
            speedMultiplier = 0.3f;
            variants = 0;
            status = OlStatusEffects.slime;
            statusDuration = 6f;
            supportsOverlay = true;
            drownTime = 210f;
            albedo = 0.9f;
            isLiquid = true;
            liquidDrop = OlLiquids.dalanii;
            liquidMultiplier = 1.5f;
            cacheLayer = OlShaders.dalaniteLayer;
        }};
        //endregion Environment
    }
}
