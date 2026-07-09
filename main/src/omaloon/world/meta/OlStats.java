package omaloon.world.meta;

import arc.util.*;
import mindustry.world.meta.*;
import omaloon.gen.*;

public class OlStats{
    public static StatUnit
    blocksCubed = new StatUnit("omaloon-blocks-cubed"),
    densityUnit = new StatUnit("omaloon-density-unit"),
    viscosityUnit = new StatUnit("omaloon-viscosity-unit"),
    pressureUnit = new StatUnit("omaloon-pressure-unit", "" + OlIconc.omaloonPressure),

    percentPerSecond = new StatUnit("omaloon-percent-per-second");

    public static StatCat pressure = new StatCat("omaloon-pressure");

    public static Stat
    space = new Stat("omaloon-space"),

    debris = new Stat("omaloon-debris"),

    density = new Stat("omaloon-density"),

    minPressure = new Stat("omaloon-min-pressure", pressure),
    maxPressure = new Stat("omaloon-max-pressure", pressure),

    pumpStrength = new Stat("omaloon-pump-strength"),
    pressureGradient = new Stat("omaloon-pressure-gradient"),

    minMaxSegments = new Stat("omaloon-min-max-segments");

    public static String formatValue(float value, int decimals, boolean addPlus){
        String format = Strings.autoFixed(Math.abs(value), decimals);
        return (value < 0 ? "-" : (addPlus ? "+" : "")) + format;
    }
}
