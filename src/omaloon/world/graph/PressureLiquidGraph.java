package omaloon.world.graph;

import arc.struct.*;
import mindustry.*;
import omaloon.gen.*;
import omaloon.world.interfaces.*;

public class PressureLiquidGraph {
	public PressureUpdater entity;

	public final Seq<HasPressure> builds = new Seq<>();

	public boolean changed;

	public PressureLiquidGraph() {
		entity = PressureUpdater.create();
		entity.graph(this);
		entity.add();
	}

	/**
	 * self-explanatory
	 */
	public void addBuild(HasPressure build) {
		builds.addUnique(build);
		build.pressure().graph = this;
		for (HasPressure next : build.nextBuilds()) {
			if (!builds.contains(next)) addBuild(next);
		}
		changed = true;
	}
	/**
	 * @param erased when false will put the build in another graph.
	 * Always make erased true when the build will be removed from the game
	 */
	public void removeBuild(HasPressure build, boolean erased) {
		builds.remove(build);
		if (!erased) {
			build.pressure().graph = new PressureLiquidGraph();
			build.pressureGraph().addBuild(build);
			if (builds.isEmpty()) entity.remove();
		} else {
			for (HasPressure next : build.nextBuilds()) {
				removeBuild(next, false);
			}
		}
		changed = true;
	}

	public void update() {
		if (changed) {
			builds.removeAll(b -> b == null || Vars.world.build(b.pos()) == null);
			changed = false;
		}
	}
}
