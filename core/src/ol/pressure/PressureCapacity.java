package ol.pressure;

public class PressureCapacity implements IPressureCapacity {
    public static float BAR_DISPLAY_LIMIT = 0.005f;

    public static int getBarPressure(float pressure) {
        return (int) (Math.floor(pressure) * BAR_DISPLAY_LIMIT);
    }
}