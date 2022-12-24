package ol.world.meta;

import mindustry.world.meta.*;

public class OlStat {

    public static final Stat
            magnetic = new Stat("magnetic"),
            damageSpread = new Stat("damage-share", StatCat.general),
            tier = new Stat("tier", OlStatCat.pressure),
            pressureConsume = new Stat("pressure-consume", OlStatCat.pressure),
            pressureProduction = new Stat("pressure-production", OlStatCat.pressure),
            maxPressure = new Stat("max-pressure", OlStatCat.pressure),
            requirements = new Stat("requirements", StatCat.crafting);
    //averageSurfaceTemperature = new Stat("average-surface-temperature"),
    //rad = new Stat("radius"),
    //orbitRad = new Stat("orbit-radius");
}
