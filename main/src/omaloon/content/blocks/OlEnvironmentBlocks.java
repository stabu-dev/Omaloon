package omaloon.content.blocks;

import arc.graphics.*;
import arc.math.geom.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.blocks.environment.*;
import omaloon.content.*;
import omaloon.gen.*;
import omaloon.graphics.*;
import omaloon.type.shape.*;
import omaloon.world.blocks.environment.*;
import omaloon.world.blocks.environment.OlCliff;
import omaloon.world.blocks.environment.customsshapeproop.*;
import omaloon.world.patterns.*;

public class OlEnvironmentBlocks{
    public static Block
    // Cliff
    cliff,

    // Ores
    oreCobalt, oreNickel, oreCoal, magnetiteCrystals,

    // Artificial
    glasmoreMetal, glasmoreMetalPiping, /*glasmoreMetalPlus, glasmoreMetalDonut,*/

    // ---Biomes---
    // Dead forrest
    gerbRemains,
    ruinedTiles, ruinedMasonry, ruinedWall,

    deadShrub, deadSpikeTree, deadBushTree,
    fallenDeadTree, fallenDeadTreeTopHalf, fallenDeadTreeBottomHalf,
    deadTree, deadStump,

    deadGrass,

    frozenSoil, frozenSoilWall, frozenSoilBoulder,

    // Albaster
    alabaster, alabasterTiles, alabasterCrater, alabasterCraterLarge,
    alabasterWall, alabasterBoulder,

    // Aghanite
    aghaniteWall, aghaniteCrag, aghaniteBoulder,

    smoothAghanite, crackedAghanite, pebbledAghanite, sandyAghanite, aghanite,
    whiteAghaniteWall, whiteAghaniteBoulder,

    smoothWhiteAghanite, weatheredWhiteAghanite, whiteAghanite, whiteAghaniteDust,

    grayAghanite, smoothGrayAghanite,
    grayAghaniteWall, grayAghaniteCrag, grayAghaniteBoulder,

    verdantAghanite,
    verdantAghaniteWall, verdantAghaniteBoulder,

    // Glacium Crater
    deepGlacium, glacium, greniteGlacium,

    grenite, coastalGrenite,
    greniteWall, darkGreniteWall, greniteBoulder,

    // Ice & Snow
    blueIce, blueIcePieces, weatheredIce,
    blueIceWall, weatheredIceWall,

    blueSnow, blueSnowdrifts,
    blueSnowWall, blueBoulder;
    // ---End---

