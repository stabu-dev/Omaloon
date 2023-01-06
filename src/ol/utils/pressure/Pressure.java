package ol.utils.pressure;

import arc.*;
import ol.gen.*;

import mindustry.gen.*;

public class Pressure {
    public static int getPressureRendererProgress() {
        return Core.settings.getInt("mod.ol.pressureupdate");
    }

    public static float calculateWithCooldown(Building building) {
        return building instanceof PressureAblec p ? calculateWithCooldown(p) : 0F;
    }

    private static float calculateWithCooldown(PressureAblec pressureAble) {
        if(pressureAble.producePressure()) {
            return pressureAble.pressureThread();
        }

        if(pressureAble.downPressure()) {
            return -pressureAble.calculatePressureDown();
        }

        return 0F;
    }

    public static float calculatePressure(Building source) {
        if(source instanceof PressureAblec pr) {
            return Math.max(pr.net().sumf(Pressure::calculateWithCooldown), 0);
        }

        return 0;
    }
}