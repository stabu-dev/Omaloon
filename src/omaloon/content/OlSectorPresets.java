package omaloon.content;

import arc.util.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.type.*;
import omaloon.type.*;

import static omaloon.type.ExtraSectorPreset.*;

public class OlSectorPresets {
	public static SectorPreset theCrater, redeploymentPath, frozenValley;

	public static void load() {
		theCrater = new ExtraSectorPreset("The_Crater", OlPlanets.glasmore, 0, () -> {
			if (getFlag("hailmassive", true)) {
				Vars.state.rules.weather.add(new Weather.WeatherEntry(OlWeathers.hailStone,
					2.5f * Time.toMinutes, 5f * Time.toMinutes,
					30f * Time.toSeconds, 1.5f * Time.toMinutes
				) {{
					always = true;
				}});
			}
			if (getFlag("hailfinal", true) && !Vars.state.rules.weather.isEmpty()) {
				Vars.state.rules.weather.clear();
				Vars.state.rules.weather.add(new Weather.WeatherEntry(OlWeathers.hailStone,
					2.5f * Time.toMinutes, 5f * Time.toMinutes,
					30f * Time.toSeconds, 1.5f * Time.toMinutes
				));
				Groups.weather.each(weather -> weather.life = 300f);
			}
			if (getFlag("haildemo", true)) {
				Call.createWeather(OlWeathers.hailStone, 1f, 7f * 60f, 1f, 1f);
			}
		});
		redeploymentPath = new SectorPreset("Redeployment_Path", OlPlanets.glasmore, 1) {{
			captureWave = 15;
		}};
		frozenValley = new SectorPreset("Frozen_Valley", OlPlanets.glasmore, 2) {{
			captureWave = 20;
			difficulty = 2;
		}};
	}
}
