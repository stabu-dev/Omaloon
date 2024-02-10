package omaloon.world.meta;

import mindustry.world.meta.*;

public class OlStats {
	public static final StatCat pressure = new StatCat("omaloon-pressure");

	public static final Stat
		minPressure = new Stat("omaloon-minPressure", pressure),
		maxPressure = new Stat("omaloon-maxPressure", pressure),
		consumePressure = new Stat("omaloon-consumePressure", pressure),
		outputPressure = new Stat("omaloon-outputPressure", pressure);

	public static final StatUnit
		pressureUnits = new StatUnit("omaloon-pressureUnit"),
		pressureSecont = new StatUnit("omaloon-pressureSecond");
}
