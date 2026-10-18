package omaloon.content;

import arc.util.*;
import mindustry.type.*;
import mindustry.type.Weather.*;
import omaloon.type.*;

public class OlSectorPresets{
    public static SectorPreset crater, redeploymentPath, deadValley;

    public static void load() {
        crater = new OlSectorPreset("crater", OlPlanets.glasmore, 41) {{
            alwaysUnlocked = true;
            difficulty = 1;
            // the first landing starts with the map loadout
            overrideLaunchDefaults = true;
            addStartingItems = true;

            // the last wave is set once the floe core is built
            captureWave = 2;
            capturedWeather.add(new WeatherEntry(OlWeathers.hailStorm, 2f * Time.toMinutes, 5f * Time.toMinutes, 1.5f * Time.toMinutes, 3f * Time.toMinutes));
        }};
        redeploymentPath = new OlSectorPreset("redeployment-path", OlPlanets.glasmore, 219) {{
            captureWave = 15;
        }};
    }
}
