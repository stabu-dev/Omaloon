package omaloon.world.interfaces;

import arc.math.*;
import mindustry.gen.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

public interface HasPressure extends Buildingc {
	PressureModule pressure();
	PressureConfig pressureConfig();

	/**
	 * returns current pressure of the building
	 */
	default float getPressure() {
		return pressure().pressure;
	}
	/**
	 * returns current pressure mapped to a 0-1 range
	 */
	default float getPressureMap() {
		return Mathf.map(getPressure(), pressureConfig().minPressure, pressureConfig().maxPressure, 0, 1);
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
			case overPressure -> damage(1/60f * pressureConfig().overPressureDamageScl);
			case underPressure -> kill();
			default -> {}
		}
	}

	/**
	 * if something tries dumping into this, dump pressure into something else
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
		for(HasPressure other : proximity().copy().select(building -> building instanceof HasPressure).<HasPressure>as()) {
			other = other.getPressureDestination(this, 0);
			float diff = getPressure() - (getPressure() + other.getPressure())/2f;
			if (canDumpPressure(other, diff)) transferPressure(other, diff);
		}
	}
}
