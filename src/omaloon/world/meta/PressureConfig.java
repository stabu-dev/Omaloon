package omaloon.world.meta;

import arc.*;
import arc.math.*;
import arc.struct.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import omaloon.world.interfaces.*;

public class PressureConfig {
	public boolean
		acceptsPressure = true,
		outputsPressure = true;
	public float
		overPressureDamage = 0.33f,
	  underPressureDamage = 0.66f,
		minPressure = -50,
		maxPressure = 50;
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
				() -> build.getPressure() > 0 ? Pal.accent : Pal.lancerLaser,
				() -> Math.abs(Mathf.map(build.getPressure(), minPressure, maxPressure, -1, 1))
			);
		});
	}
}
