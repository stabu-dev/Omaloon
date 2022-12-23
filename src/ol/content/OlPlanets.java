package ol.content;

import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;

import mindustry.game.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;
import mindustry.world.meta.*;

import ol.graphics.*;
import ol.graphics.g3d.*;
import ol.generators.*;
import ol.type.planets.*;

public class OlPlanets {

    public static Planet amsha, omaloon;

    public static void load() {
        amsha = new OlPlanet("amsha", null, 4f, 0) {{
            bloom = true;
            accessible = false;
            hasAtmosphere = true;

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

        omaloon = new OlPlanet("omaloon", amsha, 1f, 3) {{
            generator = new OmaloonPlanetGenerator();

            hasAtmosphere = true;
            meshLoader = () -> new MultiMesh(
                    new CircleMesh(100, 1.9f, 2.1f, new Vec3(0,1,0).rotate(Vec3.X, 25)),
                    new HexMesh(this, 6)
            );

            allowSectorInvasion = false;
            atmosphereColor = OlPal.oLDarkBlue;

            atmosphereRadIn = 0.02f;
            atmosphereRadOut = 0.3f;

            landCloudColor = OlPal.oLDarkBlue.cpy().a(0.5f);

            orbitRadius = 60f;
            startSector = 12;

            accessible = true;
            alwaysUnlocked = true;
            bloom = false;

            iconColor = OlPal.oLDarkBlue;
            orbitTime = Mathf.pow(orbitRadius, 1.5f) * 960;

            ruleSetter = rules -> {
                rules.waveTeam = Team.green;
                rules.attributes.set(Attribute.heat, -0.2f);

                rules.showSpawns = true;
                rules.coreCapture = true;
                rules.coreIncinerates = false;
            };
        }};
    }
}
