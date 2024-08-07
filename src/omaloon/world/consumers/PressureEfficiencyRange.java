package omaloon.world.consumers;

import arc.*;
import arc.math.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;

public class PressureEfficiencyRange extends Consume {
	public float startRange, endRange, efficiencyMultiplier;
	public boolean reverse;

	public PressureEfficiencyRange(float startRange, float endRange, float efficiencyMultiplier, boolean reverse) {
		this.startRange = startRange;
		this.endRange = endRange;
		this.efficiencyMultiplier = efficiencyMultiplier;
		this.reverse = reverse;
	}

	@Override public void apply(Block block) {
		block.hasLiquids = true;
	}

	public HasPressure cast(Building build) {
		try {
			return (HasPressure) build;
		} catch(Exception e) {
			throw new RuntimeException("This consumer should be used on a building that implements HasPressure", e);
		}
	}

	@Override
	public void display(Stats stats) {
		stats.add(OlStats.pressureRange, Core.bundle.get("stat.omaloon-pressurerange.format"), startRange, endRange);
	}

	@Override
	public float efficiency(Building build) {
		return shouldConsume(cast(build)) ? 1f : 0f;
	}
	@Override
	public float efficiencyMultiplier(Building build) {
		if (!shouldConsume(cast(build))) return 0f;
		if (reverse) {
			return Mathf.maxZero(Mathf.map(cast(build).getPressure(), endRange, startRange, 1f, efficiencyMultiplier));
		} else {
			return Mathf.maxZero(Mathf.map(cast(build).getPressure(), startRange, endRange, 1f, efficiencyMultiplier));
		}
	}

	public boolean shouldConsume(HasPressure build) {
		int pressure = Math.round(build.getPressure());
		return startRange <= pressure && pressure <= endRange;
	}
}
