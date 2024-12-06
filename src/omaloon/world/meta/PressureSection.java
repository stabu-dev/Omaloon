package omaloon.world.meta;

import arc.struct.*;
import arc.util.*;
import mindustry.type.*;
import omaloon.world.interfaces.*;

public class PressureSection {
	public Seq<HasPressure> builds = new Seq<>();

	/**
	 * Adds a certain amount of a fluid into this module, updating it's pressure accordingly. A null liquid means that air is being added to it.
	 */
	public void addFluid(@Nullable Liquid liquid, float amount) {
		if (builds.isEmpty()) return;
		float div = amount/builds.size;
		builds.each(b -> b.pressure().addFluid(liquid, div, b.pressureConfig()));
	}

	/**
	 * Removes a certain amount of a fluid from this module, updating pressure accordingly. A null liquid means that air is being removed from it. Liquids cannot be negative.
	 */
	public void removeFluid(@Nullable Liquid liquid, float amount) {
		if (builds.isEmpty()) return;
		float div = amount/builds.size;
		builds.each(b -> b.pressure().removeFluid(liquid, div, b.pressureConfig()));
	}
}
