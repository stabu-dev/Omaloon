package omaloon.world.meta;

import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.type.*;
import omaloon.world.interfaces.*;

public class PressureSection {
	public Seq<HasPressure> builds = new Seq<>();

	/**
	 * Temporary seqs for use in flood.
	 */
	public static final Seq<HasPressure> tmp = new Seq<>(), tmp2 = new Seq<>();

	/**
	 * Adds a build to this section, and removes the build from its older section.
	 */
	public void addBuild(HasPressure build) {
		builds.add(build);
		build.pressure().section.builds.remove(build);
		build.pressure().section = this;
	}

	/**
	 * Adds a certain amount of a fluid into this module, updating it's pressure accordingly. A null liquid means that air is being added to it.
	 */
	public void addFluid(@Nullable Liquid liquid, float amount) {
		if (builds.isEmpty()) return;
		float div = amount/builds.size;
		builds.each(b -> b.pressure().addFluid(liquid, div, b.pressureConfig()));
	}

	/**
	 * Merges buildings to this section with floodFill.
	 */
	public void mergeFlood(HasPressure other) {
		tmp.clear().add(other);
		tmp2.clear();

		while(!tmp.isEmpty()) {
			HasPressure next = tmp.pop();
			tmp2.addUnique(next);
			next.nextBuilds().each(b -> {
				if (b.getSectionDestination(next) != null && !tmp2.contains(b.getSectionDestination(next))) {
					tmp.add(b.getSectionDestination(next));
				}
			});
		}

		tmp2.each(this::addBuild);
		updateLiquids();
	}

	/**
	 * Removes a certain amount of a fluid from this module, updating pressure accordingly. A null liquid means that air is being removed from it. Liquids cannot be negative.
	 */
	public void removeFluid(@Nullable Liquid liquid, float amount) {
		if (builds.isEmpty()) return;
		float div = amount/builds.size;
		builds.each(b -> b.pressure().removeFluid(liquid, div, b.pressureConfig()));
	}

	public void updateLiquids() {
		float[] liquids = new float[Vars.content.liquids().size];
		float air = 0;

		for(Liquid liquid : Vars.content.liquids()) for (HasPressure build : builds) {
			liquids[liquid.id] += build.pressure().liquids[liquid.id];
			build.pressure().liquids[liquid.id] = 0;
			build.pressure().pressures[liquid.id] = 0;
		}
		for(HasPressure build : builds) {
			air += build.pressure().air;
			build.pressure().air = 0;
			build.pressure().pressure = 0;
		}

		for(Liquid liquid : Vars.content.liquids()) addFluid(liquid, liquids[liquid.id]);
		addFluid(null, air);
	}
}
