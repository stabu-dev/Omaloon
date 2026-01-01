package omaloon.content;

import arc.graphics.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.graphics.g3d.*;
import mindustry.maps.planet.*;
import mindustry.type.*;
import mindustry.world.meta.*;

public class OlPlanets{
    public static Planet
    omaloon,

    glasmore;

    public static void load() {
        omaloon = new Planet("omaloon", null, 4f) {{
            bloom = true;
            accessible = false;
            // TODO temporary sun copy
            meshLoader = () -> new SunMesh(
                this,
                4, 5.0, 0.3, 1.7, 1.2, 1.0, 1.1F,
                Color.valueOf("ff7a38"),
                Color.valueOf("ff9638"),
                Color.valueOf("ffc64c"),
                Color.valueOf("ffc64c"),
                Color.valueOf("ffe371"),
                Color.valueOf("f4ee8e")
            );
        }};

        glasmore = new Planet("glasmore", omaloon, 1f, 3) {{
            generator = new ErekirPlanetGenerator();
            meshLoader = () -> new MultiMesh(
                new NoiseMesh(this, 0, 6, Color.valueOf("d4f2ff").mul(0.8f), 1, 1, 1, 4, 0.025f)
            );
//            cloudMeshLoader = () -> new MultiMesh(
//            new HexSkyMesh(this, 2, 0.15f, 0.14f, 5, Color.valueOf("eba768").a(0.75f), 2, 0.42f, 1f, 0.43f),
//            new HexSkyMesh(this, 3, 0.6f, 0.15f, 5, Color.valueOf("eea293").a(0.75f), 2, 0.42f, 1.2f, 0.45f)
//            );
            alwaysUnlocked = true;
            landCloudColor = Color.valueOf("ed6542");
            atmosphereColor = Color.valueOf("f07218");
//            defaultEnv = Env.scorching | Env.terrestrial;
//            startSector = 10;
            hasAtmosphere = false;
            atmosphereRadIn = 0.02f;
            atmosphereRadOut = 0.3f;
//            tidalLock = true;
//            orbitSpacing = 2f;
//            totalRadius += 2.6f;
//            lightSrcTo = 0.5f;
//            lightDstFrom = 0.2f;
            clearSectorOnLose = true;
            defaultCore = Blocks.coreBastion;
            iconColor = Color.valueOf("ff9266");
//            enemyBuildSpeedMultiplier = 0.4f;
            allowLaunchToNumbered = false;
//            updateLighting = false;

            defaultAttributes.set(Attribute.heat, 0.8f);

            ruleSetter = r -> {
                r.waveTeam = Team.malis;
                r.placeRangeCheck = false;
                r.showSpawns = true;
                r.fog = true;
                r.staticFog = true;
                r.lighting = false;
                r.coreDestroyClear = true;
                r.onlyDepositCore = true;
            };
            campaignRuleDefaults.fog = true;
            campaignRuleDefaults.showSpawns = true;
            campaignRuleDefaults.rtsAI = true;

            unlockedOnLand.add(Blocks.coreBastion);
        }};
    }
}
