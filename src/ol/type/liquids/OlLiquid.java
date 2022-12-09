package ol.type.liquids;

import arc.graphics.Color;
import mindustry.type.Liquid;
import mindustry.world.meta.Stat;
import ol.type.items.OlItem;
import ol.world.meta.OlStat;

public class OlLiquid extends Liquid {
    public float magnetic = 0f;

    public OlLiquid(String name, Color color) {
        super(name, color);
    }

    @Override
    public void setStats(){
        stats.addPercent(OlStat.magnetic, magnetic);
        stats.addPercent(Stat.explosiveness, explosiveness);
        stats.addPercent(Stat.flammability, flammability);
        stats.addPercent(Stat.temperature, temperature);
        stats.addPercent(Stat.heatCapacity, heatCapacity);
        stats.addPercent(Stat.viscosity, viscosity);
    }
}
