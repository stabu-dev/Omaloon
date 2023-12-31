package omaloon.world.interfaces;

import mindustry.gen.*;
import omaloon.world.blocks.meta.*;
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
	 * can receive/send pressure to another place
	 */
	default boolean acceptsPressure(HasPressure from, float pressure) {
		return getPressure() + pressure <= from.getPressure() - pressure;
	}
	default boolean canDumpPressure(HasPressure to, float pressure) {
		return to.getPressure() + pressure <= getPressure() - pressure;
	}

	/**
	 * if something tries dumping into this, dump pressure into something else
	 */
	default HasPressure getPressureDestination(HasPressure from) {
		return this;
	}

	/**
	 * transfers pressure between 2 buildings
	 */
	default void transferPressure(HasPressure to, float pressure) {
		if (to.acceptsPressure(this, pressure)) {
			pressure().pressure -= pressure;
			to.pressure().pressure += pressure;
		}
	}

	/**
	 * dumps pressure onto available builds
	 */
	default void dumpPressure() {
		for(HasPressure other : proximity().copy().select(building -> building instanceof HasPressure).<HasPressure>as()) {
			other = other.getPressureDestination(this);
			float diff = getPressure() - (getPressure() + other.getPressure())/2f;
			if (canDumpPressure(other, diff)) transferPressure(other, diff);
		}
	}
}
