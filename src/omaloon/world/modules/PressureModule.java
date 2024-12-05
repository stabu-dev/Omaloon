package omaloon.world.modules;

import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.type.*;
import mindustry.world.modules.*;
import omaloon.world.meta.*;

public class PressureModule extends BlockModule {
	public static float arbitraryPressureScalar = 10f;

	public float[] liquids = new float[Vars.content.liquids().size];
	public float[] pressures = new float[Vars.content.liquids().size];
	public float air = 0, pressure = 0;

	public @Nullable Liquid current;

	/**
	 * Adds a certain amount of a fluid into this module, updating it's pressure accordingly. A null liquid means that air is being added to it.
	 */
	public void addFluid(@Nullable Liquid liquid, float amount, PressureConfig reference) {
		if (liquid == null) {
			air += amount;
			pressure = air / reference.fluidCapacity * arbitraryPressureScalar;
		} else {
			liquids[liquid.id] += amount;
			pressures[liquid.id] = liquids[liquid.id] / reference.fluidCapacity * arbitraryPressureScalar;
		}
		current = liquid;
	}

	public float currentAmount() {
		return current == null ? air : liquids[current.id];
	}

	public @Nullable Liquid getMain() {
		Liquid out = null;
		for(int i = 0; i < liquids.length; i++) {
			if (out == null && liquids[i] > 0) out = Vars.content.liquid(i);
			if (out != null && liquids[i] > liquids[out.id]) out = Vars.content.liquid(i);
		}
		return out;
	}

	/**
	 * Returns the amount of pressure felt by this module from a certain liquid + the pressure of air inside this module.
	 */
	public float getPressure(@Nullable Liquid liquid) {
		if (liquid == null) {
			return pressure;
		}
		return pressures[liquid.id] + pressure;
	}

	@Override
	public void read(Reads read) {
		pressure = read.f();
	}

	/**
	 * Removes a certain amount of a fluid from this module, updating pressure accordingly. A null liquid means that air is being removed from it. Liquids cannot be negative.
	 */
	public void removeFluid(@Nullable Liquid liquid, float amount, PressureConfig reference) {
		if (liquid == null) {
			air -= amount;
			pressure = air / reference.fluidCapacity * arbitraryPressureScalar;
		} else {
			liquids[liquid.id] = Mathf.maxZero(liquids[liquid.id] - amount);
			pressures[liquid.id] = liquids[liquid.id] / reference.fluidCapacity * arbitraryPressureScalar;
		}
		current = liquid;
	}

	@Override
	public void write(Writes write) {
		write.f(pressure);
	}
}
