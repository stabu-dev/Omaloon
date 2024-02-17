package omaloon.world.consumers;

import arc.math.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.consumers.*;
import omaloon.world.interfaces.*;

public class PressureEfficiencyRange extends Consume {
	public float startRange, endRange, efficiencyMultiplier;
	public boolean reverse;

	public PressureEfficiencyRange(float startRange, float endRange, float efficiencyMultiplier, boolean reverse) {
		this.startRange = startRange;
		this.endRange = endRange;
		this.efficiencyMultiplier = efficiencyMultiplier;
		this.reverse = reverse;
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
		if (reverse) {
			return startRange >= build.getPressure() && build.getPressure() >= endRange;
		} else {
			return startRange <= build.getPressure() && build.getPressure() <= endRange;
		}
	}
}
