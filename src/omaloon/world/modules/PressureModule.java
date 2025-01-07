package omaloon.world.modules;

import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.type.*;
import mindustry.world.modules.*;
import omaloon.world.meta.*;

public class PressureModule extends BlockModule {
	public static float arbitraryPressureScalar = 10f;
	public static float smoothingSpeed = 0.03f;

	public float[] liquids = new float[Vars.content.liquids().size];
	public float[] pressures = new float[Vars.content.liquids().size];
	public float air = 0, pressure = 0;

	public PressureSection section = new PressureSection();

	public Liquid current = Liquids.water;

	/**
	 * Adds a certain amount of a fluid into this module, updating it's pressure accordingly. A null liquid means that air is being added to it.
	 */
	public void addFluid(@Nullable Liquid liquid, float amount, PressureConfig reference) {
		if (liquid == null) {
			air += amount;
			pressure = air / reference.fluidCapacity * arbitraryPressureScalar;
		} else {
			if (air < 0) {
				air = 0;
				pressure = 0;
			}
			liquids[liquid.id] += amount;
			pressures[liquid.id] = liquids[liquid.id] / reference.fluidCapacity * arbitraryPressureScalar;
		}
		if (liquid != null) current = liquid;
	}

	public float get(@Nullable Liquid liquid) {
		if (liquid == null) {
			return air;
		} else {
			return liquids[liquid.id];
		}
	}

	public @Nullable Liquid getMain() {
		Liquid out = null;
		for(int i = 0; i < liquids.length; i++) {
			if (out == null && liquids[i] > 0.01) out = Vars.content.liquid(i);
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
		air = read.f();
		pressure = read.f();

		int count = read.s();

		for(int i = 0; i < count; i++){
			Liquid liq = Vars.content.liquid(read.s());
			float amount = read.f();
			float pressure = read.f();
			if(liq != null){
				if (amount > 0) current = liq;
				liquids[liq.id] = amount;
				pressures[liq.id] = pressure;
			}
		}
	}

	/**
	 * Removes a certain amount of a fluid from this module, updating pressure accordingly. A null liquid means that air is being removed from it. Liquids cannot be negative.
	 */
	public void removeFluid(@Nullable Liquid liquid, float amount, PressureConfig reference) {
		if (liquid == null) {
			air -= (getMain() != null ? Math.min(air, amount) : amount);
			pressure = air / reference.fluidCapacity * arbitraryPressureScalar;
		} else {
			liquids[liquid.id] = Mathf.maxZero(liquids[liquid.id] - amount);
			pressures[liquid.id] = liquids[liquid.id] / reference.fluidCapacity * arbitraryPressureScalar;
		}
		if (liquid != null) current = liquid;
	}

	@Override
	public void write(Writes write) {
		write.f(air);
		write.f(pressure);

		write.s(liquids.length);

		for(int i = 0; i < liquids.length; i++){
			write.s(i); //liquid ID
			write.f(liquids[i]); //liquid amount
			write.f(pressures[i]); //liquid amount
		}
	}
}
