package omaloon.world.meta;

import mindustry.world.meta.*;

public class OlStats {
	public static final StatCat pressure = new StatCat("omaloon-pressure");

	public static final Stat
		minSpeed = new Stat("omaloon-min-speed"),
		maxSpeed = new Stat("omaloon-max-speed"),

		pressureFlow = new Stat("omaloon-pressureflow", pressure),

		minPressure = new Stat("omaloon-minPressure", pressure),
		maxPressure = new Stat("omaloon-maxPressure", pressure),
		consumePressure = new Stat("omaloon-consumePressure", pressure),
		pressureRange = new Stat("omaloon-pressurerange", pressure),
		outputPressure = new Stat("omaloon-outputPressure", pressure);

	public static final StatUnit
		pressureUnits = new StatUnit("omaloon-pressureUnits"),
		pressureSecond = new StatUnit("omaloon-pressureSecond");
}
