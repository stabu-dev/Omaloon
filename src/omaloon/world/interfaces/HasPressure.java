package omaloon.world.interfaces;

import arc.graphics.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import omaloon.world.graph.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

public interface HasPressure extends Buildingc {
	PressureModule pressure();

	PressureConfig pressureConfig();
	default PressureLiquidGraph pressureGraph() {
		return pressure().graph;
	}

	/**
	 * @param flow determines if the returned value will have only builds to which it can flow pressure to
	 */
	default Seq<HasPressure> nextBuilds(boolean flow) {
		if (flow) {
			return proximity().select(b -> b instanceof HasPressure).<HasPressure>as().removeAll(b -> !b.pressureConfig().flows).map(b -> b.getPressureDestination(this, 0)).removeAll(b -> {
				return (!connects(b) && proximity().contains((Building) b)) || !b.acceptsPressure(this, 0) || !canDumpPressure(b, 0);
			});
		}
		return proximity().select(b -> b instanceof HasPressure).<HasPressure>as().map(b -> b.getPressureDestination(this, 0)).removeAll(b -> {
			return !connects(b) && proximity().contains((Building) b) || pressureConfig().linkBlackList.contains(b.getClass());
		});
	}

	/**
	 * returns current pressure of the building
	 */
	default float getPressure() {
		return pressure().pressure;
	}

	/**
	 * returns flow of liquids from one build to another
	 */
	default float getPressureFlow(HasPressure to) {
		//ensures that the liquid flows properly
		if (to.getPressure() == 0) return 1f;
		return Math.max(getPressure()/to.getPressure(), 1f);
	}

	/**
	 * bar related methods
	 */
	default float getPressureMap() {
		return Math.abs(Mathf.map(getPressure(), pressureConfig().minPressure, pressureConfig().maxPressure, -1, 1));
	}
	default Color getBarColor() {
		return getPressure() > 0 ? Pal.accent : Pal.lancerLaser;
	}

	/**
	 * can receive/send pressure to another place
	 */
	default boolean acceptsPressure(HasPressure from, float pressure) {
		return getPressure() + pressure <= from.getPressure() - pressure;
	}
	default boolean canDumpPressure(HasPressure to, float pressure) {
		return to.getPressure() + pressure <= getPressure() - pressure;
	}

	/**
	 * static connection(useful for pipes and bitmask related things)
	 */
	default boolean connects(HasPressure to) {
		return pressureConfig().outputsPressure || pressureConfig().acceptsPressure;
	}

	/**
	 * returns current pressure state
	 */
	default PressureState getPressureState() {
		if (getPressure() < pressureConfig().minPressure) return PressureState.underPressure;
		if (getPressure() > pressureConfig().maxPressure) return PressureState.overPressure;
		return PressureState.normal;
	}

	/**
	 * checks pressure for over or under pressure
	 */
	default void updateDeath() {
		switch (getPressureState()) {
			case overPressure -> damage(1 / 60f * pressureConfig().overPressureDamageScl);
			case underPressure -> kill();
			default -> {
			}
		}
	}

	/**
	 * pressure destination from dumping into this
	 */
	default HasPressure getPressureDestination(HasPressure from, float pressure) {
		return this;
	}

	/**
	 * transfers pressure between 2 buildings
	 */
	default void transferPressure(HasPressure to, float pressure) {
		if (to.acceptsPressure(this, pressure)) {
			removePressure(pressure);
			to.handlePressure(pressure);
		}
	}

	/**
	 * adds/removes pressure
	 */
	default void handlePressure(float pressure) {
		pressure().pressure += pressure;
	}
	default void removePressure(float pressure) {
		pressure().pressure -= pressure;
	}

	/**
	 * dumps pressure onto available builds
	 */
	default void dumpPressure() {
		for (HasPressure other : nextBuilds(true)) {
			float diff = getPressure() - (getPressure() + other.getPressure()) / 2f;
			if (canDumpPressure(other, diff)) {
				transferPressure(other, diff);
			}
		}
	}

	default void dumpLiquidPressure(Liquid liquid) {
		int dump = cdump();
		if (liquids().get(liquid) > 0.0001f) {
			if (!Vars.net.client() && Vars.state.isCampaign() && team() == Vars.state.rules.defaultTeam) {
				liquid.unlock();
			}

			for(int i = 0; i < nextBuilds(true).size; ++i) {
				incrementDump(nextBuilds(true).size);
				HasPressure other = nextBuilds(true).get((i + dump) % nextBuilds(true).size);
				other = other.getLiquidDestination(as(), liquid).as();
				if (other != null && other.block().hasLiquids && canDumpLiquid(other.as(), liquid) && other.liquids() != null) {
					float ofract = other.liquids().get(liquid) / other.block().liquidCapacity;
					float fract = liquids().get(liquid) / block().liquidCapacity;
					if (ofract < fract) {
						transferLiquid(other.as(), (fract - ofract) * block().liquidCapacity * getPressureFlow(other) / nextBuilds(true).size, liquid);
					}
				}
			}

		}
	}

	//
	default float moveLiquidPressure(HasPressure next, Liquid liquid) {
		if (next != null) {
			next = (HasPressure) next.getLiquidDestination(as(), liquid);
			if (next.team() == team() && next.block().hasLiquids && liquids().get(liquid) > 0f) {
				float ofract = next.liquids().get(liquid) / next.block().liquidCapacity;
				float fract = liquids().get(liquid) / block().liquidCapacity;
				float flow = Math.min(Mathf.clamp(fract - ofract) * block().liquidCapacity, liquids().get(liquid)) * getPressureFlow(next);
				flow = Math.min(flow, next.block().liquidCapacity - next.liquids().get(liquid))/2;
				if (flow > 0f && ofract <= fract && next.acceptLiquid(this.as(), liquid)) {
					next.handleLiquid(this.as(), liquid, flow);
					liquids().remove(liquid, flow);
					return flow;
				}

				if (!next.block().consumesLiquid(liquid) && next.liquids().currentAmount() / next.block().liquidCapacity > 0.1F && fract > 0.1F) {
					float fx = (x() + next.x()) / 2f;
					float fy = (y() + next.y()) / 2f;
					Liquid other = next.liquids().current();
					if (other.blockReactive && liquid.blockReactive) {
						if ((!(other.flammability > 0.3F) || !(liquid.temperature > 0.7F)) && (!(liquid.flammability > 0.3F) || !(other.temperature > 0.7F))) {
							if (liquid.temperature > 0.7F && other.temperature < 0.55F || other.temperature > 0.7F && liquid.temperature < 0.55F) {
								liquids().remove(liquid, Math.min(liquids().get(liquid), 0.7F * Time.delta));
								if (Mathf.chanceDelta(0.20000000298023224)) {
									Fx.steam.at(fx, fy);
								}
							}
						} else {
							this.damageContinuous(1.0F);
							next.damageContinuous(1.0F);
							if (Mathf.chanceDelta(0.1)) {
								Fx.fire.at(fx, fy);
							}
						}
					}
				}
			}

		}
		return 0.0F;
	}
}
