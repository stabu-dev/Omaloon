package omaloon.content;

import arc.func.*;
import arc.graphics.*;
import arc.math.*;
import arc.util.noise.*;
import mindustry.content.*;
import mindustry.graphics.g3d.*;
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

    glasmore;

    public static void load(){
        omaloon = new Planet("omaloon", null, 4f){{
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

            meshLoader = () -> new AsteroidBeltMesh(this,
                60,
                20f,
                6f,
                1.5f,
                0.01f, 0.08f,
                42,
                Blocks.stoneWall,
                OlEnvironmentBlocks.verdantAghaniteWall,
                0.5f
            );
        }};

        glasmore = new OlPlanet("glasmore", omaloon, 1f, 3){{
            generator = new GlasmorePlanetGenerator();

            Func<Boolean, GenericMesh> atmosphereMeshLoader = isAtmosphere -> {
                return new MultiMesh(
                    new NoiseMesh(this, 0, 6, Color.valueOf("d4f2ff").mul(0.8f), 1, 1, 1, 4, 0.025f) {{
                        if (isAtmosphere) shader = OlShaders.depth;
                    }},
                    new HeightMesh(this, 6, 0.85f, position -> {
                        int seed = 3;
                        double octaves = 7, persistence = 0.7, scale = 0.25;
                        float mag = 2;

                        float powMountain = Mathf.clamp(Mathf.pow(Simplex.noise3d(
                            7 + seed, octaves, persistence, scale,
                            5 + position.x, 5 + position.y, 5 + position.z
                        ), 12f) * 300f, 0, 0.5f);

                        return Simplex.noise3d(
                            7 + seed, octaves, persistence, scale,
                            5 + position.x, 5 + position.y, 5 + position.z
                        ) * mag + powMountain;

                    }, (position, height) -> {
                        if (height < 1f) return Color.valueOf("574F51");

                        if (height > 1.5f) return Color.valueOf("D4F2FF");
                        return Color.valueOf("4F3F3B");
                    }) {{
                        if (isAtmosphere) shader = OlShaders.depth;
                    }},
                    new HeightMesh(this, 6, 0.85f, position -> {
                        int seed = 5;
                        double octaves = 5, persistence = 0.7, scale = 0.4;
                        float mag = 1.75f;

                        return new Interp.Pow(10).apply(Simplex.noise3d(
                            7 + seed, octaves, persistence, scale,
                            5 + position.x, 5 + position.y, 5 + position.z
                        )) * mag * Simplex.noise3d(
                            17 + seed, 3, 0.7, 0.5,
                            5 + position.x, 5 + position.y, 5 + position.z
                        );

                    }, (position, height) -> Color.valueOf("4F3F3B")) {{
                        if (isAtmosphere) shader = OlShaders.depth;
                    }}
                );
            };

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
            orbitRadius = 40f;
//            totalRadius += 2.6f;
//            lightSrcTo = 0.5f;
//            lightDstFrom = 0.2f;
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
    }
}
