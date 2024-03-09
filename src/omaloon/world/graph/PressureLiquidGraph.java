package omaloon.world.graph;

import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.type.*;
import omaloon.gen.*;
import omaloon.world.interfaces.*;

import java.util.*;

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
	@Deprecated // remove it
	public static ObjectIntMap<HasPressure> floodRange(HasPressure from, int range) {
		ObjectIntMap<HasPressure> out = new ObjectIntMap<>();
		if (from == null) return out;
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

		builds.each(build -> {
			Seq<HasPressure> others = build.nextBuilds(true);

			others.each(other -> {
				float buildP = build.getPressure();
				float otherP = other.getPressure();
				float pFlow = (buildP - (buildP + otherP) / 2f)/others.size;

				float buildF = build.liquids().currentAmount()/build.block().liquidCapacity;
				float otherF = other.liquids().currentAmount()/other.block().liquidCapacity;
				// TODO pressure affects flow
				float flow = Math.min(build.block().liquidCapacity * (buildF - otherF)/Math.max(others.size, 2f), build.liquids().currentAmount());

				if (other.acceptLiquid(build.as(), build.liquids().current()) && build.canDumpLiquid(other.as(), build.liquids().current())) {
					build.liquids().remove(build.liquids().current(), flow);
					other.handleLiquid(build.as(), build.liquids().current(), flow);
				}
				if (other.acceptsPressure(build, pFlow) && build.canDumpPressure(other, pFlow)) {
					build.removePressure(pFlow);
					other.handlePressure(pFlow);
				}
				Liquid buildLiquid = build.liquids().current();
				Liquid otherLiquid = other.liquids().current();
				if (buildLiquid.blockReactive && otherLiquid.blockReactive) {
					if (
						(!(otherLiquid.flammability > 0.3f) || !(buildLiquid.temperature > 0.7f)) &&
							(!(buildLiquid.flammability > 0.3f) || !(otherLiquid.temperature > 0.7f))
					) {
						if (
							buildLiquid.temperature > 0.7f && otherLiquid.temperature < 0.55f ||
								otherLiquid.temperature > 0.7f && buildLiquid.temperature < 0.55f
						) {
							build.liquids().remove(buildLiquid, Math.min(build.liquids().get(buildLiquid), 0.7f * Time.delta));
							if (Mathf.chanceDelta(0.1f)) {
								Fx.steam.at(build.x(), build.y());
							}
						}
					} else {
						build.damageContinuous(1f);
						other.damageContinuous(1f);
						if (Mathf.chanceDelta(0.1f)) {
							Fx.fire.at(build.x(), build.y());
						}
					}
				}
			});
		});
	}
}
