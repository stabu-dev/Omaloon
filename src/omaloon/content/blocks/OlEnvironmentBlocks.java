package omaloon.content.blocks;

import arc.graphics.*;
import arc.math.geom.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import omaloon.content.*;
import omaloon.graphics.*;
import omaloon.world.blocks.environment.*;

public class OlEnvironmentBlocks{
    public static Block
        //cliff
        cliffUp, cliffHelper,
    //ores
    oreCobalt, oreBeryllium, oreCoal,
    //biomes
    deadShrub, gerbDebris,
        deadGrass,

    frozenSoilWall, frozenSoilBoulder,
        frozenSoil,

    albasterWall, albasterBoulder,
        albaster, albasterTiles, albasterCrater,

    aghaniteWall, aghaniteCrag, aghaniteBoulder,
        smoothAghanite, crackedAghanite, pebbledAghanite, sandyAghanite, aghanite,
    whiteAghaniteWall, whiteAghaniteBoulder,
        smoothWhiteAghanite, weatheredWhiteAghanite, whiteAghanite, whiteAghaniteDust,
    grayAghaniteWall, grayAghaniteCrag, grayAghaniteBoulder,
        grayAghanite, smoothGrayAghanite,
    verdantAghaniteWall, verdantAghaniteBoulder,
        verdantAghanite,

    deepGlacium, glacium, greniteGlacium,

    greniteWall, darkGreniteWall, greniteBoulder,
        grenite, coastalGrenite,

    blueIceWall, weatheredIceWall, blueSnowWall, blueBoulder,
        blueIce, blueIcePieces, blueSnow, blueSnowdrifts, weatheredIce,
    //artificial
    glasmoreMetal, ruinedGerbTiles, ruinedGerbMasonry,
        ruinedGerbWall,
    //dead tree
    fallenDeadTree, fallenDeadTreeTopHalf, fallenDeadTreeBottomHalf,
        spikedTree, bushTree,
        standingDeadTree, deadTreeStump,

    end;

