package omaloon.math;

import arc.math.*;

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
	public static float bernoulliFlowRate(float opening, float pressureStart, float pressureEnd, float density) {
		return opening * (pressureEnd - pressureStart) / density;
	}
	// TODO rename
}
