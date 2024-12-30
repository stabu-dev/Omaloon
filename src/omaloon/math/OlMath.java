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
	public static float bernoulliFlowRate(float area, float pressureStart, float pressureEnd, float density, float time) {
		float diff = -2f * (pressureEnd - pressureStart);
		if (diff == 0) return 0f;
		return (area * (diff/Math.abs(diff)) * Mathf.sqrt(Math.abs(diff) / density) * time * density)/(60f);
	}
}