    public static void load(){
        // Cliff
        cliff = new OlCliff("cliff");

        // Ores
        oreCobalt = new OreBlock("ore-cobalt", OlItems.cobalt){{
            mapColor = Color.valueOf("85939d");
            oreThreshold = 0.81f;
            oreScale = 23.47619f;
        }};
        oreNickel = new OreBlock("ore-nickel", OlItems.nickel){{
            mapColor = Color.valueOf("3a8f64");
        }};
        oreCoal = new OreBlock("ore-coal", Items.coal){{
            oreThreshold = 0.846f;
            oreScale = 24.428572f;
        }};

        magnetiteCrystals = new PatternOreBlock("magnetite-crystals", OlItems.magnetite){{
            pattern = new Pattern("omaloon-magnetite-large-crystals"){{
                shape = new RectanglePatternShape(2, 2);
                variants = 3;
            }};
            variants = 2;
            drawParentUnder = true;
            isPattern = true;
        }};

        // Artificial
        glasmoreMetal = new PatternFloor("glasmore-metal", 6){{
            pattern = new Pattern("omaloon-glasmore-metal-don"){{
                shape = new CustomPatternShape("omaloon-glasmore-metal-don-mask");
            }};
        }};

        glasmoreMetalPiping = new Floor("glasmore-metal-piping"){{
           autotile = true;
           drawEdgeOut = false;
           drawEdgeIn = false;
        }};
        // TODO: glasmore-metal-plus (MultiPattern)
        /*glasmoreMetalPlus = new PatternFloor("glasmore-metal", 6){{
            pattern = new Pattern("omaloon-glasmore-metal-plus"){{
                shape = new CustomPatternShape("omaloon-glasmore-metal-plus-mask");
                variants = 2;
            }};
            drawPatternEdges = true;
            blendGroup = this;
        }};*/

        // ---Biomes---
        // Dead forrest
        ruinedTiles = new Floor("ruined-tiles", 3){{
            wall = ruinedWall;
        }};
        ruinedMasonry = new Floor("ruined-masonry", 3){{
            wall = ruinedWall;
        }};
        ruinedWall = new StaticWall("ruined-wall"){{
            variants = 4;
        }};

        gerbRemains = new RotatedProp("gerb-remains"){{
            variants = 3;
            breakSound = OlSounds.debrisBreak;
            ruinedTiles.asFloor().decoration = this;
            ruinedMasonry.asFloor().decoration = this;
        }};

        deadGrass = new Floor("dead-grass", 5){{
            wall = frozenSoilWall;
        }};

        deadShrub = new Prop("dead-shrub"){{
            customShadow = true;
            variants = 3;
            deadGrass.asFloor().decoration = this;
        }};
        deadSpikeTree = new Prop("dead-spike-tree"){{
            variants = 2;
            customShadow = true;
        }};
        deadBushTree = new RotatedProp("dead-bush-tree"){{
            variants = 1;
            customShadow = true;
            breakSound = Sounds.plantBreak;
        }};

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

        deadTree = new CustomShapeProp("dead-tree"){{
            clipSize = 32f;
            variants = 1;
            spriteOffsets = new Vec2[]{
            new Vec2(-4f, -12f),
            };
        }};
        deadStump = new CustomShapeProp("dead-tree-stump"){{
            clipSize = 16f;
            variants = 1;
            rotateRegions = drawUnder = true;
            spriteOffsets = new Vec2[]{
            new Vec2(-4f, -4f),
            };
        }};

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

        // Albaster
        alabaster = new Floor("alabaster", 4){{
            wall = alabasterWall;
        }};
        alabasterTiles = new Floor("alabaster-tiles", 3){{
            wall = alabasterWall;
        }};
        alabasterCrater = new PatternFloor("alabaster-craters", 4){{
            pattern = new Pattern("omaloon-alabaster-craters-large"){{
                shape = new RectanglePatternShape(2, 2);
                variants = 2;
            }};
            blendGroup = alabaster;
            wall = alabasterWall;
        }};

        alabasterWall = new StaticWall("alabaster-wall"){{
            variants = 3;
        }};
        alabasterBoulder = new Prop("alabaster-boulder"){{
            variants = 3;
            alabaster.asFloor().decoration = this;
            alabasterTiles.asFloor().decoration = this;
            alabasterCrater.asFloor().decoration = this;
        }};

        // Aghanite
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
        aghaniteWall = new StaticWall("aghanite-wall"){{
            variants = 4;
        }};
        aghaniteCrag = new Prop("aghanite-crag"){{
            customShadow = true;
            variants = 3;
        }};
        aghaniteBoulder = new Prop("aghanite-boulder"){{
            variants = 3;
            smoothAghanite.asFloor().decoration = this;
            crackedAghanite.asFloor().decoration = this;
            pebbledAghanite.asFloor().decoration = this;
            sandyAghanite.asFloor().decoration = this;
            aghanite.asFloor().decoration = this;
        }};

        grayAghanite = new Floor("gray-aghanite", 3){{
            wall = grayAghaniteWall;
        }};
        smoothGrayAghanite = new Floor("smooth-gray-aghanite", 3){{
            wall = grayAghaniteWall;
        }};
        grayAghaniteWall = new StaticWall("gray-aghanite-wall"){{
            variants = 2;
        }};
        grayAghaniteCrag = new Prop("gray-aghanite-crag"){{
            customShadow = true;
            variants = 2;
        }};
        grayAghaniteBoulder = new Prop("gray-aghanite-boulder"){{
            variants = 3;
            grayAghanite.asFloor().decoration = this;
            smoothGrayAghanite.asFloor().decoration = this;
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
        whiteAghaniteWall = new StaticWall("white-aghanite-wall"){{
            variants = 3;
        }};
        whiteAghaniteBoulder = new Prop("white-aghanite-boulder"){{
            customShadow = true;
            variants = 2;
            smoothWhiteAghanite.asFloor().decoration = this;
            weatheredWhiteAghanite.asFloor().decoration = this;
            whiteAghanite.asFloor().decoration = this;
        }};

        verdantAghanite = new Floor("verdant-aghanite", 3){{
            wall = verdantAghaniteWall;
        }};
        verdantAghaniteWall = new StaticWall("verdant-aghanite-wall"){{
            variants = 3;
        }};
        verdantAghaniteBoulder = new Prop("verdant-aghanite-boulder"){{
            customShadow = true;
            variants = 2;
            verdantAghanite.asFloor().decoration = this;
        }};

        // Glacium Crater
        deepGlacium = new Floor("deep-glacium", 1){{
            speedMultiplier = 0.1f;
            liquidDrop = OlLiquids.glacium;
            liquidMultiplier = 1.3f;
            isLiquid = true;
            status = OlStatusEffects.glacied;
            statusDuration = 120f;
            drownTime = 200f;
            cacheLayer = OlShaders.glaciumLayer;
            albedo = 0.9f;
            supportsOverlay = true;
        }};
        glacium = new Floor("shallow-glacium", 1){{
            speedMultiplier = 0.3f;
            status = OlStatusEffects.glacied;
            statusDuration = 90f;
            liquidDrop = OlLiquids.glacium;
            isLiquid = true;
            cacheLayer = OlShaders.glaciumLayer;
            albedo = 0.9f;
            supportsOverlay = true;
        }};
        greniteGlacium = new Floor("grenite-glacium", 3){{
            speedMultiplier = 0.6f;
            status = OlStatusEffects.glacied;
            statusDuration = 60f;
            liquidDrop = OlLiquids.glacium;
            isLiquid = true;
            shallow = true;
            cacheLayer = OlShaders.glaciumLayer;
            albedo = 0.9f;
            supportsOverlay = true;
        }};

        grenite = new Floor("grenite", 4){{
            wall = alabasterWall;
        }};
        coastalGrenite = new Floor("coastal-grenite", 3){{
            wall = alabasterWall;
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

        // Ice & Snow
        blueIce = new Floor("blue-ice", 3){{
            mapColor = Color.valueOf("5195ab");
            wall = blueIceWall;
            albedo = 0.9f;
        }};
        blueIcePieces = new OverlayFloor("blue-ice-pieces"){{
            variants = 3;
        }};
        weatheredIce = new OverlayFloor("weathered-ice"){{
            variants = 2;
        }};
        blueIceWall = new StaticWall("blue-ice-wall"){{
            variants = 2;
        }};
        weatheredIceWall = new StaticWall("weathered-ice-wall"){{
            variants = 2;
        }};

        blueSnow = new Floor("blue-snow", 3){{
            wall = blueIceWall;
            albedo = 0.7f;
        }};
        blueSnowdrifts = new OverlayFloor("blue-snowdrifts"){{
            variants = 3;
        }};
        blueSnowWall = new StaticWall("blue-snow-wall"){{
            variants = 2;
        }};
        blueBoulder = new Prop("blue-boulder"){{
            variants = 3;
            blueIce.asFloor().decoration = this;
            blueSnow.asFloor().decoration = this;
        }};
    }
}