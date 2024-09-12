package omaloon.content;

import arc.util.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.type.Weather.*;
import omaloon.type.*;

import static arc.util.Time.*;
import static omaloon.type.ExtraSectorPreset.*;

public class OlSectorPresets {
	public static SectorPreset theCrater, redeploymentPath, frozenValley;

	public static void load() {
		theCrater = new ExtraSectorPreset("The_Crater", OlPlanets.glasmore, 492, () -> {
			if (getFlag("hailmassive", true)) {
				Vars.state.rules.weather.add(new WeatherEntry(OlWeathers.hailStone,
					2.5f * toMinutes, 5f * toMinutes,
					30f * Time.toSeconds, 1.5f * toMinutes
				) {{
					always = true;
				}});
			}
			if (getFlag("hailfinal", true) && !Vars.state.rules.weather.isEmpty()) {
				Vars.state.rules.weather.clear();
				Vars.state.rules.weather.add(new WeatherEntry(OlWeathers.hailStone,
					2.5f * toMinutes, 5f * toMinutes,
					30f * toSeconds, 1.5f * toMinutes
				));
				Groups.weather.each(weather -> weather.life = 300f);
			}
			if (getFlag("haildemo", true)) {
				Call.createWeather(OlWeathers.hailStone, 1f, 7f * 60f, 1f, 1f);
			}
		});
		redeploymentPath = new ExtraSectorPreset("Redeployment_Path", OlPlanets.glasmore, 607, () -> {
			if (getFlag("addweather", true)) {
				Vars.state.rules.weather.clear();
				Vars.state.rules.weather.add(
					new WeatherEntry(OlWeathers.wind, toMinutes, 12f * toMinutes, 2f * toMinutes, 3f * toMinutes),
					new WeatherEntry(OlWeathers.aghaniteStorm, 1.5f * toMinutes, 5f * toMinutes, 5f * toMinutes, 8f * toMinutes)
				);
			}
		}) {{
			captureWave = 15;
		}};
		frozenValley = new ExtraSectorPreset("Frozen_Valley", OlPlanets.glasmore, 660, () -> {
			if (getFlag("addweather", true)) {
				Vars.state.rules.weather.clear();
				Vars.state.rules.weather.add(
					new WeatherEntry(OlWeathers.wind, 0.5f * toMinutes, 2.5f * toMinutes, 5f * toMinutes, 10f * toMinutes)
				);
			}
		}) {{
			captureWave = 20;
			difficulty = 3;
		}};
	}
}
