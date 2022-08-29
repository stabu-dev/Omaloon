package ol.content;

import arc.graphics.Color;
import arc.math.Mathf;
import mindustry.game.Team;
import mindustry.graphics.g3d.*;
import mindustry.type.Planet;
import mindustry.world.meta.Attribute;
import ol.graphics.OlPal;
import ol.system.generators.OmaLoonPlanetGenerator;
import ol.type.planets.OlPlanet;

public class OlPlanets{

    public static Planet amsha, omaloon;

    public static void load() {
        amsha = new OlPlanet("amsha", null, 4f, 0) {{
            averageSurfaceTemperature = 2300;
            rad = 50;
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
            averageSurfaceTemperature = 193.15f;
            rad = 0.15f;
            orbitRad = 446;
            generator = new OmaLoonPlanetGenerator();
            hasAtmosphere = true;
            meshLoader = () -> new MultiMesh(
                    new HexMesh(this, 6)
            );
            allowSectorInvasion = false;
            atmosphereColor = OlPal.OLDarkBlue;
            atmosphereRadIn = 0.02f;
            atmosphereRadOut = 0.3f;
            landCloudColor = OlPal.OLDarkBlue.cpy().a(0.5f);
            orbitRadius = 60f;
            startSector = 12;
            accessible = true;
            alwaysUnlocked = false;
            bloom = false;
            orbitTime = Mathf.pow(orbitRadius, 1.5f) * 960;
            ruleSetter = r -> {
              r.waveTeam = Team.green;
              r.attributes.set(Attribute.heat, -0.2f);
              r.showSpawns = true;
              r.coreCapture = true;
              r.coreIncinerates = false;
            };
        }};
    }
}
