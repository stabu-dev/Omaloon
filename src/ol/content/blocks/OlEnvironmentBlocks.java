package ol.content.blocks;

import mindustry.content.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;

import ol.content.*;
import ol.graphics.*;

public class OlEnvironmentBlocks {
    public static Block
    //ores
    grumonOre, tungstenOre, omaliteOre,
    //environment
    gravelDalanii, greeniteDalanii, dalanii, deepDalanii,
    gravel, gravelWall,
    coastalGreenite, greenite, greeniteBoulder, greeniteWall;
    //darkGreenite, darkGreeniteWall;
    public static void load() {
        //ores
        grumonOre = new OreBlock("grumon-ore"){{
            oreDefault = true;
            variants = 3;
            oreThreshold = 45F;
            oreScale = 0.3F;
            itemDrop = OlItems.grumon;

            localizedName = itemDrop.localizedName;
            mapColor.set(itemDrop.color);
            useColor = true;
        }};

        tungstenOre = new OreBlock("tungsten-ore"){{
            oreDefault = true;
            variants = 3;
            oreThreshold = 25F;
            oreScale = 0.3F;
            itemDrop = Items.tungsten;

            mapColor.set(itemDrop.color);
            useColor = true;
        }};

        omaliteOre = new OreBlock("omalite-ore"){{
            oreDefault = true;
            variants = 3;
            oreThreshold = 25.4F;
            oreScale = 0.3F;
            itemDrop = OlItems.omalite;

            localizedName = itemDrop.localizedName;
            mapColor.set(itemDrop.color);
            useColor = true;
        }};
        //end ores
        //environment
        deepDalanii = new Floor("deep-dalanii") {{
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

            cacheLayer = OlShaders.dalaniiLayer;
        }};

        dalanii = new Floor("flor-dalanii") {{
            speedMultiplier = 0.5f;
            variants = 0;
            status = OlStatusEffects.slime;
            statusDuration = 6f;
            supportsOverlay = true;

            albedo = 0.9f;
            isLiquid = true;
            liquidDrop = OlLiquids.dalanii;
            liquidMultiplier = 1.5f;

            cacheLayer = OlShaders.dalaniiLayer;
        }};

        gravelDalanii = new Floor("gravel-dalanii") {{
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
            cacheLayer = OlShaders.dalaniiLayer;
        }};

        greeniteDalanii = new Floor("greenite-dalanii"){{
            speedMultiplier = 0.8f;
            variants = 3;

            status = OlStatusEffects.slime;

            statusDuration = 6f;
            supportsOverlay = true;
            albedo = 0.9f;
            isLiquid = true;

            liquidDrop = OlLiquids.dalanii;
            liquidMultiplier = 1.5f;
            cacheLayer = OlShaders.dalaniiLayer;
        }};

        gravel = new Floor("gravel"){{
            variants = 3;
            itemDrop = Items.sand;
            wall = gravelWall;
        }};

        gravelWall = new StaticWall("gravel-wall"){{
            variants = 2;
            itemDrop = Items.sand;
        }};

        coastalGreenite = new Floor("coastal-greenite"){{
            variants = 3;
            wall = greeniteWall;
        }};

        greenite = new Floor("greenite"){{
            variants = 3;
            wall = greeniteWall;
        }};

        greeniteBoulder = new Prop("greenite-boulder"){{
            variants = 3;
            greenite.asFloor().decoration = this;
            coastalGreenite.asFloor().decoration = this;
        }};

        greeniteWall = new StaticWall("greenite-wall"){{
            variants = 2;
        }};

        /*darkGreenite = new Floor("dark-greenite"){{
            variants = 3;
            wall = darkGreeniteWall;
        }};

        darkGreeniteWall = new StaticWall("dark-greenite-wall"){{
            variants = 2;
        }};*/
    }
}