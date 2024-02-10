package omaloon.world.meta;

import mindustry.world.meta.*;

public class PressureConfig {
	public boolean
		acceptsPressure = true,
		outputsPressure = true,
		linksGraph = true;
	public float
		overPressureDamageScl = 1f,
		minPressure = -100,
		maxPressure = 100;

	public void addStats(Stats stats) {
		stats.add(OlStats.minPressure, minPressure, OlStats.pressureUnits);
		stats.add(OlStats.maxPressure, maxPressure, OlStats.pressureUnits);
	}
}
