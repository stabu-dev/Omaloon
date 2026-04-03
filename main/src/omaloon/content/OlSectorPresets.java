package omaloon.content;

import mindustry.type.*;
import omaloon.type.*;

public class OlSectorPresets{
    public static SectorPreset crater, redeploymentPath, deadValley;

    public static void load() {
        crater = new OlSectorPreset("crater", OlPlanets.glasmore, 41) {{
            alwaysUnlocked = true;
            difficulty = 1;
        }};
        redeploymentPath = new OlSectorPreset("redeployment-path", OlPlanets.glasmore, 219) {{
            captureWave = 15;
        }};
    }
}
