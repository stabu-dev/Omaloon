package omaloon.world.graph;

import arc.struct.*;
import omaloon.gen.*;
import omaloon.world.interfaces.*;

import java.util.*;

public class PressureLiquidGraph {
	public PressureUpdater entity;

	public final Seq<HasPressure> builds = new Seq<>();

	public boolean changed;

	// TODO temporary, will change depending on the amount of pressure
	public static int flowRange = 3;
	public static int flowSteps = 10;

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
		for (HasPressure next : build.nextBuilds(false)) {
			if (!builds.contains(next) && next.pressureConfig().linksGraph) addBuild(next);
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
		} else {
			for (HasPressure next : build.nextBuilds(false)) {
				removeBuild(next, false);
			}
		}
		if (builds.isEmpty()) entity.remove();
		changed = true;
	}

	/**
	 * returns a list of blocks and it's respective distances in blocks from the source block
	 * list will go visually farther in bigger blocks
	 */
	public static ObjectIntMap<HasPressure> floodRange(HasPressure from, int range) {
		ObjectIntMap<HasPressure> out = new ObjectIntMap<>();
		if (from == null) return out;
		PressureLiquidGraph sourceGraph = from.pressureGraph();
		Seq<HasPressure> temp = Seq.with(from);
		out.put(from, range);
		range--;

		while (range > 0 && !temp.isEmpty()) {
			Seq<HasPressure> temp2 = Seq.with();
			while (!temp.isEmpty()) {
				Seq<HasPressure> nextBuilds = temp.pop().nextBuilds(true).removeAll(Objects::isNull);
				int finalRange = range;
				nextBuilds.each(b -> {
					if (!out.containsKey(b)) {
						temp2.add(b);
						out.put(b, finalRange);
					}
				});
			}
			temp.add(temp2);
			range--;
		}
		return out;
	}

	public void update() {
		if (changed) {
			builds.removeAll(build -> !build.isValid() || build.pressureGraph() != this);
			if (builds.isEmpty()) entity.remove();
			changed = false;
		}

		// TODO updade the least pressure builds too
		for (int i = 0; i < flowSteps; i++) {
			HasPressure mostPressure = builds.max(HasPressure::getPressure);
			if (builds.size <= 1) break;
			ObjectIntMap<HasPressure> flowMap = floodRange(mostPressure, flowRange);
			float distributedPressure = mostPressure.getPressure() - flowMap.keys().toArray().sumf(HasPressure::getPressure) / flowMap.keys().toArray().size + 1;
			Seq<HasPressure> others = flowMap.keys().toArray().removeAll(b -> b == mostPressure);
			mostPressure.removePressure(distributedPressure);
			others.each(build -> build.handlePressure(distributedPressure/others.size));
		}
	}
}
