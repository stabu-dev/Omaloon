package ol.utils.pressure;

import arc.*;
import ol.world.blocks.pressure.*;

import mindustry.gen.*;

public class Pressure {
    public static int getPressureRendererProgress() {
        return Core.settings.getInt("mod.ol.pressureupdate");
    }

    public static float calculateWithCooldown(Building building) {
        return building instanceof PressureAble<?> p ? calculateWithCooldown(p) : 0F;
    }

    private static float calculateWithCooldown(PressureAble<?> pressureAble) {
        if(pressureAble.producePressure()) {
            return pressureAble.pressureThread();
        }

        if(pressureAble.downPressure()) {
            return -pressureAble.calculatePressureDown();
        }

        return 0F;
    }

    public static float calculatePressure(Building source) {
        if(source instanceof PressureAble<?> pr) {
            return Math.max(pr.net().sumf(Pressure::calculateWithCooldown), 0);
        }

        return 0;
    }
}