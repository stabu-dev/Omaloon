package omaloon.content;

import arc.util.*;
import mindustry.*;
import mindustry.type.*;
import omaloon.type.*;

public class OlSectorPresets {
	public static SectorPreset theCrater;

	public static void load() {
		theCrater = new ExtraSectorPreset("The_Crater", OlPlanets.glasmore, 0, () -> {
			if (Vars.state.rules.objectiveFlags.contains("callhail") && Vars.state.rules.weather.isEmpty()) {
				Vars.state.rules.weather.add(new Weather.WeatherEntry(OlWeathers.hailStone) {{
					always = true;
				}});
			}
			if (Vars.state.rules.objectiveFlags.contains("hailend")) {
				Vars.state.rules.objectiveFlags.remove("hailend");
				Vars.state.rules.weather.replace(
					Vars.state.rules.weather.peek(), new Weather.WeatherEntry(OlWeathers.hailStone,
						2.5f * Time.toMinutes, 5f * Time.toMinutes,
						30f * Time.toSeconds, 1.5f * Time.toMinutes
					)
				);
			}
		});
	}
}
