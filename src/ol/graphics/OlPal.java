package ol.graphics;

import arc.graphics.*;

public class OlPal {
    public static Color
            oLBlue = Color.valueOf("83c1ed"),
            oLDarkBlue = Color.valueOf("517d9d"),
            oLDalanite = Color.valueOf("a8d4ff"),
            oLPressureMin = Color.darkGray,
            oLPressure = Color.gray;

    //OLPressureMin -> OLPressure mixcol
    public static Color mixcol(Color colorFrom, Color colorTo, float progress) {
        if(progress == 0) {
            return colorFrom;
        }

        if(progress == 1) {
            return colorTo;
        }

        float reverse = 1f - progress;
        return new Color(
                colorFrom.r * reverse + colorTo.r * progress,
                colorFrom.g * reverse + colorTo.g * progress,
                colorFrom.b * reverse + colorTo.b * progress,
                colorFrom.a * reverse + colorTo.a * progress
        );
    }
}
