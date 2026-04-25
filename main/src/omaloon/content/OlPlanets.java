package omaloon.content;

import arc.func.*;
import arc.graphics.*;
import arc.math.*;
import arc.util.noise.*;
import mindustry.content.*;
import mindustry.graphics.g3d.*;
import mindustry.maps.planet.*;
import mindustry.type.*;
import mindustry.ui.dialogs.*;
import mindustry.world.meta.*;
import omaloon.content.blocks.*;
import omaloon.graphics.*;
import omaloon.graphics.g3d.*;
import omaloon.maps.generators.*;
import omaloon.type.*;

public class OlPlanets{
    public static Planet
    omaloon,
    asteroidBelt,
    glasmore, purpura;

    public static void load(){
        omaloon = new Planet("omaloon", null, 3f){{
            bloom = true;
            accessible = false;
            meshLoader = () -> new SunMesh(
            this, 4, 5, 0.3f, 1.0f, 1.2f, 1, 1.3f,
            Color.valueOf("#8B4513"),
            Color.valueOf("#A0522D"),
            Color.valueOf("c2311e"),
            Color.valueOf("ff6730"),
            Color.valueOf("bf342f"),
            Color.valueOf("8e261d")
            );
        }};

        glasmore = new OlPlanet("glasmore", omaloon, 1f, 4){{
            generator = new GlasmorePlanetGenerator();

            CraterData craterData = new CraterData(42, 50, position ->
                Simplex.noise3d(7, 1, 1, 4, 5f + position.x, 5f + position.y, 5f + position.z) * 0.025f
            );
            craterData.noiseIntensity = 0.18f;

            Color glaciumCol = Color.valueOf("5e929d");
            Color groundCol = Color.valueOf("574F51");

            CraterMesh.HeightFunc baseHeightFunc = position ->
                Simplex.noise3d(7, 1, 1, 4, 5f + position.x, 5f + position.y, 5f + position.z) * 0.025f;

            Func<Boolean, GenericMesh> atmosphereMeshLoader = isAtmosphere -> new MultiMesh(
                new CraterMesh(this, 6, 1f, craterData, baseHeightFunc,
                    (pos, h, out) -> out.set(Color.valueOf("d4f2ff").mul(0.8f)),
                    groundCol, glaciumCol){{ if(isAtmosphere) shader = OlShaders.depth; }},
                new CraterMesh(this, 6, 0.85f, craterData,
                    position -> {
                        int seed = 3;
                        double octaves = 7, persistence = 0.7, scale = 0.25;
                        float mag = 2;
                        float powMountain = Mathf.clamp(Mathf.pow(Simplex.noise3d(7 + seed, octaves, persistence, scale, 5 + position.x, 5 + position.y, 5 + position.z), 12f) * 300f, 0, 0.5f);
                        return Simplex.noise3d(7 + seed, octaves, persistence, scale, 5 + position.x, 5 + position.y, 5 + position.z) * mag + powMountain;
                    },
                    (pos, h, out) -> {
                        if(h < 1f) out.set(Color.valueOf("574F51"));
                        else if(h > 1.5f) out.set(Color.valueOf("D4F2FF"));
                        else out.set(Color.valueOf("4F3F3B"));
                    },
                    groundCol, glaciumCol){{ if(isAtmosphere) shader = OlShaders.depth; }},
                new CraterMesh(this, 6, 0.85f, craterData,
                    position -> {
                        int seed = 5;
                        double octaves = 5, persistence = 0.7, scale = 0.4;
                        float mag = 1.75f;
                        return (float)Math.pow(Simplex.noise3d(7 + seed, octaves, persistence, scale, 5 + position.x, 5 + position.y, 5 + position.z), 10) * mag * Simplex.noise3d(17 + seed, 3, 0.7, 0.5, 5 + position.x, 5 + position.y, 5 + position.z);
                    },
                    (pos, h, out) -> out.set(Color.valueOf("4F3F3B")),
                    groundCol, glaciumCol){{ if(isAtmosphere) shader = OlShaders.depth; }},
                new CraterPoolMesh(this, 6, 1f, craterData, baseHeightFunc, glaciumCol){{
                    if(isAtmosphere) shader = OlShaders.depth;
                }}
            );

            meshLoader = () -> new MultiMesh(
                new AtmosphereMesh(this, atmosphereMeshLoader.get(true)),
                atmosphereMeshLoader.get(false),
                new QuadMesh(this, "omaloon-rings"){{
                    radius = 2.4f;
                    stroke = 1f;
                    updateMesh();
                }}
            );
            cloudMeshLoader = () -> new MultiMesh(
            new HexSkyMesh(this, 6, -0.5f, 0.14f, 6, Color.valueOf("D4F2FF").a(0.3f), 2, 0.42f, 1f, 0.6f),
            new HexSkyMesh(this, 1, 0.6f, 0.15f, 6, Color.valueOf("D4F2FF").a(0.3f), 2, 0.42f, 1.2f, 0.5f)
            );
            loadIcon = false;
            alwaysUnlocked = true;
            atmosphereColor = OlEnvironmentBlocks.glacium.mapColor;
            hasAtmosphere = true;
            atmosphereRadIn = 0.02f;
            atmosphereRadOut = 0.3f;
            orbitRadius = 20f;
//          totalRadius += 2.6f;
//          lightSrcTo = 0.5f;
//          lightDstFrom = 0.2f;
            clearSectorOnLose = true;
            defaultCore = Blocks.coreBastion;
            iconColor = Color.valueOf("5e929d");
            allowLaunchToNumbered = false;

            defaultAttributes.set(Attribute.heat, -0.8f);

            startSector = 41;
            campaignRuleDefaults.fog = false;
            campaignRuleDefaults.showSpawns = true;

            unlockedOnLand.add(OlStorageBlocks.landingCapsule);
        }};

        asteroidBelt = new Planet("omaloon-asteroid-belt", omaloon, 0.01f){{
            hasAtmosphere = false;
            accessible = true;
            visible = true;
            drawOrbit = false;
            updateLighting = false;

            orbitRadius = 0f;
            orbitTime = 1f;
            rotateTime = 900f;
            clipRadius = 30f;

            //TODO: custom one
            generator = new AsteroidGenerator();

            meshLoader = () -> new MultiMesh(
            new AsteroidBeltMesh(this,
            15,
            30f,
            6f,
            1.5f,
            0.01f, 0.04f,
            42,
            OlEnvironmentBlocks.alabasterWall,
            OlEnvironmentBlocks.verdantAghaniteWall,
            0.6f
            ),
            new AsteroidBeltMesh(this,
            10,
            30f,
            6f,
            1.5f,
            0.01f, 0.04f,
            43,
            OlEnvironmentBlocks.alabasterWall,
            OlEnvironmentBlocks.greniteWall,
            0.6f
            ),
            new AsteroidBeltMesh(this,
            15,
            30f,
            6f,
            1.5f,
            0.01f, 0.08f,
            44,
            OlEnvironmentBlocks.alabasterWall,
            OlEnvironmentBlocks.smoothAghanite,
            0.6f
            ),
            new AsteroidBeltMesh(this,
            10,
            30f,
            6f,
            1.5f,
            0.01f, 0.08f,
            45,
            OlEnvironmentBlocks.alabasterWall,
            Blocks.daciteWall,
            0.5f
            ),
            new AsteroidBeltMesh(this,
            5,
            30f,
            6f,
            1.5f,
            0.01f, 0.08f,
            45,
            OlEnvironmentBlocks.alabasterWall,
            OlEnvironmentBlocks.oreNickel,
            0.6f
            ));
        }};

        purpura = new OlPlanet("purpura", omaloon, 1.5f, 0){{
            accessible = false;

            atmosphereColor = Color.valueOf("4F424D");
            atmosphereRadIn = 0;
            atmosphereRadOut = 0.05f;
            orbitRadius = 50f;
            generator = new PurpuraPlanetGenerator();

            meshLoader = () -> new MultiMesh(
            new AtmosphereHexMesh(7),
            new HexMesh(this, 7)
            );
            cloudMeshLoader = () -> new MultiMesh(
            new HexSkyMesh(this, 1, 1f, 0.05f, 6, Color.valueOf("242424").a(0.6f), 2, 0.8f, 1f, 0.f),
            new HexSkyMesh(this, 2, -1.3f, 0.06f, 6, Color.valueOf("413B42").a(0.6f), 2, 0.8f, 1f, 0.5f),
            new HexSkyMesh(this, 3, 1.3f, 0.07f, 6, Color.valueOf("7F777E").a(0.6f), 2, 0.8f, 1.2f, 0.5f),
            new HexSkyMesh(this, 4, -1.6f, 0.08f, 6, Color.valueOf("B2B2B2").a(0.6f), 2, 0.8f, 1.2f, 0.5f)
            );
        }};
    }
}
