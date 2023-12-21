package omaloon.content.blocks;

import arc.graphics.*;
import mindustry.content.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import omaloon.content.*;

public class OlEnvironmentBlocks {
    public static Block
            //ores
            oreCobalt, oreBeryllium,
            //liquid floors
            deepDalanii, dalanii, greniteDalanii,
            //floors
            albaster, albasterCrater, aghatite, quartzSand, grenite, coastalGrenite, blueIce, blueSnow,
            //walls
            albasterWall, aghatiteWall, quartzSandWall, greniteWall, blueIceWall, blueSnowWall,
            //props
            albasterBoulder, aghatiteBoulder, quartzSandBoulder, greniteBoulder, blueBoulder,

    end;

    public static void load(){
        //ores region
        oreCobalt = new OreBlock("ore-cobalt", OlItems.cobalt){{
            mapColor = Color.valueOf("85939d");
            oreDefault = true;
            oreThreshold = 0.81f;
            oreScale = 23.47619f;
        }};

        oreBeryllium = new OreBlock("ore-beril", Items.beryllium){{
            mapColor = Color.valueOf("3a8f64");
        }};

        //liquid floors region
        deepDalanii = new Floor("deep-dalanii", 0){{
            speedMultiplier = 0.1f;
            mapColor = Color.valueOf("2a4246");
            liquidDrop = OlLiquids.dalani;
            liquidMultiplier = 1.3f;
            isLiquid = true;
            status = OlStatusEffects.dalanied;
            statusDuration = 120f;
            drownTime = 200f;
            cacheLayer = CacheLayer.water;
            albedo = 0.9f;
            supportsOverlay = true;
        }};

        dalanii = new Floor("shallow-dalanii", 0){{
            speedMultiplier = 0.3f;
            mapColor = Color.valueOf("3e6067");
            status = OlStatusEffects.dalanied;
            statusDuration = 90f;
            liquidDrop = Liquids.water;
            isLiquid = true;
            cacheLayer = CacheLayer.water;
            albedo = 0.9f;
            supportsOverlay = true;
        }};

        greniteDalanii = new Floor("grenite-dalanii", 3){{
            speedMultiplier = 0.6f;
            mapColor = Color.valueOf("4d6b6e");
            status = OlStatusEffects.dalanied;
            statusDuration = 60f;
            liquidDrop = Liquids.water;
            isLiquid = true;
            cacheLayer = CacheLayer.water;
            albedo = 0.9f;
            supportsOverlay = true;
        }};
        //end liquid floors region
        //block sets region
        albaster = new Floor("albaster", 3){{
            mapColor = Color.valueOf("4d4d4d");
            wall = albasterWall;
        }};

        albasterCrater = new Floor("albaster-craters", 3){{
            mapColor = Color.valueOf("4d4d4d");
            blendGroup = albaster;
            wall = albasterWall;
        }};

        albasterWall = new StaticWall("albaster-wall"){{
            mapColor = Color.valueOf("7e7e7e");
            variants = 2;
        }};

        albasterBoulder = new Prop("albaster-boulder"){{
            variants = 2;
            albaster.asFloor().decoration = this;
            albasterCrater.asFloor().decoration = this;
        }};

        aghatite = new Floor("aghatite", 3){{
            mapColor = Color.valueOf("2d2524");
            wall = albasterWall;
        }};

        aghatiteWall = new StaticWall("aghatite-wall"){{
            mapColor = Color.valueOf("483f3d");
            variants = 4;
        }};

        aghatiteBoulder = new Prop("aghatite-boulder"){{
            variants = 2;
            aghatite.asFloor().decoration = this;
        }};

        quartzSand = new Floor("quartz-sand-floor", 3){{
            mapColor = Color.valueOf("7d6555");
            wall = albasterWall;
        }};

        quartzSandWall = new StaticWall("quartz-sand-wall"){{
            mapColor = Color.valueOf("baa789");
            variants = 4;
        }};

        quartzSandBoulder = new Prop("quartz-sand-boulder"){{
            variants = 3;
            quartzSand.asFloor().decoration = this;
        }};

        grenite = new Floor("grenite", 4){{
            mapColor = Color.valueOf("414d4b");
            wall = albasterWall;
        }};

        coastalGrenite = new Floor("coastal-grenite", 3){{
            mapColor = Color.valueOf("414d4b");
            wall = albasterWall;
        }};

        greniteWall = new StaticWall("grenite-wall"){{
            mapColor = Color.valueOf("596969");
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
        }};

        blueIceWall = new StaticWall("blue-ice-wall"){{
            mapColor = Color.valueOf("b3e7fb");
            variants = 2;
        }};

        blueSnow = new Floor("blue-snow", 3){{
            mapColor = Color.valueOf("9fd3e7");
            wall = blueIceWall;
        }};

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
