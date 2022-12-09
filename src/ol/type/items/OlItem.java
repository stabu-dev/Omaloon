package ol.type.items;

import arc.graphics.Color;
import mindustry.ctype.UnlockableContent;
import mindustry.type.Item;
import mindustry.world.meta.Stat;
import ol.world.meta.OlStat;

public class OlItem extends Item {
    public float magnetic = 0f;

    public OlItem(String name, Color color) {
        super(name, color);
    }

    @Override
    public void setStats(){
        stats.addPercent(OlStat.magnetic, magnetic);
        stats.addPercent(Stat.explosiveness, explosiveness);
        stats.addPercent(Stat.flammability, flammability);
        stats.addPercent(Stat.radioactivity, radioactivity);
        stats.addPercent(Stat.charge, charge);
    }
}
