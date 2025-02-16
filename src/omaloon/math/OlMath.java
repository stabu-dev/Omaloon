package omaloon.math;

public class OlMath{
    /**
     * Solves for the flow of a fluid through an area based on a difference of pressure.
     * <ul>
     * <li> pressureStart is the pressure at the back
     * <li> pressureEnd is the pressure at the front
     * <li> capacityStart is the fluid capacity at the back
     * <li> capacityEnd is the fluid capacity at the front
     * <li> density is the fluid density
     * <li> viscosity is the fluid viscosity
     * </ul>
     * <p>
     * returns the amount of fluid in liquid units that flows per tick
     */
    public static float flowRate(float pressureStart, float pressureEnd, float capacityStart, float capacityEnd, float density, float viscosity){
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
