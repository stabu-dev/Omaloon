package omaloon.world.consumers;

import arc.math.*;
import mindustry.gen.*;
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

	@Override
	public float efficiency(Building build) {
		if (reverse) {
			return Mathf.maxZero(Mathf.map(cast(build).getPressure(), startRange, endRange, 0f, efficiencyMultiplier));
		} else {
			return Mathf.maxZero(Mathf.map(cast(build).getPressure(), endRange, startRange, 0f, efficiencyMultiplier));
		}
	}
	@Override
	public float efficiencyMultiplier(Building build) {
		if (reverse) {
			return Mathf.maxZero(Mathf.map(cast(build).getPressure(), startRange, endRange, 0f, efficiencyMultiplier));
		} else {
			return Mathf.maxZero(Mathf.map(cast(build).getPressure(), endRange, startRange, 0f, efficiencyMultiplier));
		}
	}
}
