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
        float flow = toVolume * toPressure;
        flow += fromVolume * fromPressure;
        flow /= (fromVolume + toVolume);
        flow -= fromPressure;
        flow *= -1f;
        flow *= fromVolume;
        flow *= density;
        flow /= Math.max(1, viscosity / timeScl);

        return flow;
    }
}