    public static void load(){
        //region cliffs
        cliffUp = new OlCliff("cliff-up");
        cliffHelper = new CliffHelper("cliff-helper");
        //endregion cliffs
        //region ores
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
        //endregion
        //region artificial
        glasmoreMetal = new Floor("glasmore-metal", 6);

        ruinedGerbTiles = new Floor("ruined-gerb-tiles", 3){{
            wall = ruinedGerbWall;
        }};
        ruinedGerbMasonry = new Floor("ruined-gerb-masonry", 3){{
            wall = ruinedGerbWall;
        }};

        ruinedGerbWall = new StaticWall("ruined-gerb-wall"){{
            variants = 4;
        }};

        gerbDebris = new RotatedProp("gerb-debris"){{
            variants = 3;
            breakSound = OlSounds.debrisBreak;
            ruinedGerbTiles.asFloor().decoration = this;
            ruinedGerbMasonry.asFloor().decoration = this;
        }};
        //endregion
        //region albaster
        albaster = new Floor("albaster", 4){{
            wall = albasterWall;
        }};

        albasterTiles = new Floor("albaster-tiles", 3){{
            wall = albasterWall;
        }};

        albasterCrater = new Floor("albaster-craters", 4){{
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
        //endregion
        //region aghanite
        aghaniteWall = new StaticWall("aghanite-wall"){{
            variants = 4;
        }};

        aghaniteCrag = new Prop("aghanite-crag"){{
            customShadow = true;
            variants = 3;
        }};

        smoothAghanite = new Floor("smooth-aghanite", 2){{
            wall = aghaniteWall;
        }};

        crackedAghanite = new Floor("cracked-aghanite", 6){{
            wall = aghaniteWall;
        }};

        pebbledAghanite = new Floor("pebbled-aghanite", 4){{
           wall = aghaniteWall;
        }};

        sandyAghanite = new Floor("sandy-aghanite", 5){{
           wall = aghaniteWall;
        }};

        aghanite = new Floor("aghanite", 3){{
            wall = aghaniteWall;
        }};

        aghaniteBoulder = new Prop("aghanite-boulder"){{
            variants = 3;
            smoothAghanite.asFloor().decoration = this;
            crackedAghanite.asFloor().decoration = this;
            pebbledAghanite.asFloor().decoration = this;
            sandyAghanite.asFloor().decoration = this;
            aghanite.asFloor().decoration = this;
        }};

        grayAghaniteWall = new StaticWall("gray-aghanite-wall"){{
            variants = 2;
        }};

        grayAghaniteCrag = new Prop("gray-aghanite-crag"){{
            customShadow = true;
            variants = 2;
        }};

        grayAghanite = new Floor("gray-aghanite", 3){{
            wall = grayAghaniteWall;
        }};

        smoothGrayAghanite = new Floor("smooth-gray-aghanite", 3){{
            wall = grayAghaniteWall;
        }};

        grayAghaniteBoulder = new Prop("gray-aghanite-boulder"){{
            variants = 3;
            grayAghanite.asFloor().decoration = this;
            smoothGrayAghanite.asFloor().decoration = this;
        }};

        whiteAghaniteWall = new StaticWall("white-aghanite-wall"){{
            variants = 2;
        }};

        smoothWhiteAghanite = new Floor("smooth-white-aghanite", 4){{
            wall = whiteAghaniteWall;
        }};

        weatheredWhiteAghanite = new Floor("weathered-white-aghanite", 2){{
            wall = whiteAghaniteWall;
        }};

        whiteAghanite = new Floor("white-aghanite", 4){{
            wall = whiteAghaniteWall;
        }};

        whiteAghaniteDust = new OverlayFloor("white-aghanite-dust"){{
            variants = 2;
        }};

        whiteAghaniteBoulder = new Prop("white-aghanite-boulder"){{
            customShadow = true;
            variants = 2;
            smoothWhiteAghanite.asFloor().decoration = this;
            weatheredWhiteAghanite.asFloor().decoration = this;
            whiteAghanite.asFloor().decoration = this;
        }};

        verdantAghaniteWall = new StaticWall("verdant-aghanite-wall"){{
            variants = 3;
        }};

        verdantAghanite = new Floor("verdant-aghanite", 3){{
            wall = verdantAghaniteWall;
        }};

        verdantAghaniteBoulder = new Prop("verdant-aghanite-boulder"){{
            customShadow = true;
            variants = 2;
            verdantAghanite.asFloor().decoration = this;
        }};
        //endregion
        //region glacium
        deepGlacium = new Floor("deep-glacium", 0){{
            speedMultiplier = 0.1f;
            liquidDrop = OlLiquids.glacium;
            liquidMultiplier = 1.3f;
            isLiquid = true;
            status = OlStatusEffects.glacied;
            statusDuration = 120f;
            drownTime = 200f;
            cacheLayer = OlShaders.dalaniLayer;
            albedo = 0.9f;
            supportsOverlay = true;
        }};

        glacium = new Floor("shallow-glacium", 0){{
            speedMultiplier = 0.3f;
            status = OlStatusEffects.glacied;
            statusDuration = 90f;
            liquidDrop = OlLiquids.glacium;
            isLiquid = true;
            cacheLayer = OlShaders.dalaniLayer;
            albedo = 0.9f;
            supportsOverlay = true;
        }};

        greniteGlacium = new Floor("grenite-glacium", 3){{
            speedMultiplier = 0.6f;
            status = OlStatusEffects.glacied;
            statusDuration = 60f;
            liquidDrop = OlLiquids.glacium;
            isLiquid = true;
            cacheLayer = OlShaders.dalaniLayer;
            albedo = 0.9f;
            supportsOverlay = true;
        }};
        //endregion
        //region grenite
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
        //endregion
        //region ice snow
        blueIceWall = new StaticWall("blue-ice-wall"){{
            mapColor = Color.valueOf("b3e7fb");
            variants = 2;
        }};

        blueIce = new Floor("blue-ice", 3){{
            mapColor = Color.valueOf("5195ab");
            wall = blueIceWall;
            albedo = 0.9f;
        }};

        blueIcePieces = new OverlayFloor("blue-ice-pieces"){{
            variants = 3;
        }};

        weatheredIceWall = new StaticWall("weathered-ice-wall"){{
            variants = 2;
        }};

        weatheredIce = new OverlayFloor("weathered-ice"){{
            variants = 2;
        }};

        blueSnowWall = new StaticWall("blue-snow-wall"){{
            mapColor = Color.valueOf("d4f2ff");
            variants = 2;
        }};

        blueSnow = new Floor("blue-snow", 3){{
            mapColor = Color.valueOf("9fd3e7");
            wall = blueIceWall;
            albedo = 0.7f;
        }};

        blueSnowdrifts = new OverlayFloor("blue-snowdrifts"){{
            variants = 3;
        }};

        blueBoulder = new Prop("blue-boulder"){{
            variants = 3;
            blueIce.asFloor().decoration = this;
            blueSnow.asFloor().decoration = this;
        }};
        //endregion
        //region frozen soil
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
        //endregion
        //region dead grass
        deadGrass = new Floor("dead-grass", 5){{
            wall = frozenSoilWall;
        }};

        deadShrub = new Prop("dead-shrub"){{
            customShadow = true;
            variants = 3;
            deadGrass.asFloor().decoration = this;
        }};
        //endregion
        //region fallen dead tree
        fallenDeadTree = new CustomShapeProp("fallen-dead-tree"){{
            clipSize = 144f;
            variants = 8;
            canMirror = true;
            spriteOffsets = new Vec2[]{
                new Vec2(-16f, -32f),
                new Vec2(8f, -32f),
                new Vec2(-16, -32f),
                new Vec2(-8f, -32f),

                new Vec2(-8f, -16f),
                new Vec2(-32f, -16f),
                new Vec2(0f, -16f),
                new Vec2(-32f, -16f)
            };
        }};
        fallenDeadTreeTopHalf = new CustomShapeProp("fallen-dead-tree-top-half"){{
            clipSize = 80f;
            variants = 8;
            canMirror = true;
            spriteOffsets = new Vec2[]{
                new Vec2(-8f, -16f),
                new Vec2(-8f, -16f),
                new Vec2(-8f, -16f),
                new Vec2(0f, -16f),

                new Vec2(-16f, -8f),
                new Vec2(-16f, -8f),
                new Vec2(-8f, -8f),
                new Vec2(-16f, -8f)
            };
        }};
        fallenDeadTreeBottomHalf = new CustomShapeProp("fallen-dead-tree-bottom-half"){{
            clipSize = 64f;
            variants = 8;
            canMirror = true;
            spriteOffsets = new Vec2[]{
                new Vec2(-12f, -8f),
                new Vec2(-4f, -8f),
                new Vec2(-12f, -8f),
                new Vec2(-12f, -8f),

                new Vec2(-8f, -12f),
                new Vec2(-8f, -12f),
                new Vec2(0f, -12f),
                new Vec2(-8f, -12f)
            };
        }};

        spikedTree = new Prop("spiked-tree"){{
            variants = 2;
            customShadow = true;
        }};
        bushTree = new RotatedProp("bush-tree"){{
            variants = 0;
            customShadow = true;
            breakSound = Sounds.plantBreak;
        }};

        standingDeadTree = new CustomShapeProp("standing-dead-tree"){{
            clipSize = 32f;
            variants = 1;
            spriteOffsets = new Vec2[]{
                new Vec2(-4f, -12f),
            };
        }};
        deadTreeStump = new CustomShapeProp("dead-tree-stump"){{
            clipSize = 16f;
            variants = 1;
            rotateRegions = drawUnder = true;
            spriteOffsets = new Vec2[]{
                new Vec2(-4f, -4f),
            };
        }};
        //endregion
    }
}
