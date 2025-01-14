package omaloon.world.consumers;

import arc.*;
import arc.math.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;

public class ConsumeFluid extends Consume {
	/**
	 * Fluid used by this consumer, if null, air is used
	 */
	public @Nullable Liquid fluid;
	/**
	 * Amount of fluid consumed, will always be positive if fluid isn't air.
	 */
	public float amount;
	/**
	 * If true, fluid is consumed per tick and not per craft.
	 */
	public boolean continuous;

	/**
	 * Min pressure required for this consumer to function.
	 */
	public float startRange;
	/**
	 * Max pressure allowed for this consumer to function.
	 */
	public float endRange;
	/**
	 * Efficiency multiplier of this consumer. Based on pressure.
	 */
	public float efficiencyMultiplier = 1f;
	/**
	 * Pressure whose building's efficiency is at it's peak.
	 */
	public float optimalPressure;
	/**
	 * Whether to display the optimal pressure.
	 */
	public boolean hasOptimalPressure = false;
	/**
	 * Interpolation curve used to determine efficiency. 0 is startRange, 1 is endRange.
	 */
	public Interp curve = Interp.one;

	public ConsumeFluid(@Nullable Liquid fluid, float amount) {
		this.fluid = fluid;
		this.amount = amount;
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
		if (amount != 0) {
			if (continuous) {
				stats.add(amount > 0 ? OlStats.removeFluid : OlStats.addFluid, OlStats.fluid(fluid, Math.abs(amount), 1f, true));
			} else {
				stats.add(amount > 0 ? OlStats.removeFluid : OlStats.addFluid, OlStats.fluid(fluid, Math.abs(amount), 60f, false));
			}
		}

		if (startRange != endRange) {
			stats.add(OlStats.pressureRange, Core.bundle.get("stat.omaloon-pressure-range.format"), Strings.autoFixed(startRange, 2), Strings.autoFixed(endRange, 2));
			if (hasOptimalPressure) stats.add(OlStats.optimalPressure, Core.bundle.get("stat.omaloon-optimal-pressure.format"), Strings.autoFixed(optimalPressure, 2), Strings.autoFixed(efficiencyMultiplier * 100f, 2));
		}
	}

	@Override
	public float efficiency(Building build) {
		if (!shouldConsume(cast(build))) return 0f;
		return 1f;
	}
	@Override
	public float efficiencyMultiplier(Building build) {
		if (!shouldConsume(cast(build))) return 0f;
		return curve.apply(1f, efficiencyMultiplier, Mathf.clamp(Mathf.map(cast(build).pressure().getPressure(fluid), startRange, endRange, 0f, 1f)));

	}

	public boolean shouldConsume(HasPressure build) {
		if (startRange == endRange) return true;
		return startRange <= build.pressure().getPressure(fluid) && build.pressure().getPressure(fluid) <= endRange && (fluid == null || build.pressure().liquids[fluid.id] > amount);
	}

	@Override public void trigger(Building build) {
		if (!continuous && shouldConsume(cast(build))) {
			cast(build).removeFluid(fluid, amount);
		}
	}
	@Override public void update(Building build) {
		if (continuous && shouldConsume(cast(build))) {
			cast(build).removeFluid(fluid, amount * Time.delta);
		}
	}
}
