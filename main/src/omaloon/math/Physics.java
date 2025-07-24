package omaloon.math;

public class Physics{
    /**
     * @return the amount of a fluid that flows between 2 tanks.
     */
    public static float fluidFlow(
    float fromPressure, float fromVolume,
    float toPressure, float toVolume,
    float density, float viscosity,
    float timeScl
    ){
        float flow = toVolume * fromPressure;
        flow += fromVolume * toPressure;
        flow /= (fromVolume + toVolume);
        flow -= fromPressure;
        flow *= fromVolume;
        flow *= density;
        flow /= Math.max(1, viscosity / timeScl);

        return flow;
    }
}
