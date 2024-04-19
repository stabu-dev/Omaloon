package omaloon.world.meta;

import arc.*;
import arc.math.*;
import arc.struct.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import omaloon.world.interfaces.*;

public class PressureConfig {
	public boolean
		acceptsPressure = true,
		outputsPressure = true,
		linksGraph = true, flows = true;
	public float
		overPressureDamageScl = 1f,
		minPressure = -100,
		maxPressure = 100;
	public Seq<Class<? extends HasPressure>> linkBlackList = new Seq<>();

	public void addStats(Stats stats) {
		stats.add(OlStats.minPressure, minPressure, OlStats.pressureUnits);
		stats.add(OlStats.maxPressure, maxPressure, OlStats.pressureUnits);
	}

	public void addBars(Block block) {
		block.addBar("pressure", entity -> {
			HasPressure build = (HasPressure) entity;
			return new Bar(
				() -> Core.bundle.get("pressure") + Mathf.round(build.getPressure()),
				build::getBarColor,
				build::getPressureMap
			);
		});
	}
}
