package omaloon.math;

public class OlMath {
	/**
	 * Solves for the flow of a fluid through an area based on a difference of pressure.
	 *
	 * area is in world units squared.
	 * pressureStart and pressureEnd are in pascals
	 * density is in liquid units / world units cubed
	 * time is in ticks
	 *
	 * returns the amount of fluid in liquid units that passes through the area over a certain time.
	 */
	public static float flowRate(float pressureStart, float pressureEnd, float capacityStart, float capacityEnd, float density, float viscosity) {
		return
			(
				capacityStart * (
					pressureStart * (
						capacityStart + capacityEnd
					) - (
						pressureEnd * capacityEnd + pressureStart * capacityStart
					)
				)
			) / (
				density * (
					capacityStart + capacityEnd
				) * viscosity
			);
	}
}
