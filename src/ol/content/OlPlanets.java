package ol.content;

import arc.graphics.Color;
import arc.graphics.gl.Shader;
import arc.math.Mathf;
import arc.struct.Seq;
import mindustry.game.Team;
import mindustry.graphics.CacheLayer;
import mindustry.graphics.Pal;
import mindustry.graphics.Shaders;
import mindustry.graphics.g3d.*;
import mindustry.type.Planet;
import mindustry.world.meta.Attribute;
import mindustry.world.meta.Env;
import ol.graphics.OlPal;
import ol.system.generators.OmaLoonPlanetGenerator;
import ol.system.generators.SetPlanetGenerator;

public class OlPlanets{

    public static Planet amsha, omaloon;

    public static void load() {
        amsha = new Planet("amsha", null, 4f, 0) {{
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

        omaloon = new Planet("omaloon", amsha, 1f, 3) {{
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
