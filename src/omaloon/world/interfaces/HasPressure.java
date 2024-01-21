package omaloon.world.interfaces;

import arc.graphics.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
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

	default Seq<HasPressure> nextBuilds() {
		return proximity().select(b -> b instanceof HasPressure).as();
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
		float
			frac = Mathf.map(getPressure(), pressureConfig().minPressure, pressureConfig().maxPressure, 0, 1),
			ofrac = Mathf.map(to.getPressure(), to.pressureConfig().minPressure, to.pressureConfig().maxPressure, 0, 1);
		return 1f + Mathf.clamp(frac - ofrac, 0.3f, 0.6f);
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
		for (HasPressure other : proximity().copy().select(building -> building instanceof HasPressure).<HasPressure>as()) {
			if (canDumpPressure(other, 0)) {
				other = other.getPressureDestination(this, 0);
				float diff = getPressure() - (getPressure() + other.getPressure()) / 2f;
				transferPressure(other, diff);
			}
		}
	}

	//
	default float moveLiquidPressure(HasPressure next, Liquid liquid) {
		if (next != null) {
			next = (HasPressure) next.getLiquidDestination(as(), liquid);
			if (next.team() == team() && next.block().hasLiquids && liquids().get(liquid) > 0.0F) {
				float ofract = next.liquids().get(liquid) / next.block().liquidCapacity;
				float fract = liquids().get(liquid) / block().liquidCapacity * getPressureFlow(next);
				float flow = Math.min(Mathf.clamp(fract - ofract) * block().liquidCapacity, liquids().get(liquid));
				flow = Math.min(flow, next.block().liquidCapacity - next.liquids().get(liquid));
				if (flow > 0.0F && ofract <= fract && next.acceptLiquid(this.as(), liquid)) {
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
