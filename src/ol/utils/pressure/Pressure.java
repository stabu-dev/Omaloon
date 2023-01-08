package ol.utils.pressure;

import mindustry.gen.*;
import ol.core.*;
import ol.gen.*;

public class Pressure{
    public static int getPressureRendererProgress(){
        return SettingsManager.pressureUpdate.get();
    }

    public static float calculateWithCooldown(Building building){
        return building instanceof PressureAblec p ? calculateWithCooldown(p) : 0F;
    }

    private static float calculateWithCooldown(PressureAblec pressureAble){
        if(pressureAble.producePressure()){
            return pressureAble.pressureThread();
        }

        if(pressureAble.downPressure()){
            return -pressureAble.calculatePressureDown();
        }

        return 0F;
    }
}