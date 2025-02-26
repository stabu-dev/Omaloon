package omaloon.world.interfaces;

import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.type.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

/**
 * @author Liz
 */
public interface HasPressure extends Buildingc {
	/**
	 * @return true whenever this building will accept a certain amount of a certain fluid from another building
	 */
	default boolean acceptsPressurizedFluid(HasPressure from, @Nullable Liquid liquid, float amount) {
		return pressureConfig().acceptsPressure;
	}

	/**
	 * Adds a certain amount of a fluid into this module through the section.
	 */
	default void addFluid(@Nullable Liquid liquid, float amount) {
		if (amount == 0) return;
		if (amount < 0) pressure().section.removeFluid(liquid, -amount);
		pressure().section.addFluid(liquid, amount);
	}

	/**
	 * @return true if both buildings are connected to each other.
	 */
	default boolean connected(HasPressure to) {
		return connects(to) && to.connects(this);
	}

	/**
	 * @return true if this building connects to another one.
	 */
	default boolean connects(HasPressure to) {
		return pressureConfig().outputsPressure || pressureConfig().acceptsPressure;
	}

	/**
	 * @return building destination to find pressure sections / transfer liquids.
	 * @see mindustry.world.blocks.liquid.LiquidJunction.LiquidJunctionBuild liquid junction's getLiquidDestination
	 */
	default HasPressure getPressureDestination(HasPressure from, float pressure) {
		return this;
	}

	/**
	 * Returns the building whose section should be the same as this build's section.
	 */
	default @Nullable HasPressure getSectionDestination(HasPressure from) {
		if (pressureConfig().fluidGroup == null || pressureConfig().fluidGroup == FluidGroup.unset || pressureConfig().fluidGroup != from.pressureConfig().fluidGroup)
			return null;
		return this;
	}

	/**
	 * Returns the next builds that this block will connect to
	 */
	default Seq<HasPressure> nextBuilds() {
		return proximity().select(
			b -> b instanceof HasPressure
		).<HasPressure>as().map(
			b -> b.getPressureDestination(this, 0)
		).removeAll(
			b -> !connected(b) && proximity().contains((Building) b) || !pressureConfig().isAllowed(b.block())
		);
	}

	/**
	 * Returns true whenever this building outputs a certain amount of fluid to another building.
	 */
	default boolean outputsPressurizedFluid(HasPressure to, @Nullable Liquid liquid, float amount) {
		return pressureConfig().outputsPressure;
	}

	PressureModule pressure();
	PressureConfig pressureConfig();

	/**
	 * Removes a certain amount of a fluid into this module through the section.
	 */
	default void removeFluid(@Nullable Liquid liquid, float amount) {
		if (amount == 0) return;
		if (amount < 0) pressure().section.addFluid(liquid, -amount);
		pressure().section.removeFluid(liquid, amount);
	}

	/**
	 * Method to update pressure related things.
	 */
	default void updatePressure() {
		if (nextBuilds().contains(other -> other.pressure().section != pressure().section))
			pressure().section.updateTransfer();

		Vars.content.liquids().each(liquid -> {
			if (pressure().getPressure(liquid) < pressureConfig().minPressure - 1f)
				damage(pressureConfig().underPressureDamage);
			if (pressure().getPressure(liquid) > pressureConfig().maxPressure + 1f)
				damage(pressureConfig().overPressureDamage);
		});
	}
}
