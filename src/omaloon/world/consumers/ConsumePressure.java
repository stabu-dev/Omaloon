package omaloon.world.consumers;

import arc.util.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;

public class ConsumePressure extends Consume {
	public float amount;
	public boolean continuous;

	public ConsumePressure(float amount, boolean continuous) {
		this.amount = amount;
		this.continuous = continuous;
	}

	public HasPressure cast(Building build) {
		try {
			return (HasPressure) build;
		} catch(Exception e) {
			throw new RuntimeException("This consumer should be used on a building that implements HasPressure", e);
		}
	}

	@Override public void apply(Block block) {
		block.hasLiquids = true;
	}

	@Override
	public void display(Stats stats) {
		if (continuous) {
			stats.add(OlStats.consumePressure, amount * 60f, OlStats.pressureSecond);
		} else {
			stats.add(OlStats.consumePressure, amount, OlStats.pressureUnits);
		}
	}

	@Override public void trigger(Building build) {
		if (!continuous) cast(build).removePressure(amount * Time.delta);
	}
	@Override public void update(Building build) {
		if (continuous) cast(build).removePressure(amount * Time.delta);
	}
}
