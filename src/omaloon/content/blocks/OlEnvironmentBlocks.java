package omaloon.content.blocks;

import arc.graphics.*;
import mindustry.content.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import omaloon.content.*;
import omaloon.graphics.*;
import omaloon.world.blocks.environment.*;

public class OlEnvironmentBlocks {
    public static Block
            //ores
            oreCobalt, oreBeryllium, oreCoal,
            //liquid floors
            deepDalani, dalani, greniteDalani,
            //floors
            deadGrass, frozenSoil, albaster, albasterTiles, albasterCrater, aghatite, aghatitePebbles, quartzSand, grenite, coastalGrenite, blueIce, blueIcePieces, blueSnow, blueSnowdrifts,
            //walls
            deadThickets, frozenSoilWall, albasterWall, aghatiteWall, quartzSandWall, greniteWall, darkGreniteWall, blueIceWall, blueSnowWall,
            //props
            fallenDeadTree, deadShrub, frozenSoilBoulder, albasterBoulder, aghatiteBoulder, quartzSandBoulder, greniteBoulder, blueBoulder,

    end;

    public static void load(){
        //ores region
        oreCobalt = new OreBlock("ore-cobalt", OlItems.cobalt){{
            mapColor = Color.valueOf("85939d");
            oreThreshold = 0.81f;
            oreScale = 23.47619f;
        }};

        oreBeryllium = new OreBlock("ore-beril", Items.beryllium){{
            mapColor = Color.valueOf("3a8f64");
        }};

        oreCoal = new OreBlock("ore-coal", Items.coal){{
            oreThreshold = 0.846f;
            oreScale = 24.428572f;
        }};

        //liquid floors region
        deepDalani = new Floor("deep-dalani", 0){{
            speedMultiplier = 0.1f;
            liquidDrop = OlLiquids.dalani;
            liquidMultiplier = 1.3f;
            isLiquid = true;
            status = OlStatusEffects.dalanied;
            statusDuration = 120f;
            drownTime = 200f;
            cacheLayer = OlShaders.dalaniLayer;
            albedo = 0.9f;
            supportsOverlay = true;
        }};

        dalani = new Floor("shallow-dalani", 0){{
            speedMultiplier = 0.3f;
            status = OlStatusEffects.dalanied;
            statusDuration = 90f;
            liquidDrop = OlLiquids.dalani;
            isLiquid = true;
            cacheLayer = OlShaders.dalaniLayer;
            albedo = 0.9f;
            supportsOverlay = true;
        }};

        greniteDalani = new Floor("grenite-dalani", 3){{
            speedMultiplier = 0.6f;
            status = OlStatusEffects.dalanied;
            statusDuration = 60f;
            liquidDrop = OlLiquids.dalani;
            isLiquid = true;
            cacheLayer = OlShaders.dalaniLayer;
            albedo = 0.9f;
            supportsOverlay = true;
        }};
        //end liquid floors region
        //block sets region
        deadGrass = new Floor("dead-grass", 4){{
            wall = deadThickets;
        }};

        deadThickets = new StaticWall("dead-thickets"){{
            variants = 2;
        }};

        deadShrub = new Prop("dead-shrub"){{
            customShadow = true;
            variants = 3;
            deadGrass.asFloor().decoration = this;
        }};

        fallenDeadTree = new CustomShapeProp("fallen-dead-tree");

        frozenSoil = new Floor("frozen-soil", 4){{
           wall = frozenSoilWall;
        }};

        frozenSoilWall = new StaticWall("frozen-soil-wall"){{
            variants = 4;
        }};

        frozenSoilBoulder = new Prop("frozen-soil-boulder"){{
            variants = 3;
            frozenSoil.asFloor().decoration = this;
        }};

        albaster = new Floor("albaster", 3){{
            wall = albasterWall;
        }};

        albasterTiles = new Floor("albaster-tiles", 3){{
            wall = albasterWall;
        }};

        albasterCrater = new Floor("albaster-craters", 3){{
            blendGroup = albaster;
            wall = albasterWall;
        }};

        albasterWall = new StaticWall("albaster-wall"){{
            variants = 3;
        }};

        albasterBoulder = new Prop("albaster-boulder"){{
            variants = 3;
            albaster.asFloor().decoration = this;
            albasterTiles.asFloor().decoration = this;
            albasterCrater.asFloor().decoration = this;
        }};

        aghatite = new Floor("aghatite", 3){{
            wall = aghatiteWall;
        }};

        aghatitePebbles = new Floor("aghatite-pebbles", 4){{
            wall = aghatiteWall;
        }};

        aghatiteWall = new StaticWall("aghatite-wall"){{
            variants = 4;
        }};

        aghatiteBoulder = new Prop("aghatite-boulder"){{
            variants = 2;
            aghatite.asFloor().decoration = this;
            aghatitePebbles.asFloor().decoration = this;
        }};

        quartzSand = new Floor("quartz-sand-floor", 3){{
            wall = albasterWall;
        }};

        quartzSandWall = new StaticWall("quartz-sand-wall"){{
            variants = 4;
        }};

        quartzSandBoulder = new Prop("quartz-sand-boulder"){{
            variants = 3;
            quartzSand.asFloor().decoration = this;
        }};

        grenite = new Floor("grenite", 4){{
            wall = albasterWall;
        }};

        coastalGrenite = new Floor("coastal-grenite", 3){{
            wall = albasterWall;
        }};

        greniteWall = new StaticWall("grenite-wall"){{
            variants = 2;
        }};

        darkGreniteWall = new StaticWall("dark-grenite-wall"){{
           variants = 2;
        }};

        greniteBoulder = new Prop("grenite-boulder"){{
            variants = 3;
            grenite.asFloor().decoration = this;
            coastalGrenite.asFloor().decoration = this;
        }};

        blueIce = new Floor("blue-ice", 3){{
            mapColor = Color.valueOf("5195ab");
            wall = blueIceWall;
            albedo = 0.9f;
        }};

        blueIcePieces = new OverlayFloor("blue-ice-pieces");

        blueIceWall = new StaticWall("blue-ice-wall"){{
            mapColor = Color.valueOf("b3e7fb");
            variants = 2;
        }};

        blueSnow = new Floor("blue-snow", 3){{
            mapColor = Color.valueOf("9fd3e7");
            wall = blueIceWall;
            albedo = 0.7f;
        }};

        blueSnowdrifts = new OverlayFloor("blue-snowdrifts");

        blueSnowWall = new StaticWall("blue-snow-wall"){{
            mapColor = Color.valueOf("d4f2ff");
            variants = 2;
        }};

        blueBoulder = new Prop("blue-boulder"){{
           variants = 3;
           blueIce.asFloor().decoration = this;
           blueSnow.asFloor().decoration = this;
        }};
        //end
    }
}
