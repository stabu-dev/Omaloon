package omaloon.content;

import arc.func.*;
import arc.graphics.*;
import arc.math.*;
import arc.util.noise.*;
import mindustry.content.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;
import mindustry.world.meta.*;
import omaloon.content.blocks.*;
import omaloon.graphics.*;
import omaloon.graphics.g3d.*;
import omaloon.maps.generators.*;
import omaloon.type.*;

public class OlPlanets{
    public static Planet
    omaloon,

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

        glasmore = new OlPlanet("glasmore", omaloon, 1f, 3){{
            generator = new GlasmorePlanetGenerator();

            Prov<GenericMesh> atmosphereMeshLoader = () -> new MultiMesh(
                new NoiseMesh(this, 0, 6, Color.valueOf("d4f2ff").mul(0.8f), 1, 1, 1, 4, 0.025f) {{
                    shader = OlShaders.depth;
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
                    shader = OlShaders.depth;
                }}
            );

            meshLoader = () -> new MultiMesh(
                new AtmosphereMesh(this, atmosphereMeshLoader.get()),
                new NoiseMesh(this, 0, 6, Color.valueOf("d4f2ff").mul(0.8f), 1, 1, 1, 4, 0.025f),
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
                })
            );
            cloudMeshLoader = () -> new MultiMesh(
                new HexSkyMesh(this, 6, -0.5f, 0.14f, 6, Color.valueOf("D4F2FF").a(0.3f), 2, 0.42f, 1f, 0.6f),
                new HexSkyMesh(this, 1, 0.6f, 0.15f, 6, Color.valueOf("D4F2FF").a(0.3f), 2, 0.42f, 1.2f, 0.5f)
            );
            loadIcon = false;
            alwaysUnlocked = true;
            landCloudColor = Color.valueOf("ed6542");
            atmosphereColor = Color.valueOf("3E6067");
            hasAtmosphere = true;
            atmosphereRadIn = 0.02f;
            atmosphereRadOut = 0.3f;
//            orbitSpacing = 2f;
//            totalRadius += 2.6f;
//            lightSrcTo = 0.5f;
//            lightDstFrom = 0.2f;
            clearSectorOnLose = true;
            defaultCore = Blocks.coreBastion;
            iconColor = Color.valueOf("5e929d");
//            enemyBuildSpeedMultiplier = 0.4f;
            allowLaunchToNumbered = false;

            defaultAttributes.set(Attribute.heat, -0.8f);

            campaignRuleDefaults.fog = true;
            campaignRuleDefaults.showSpawns = true;
            campaignRuleDefaults.rtsAI = true;

            unlockedOnLand.add(OlStorageBlocks.landingCapsule);
        }};
    }
}
