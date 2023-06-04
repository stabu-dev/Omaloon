package ol.world.meta;

import arc.Core;
import mindustry.gen.Tex;
import mindustry.world.meta.*;
import ol.pressure.capacity.PressureCapacity;

public class OlStat {
    public static StatValue omaloonPressure(float min, float max) {
        return (table) -> {
            table.add("[accent] -" + PressureCapacity.getBarPressure(min) +
                    ' ' + Core.bundle.format("ol.op.val"));
            table.table(t -> {
                t.add("i");
                t.background(Tex.button);
            }).tooltip(Core.bundle.format("ol.op.info.min")).size(15).pad(4);
            table.add("[accent]" + PressureCapacity.getBarPressure(max) +
                    ' ' + Core.bundle.format("ol.op.val"));
            table.table(t -> {
                t.add("i");
                t.background(Tex.button);
            }).tooltip(Core.bundle.format("ol.op.info.max")).size(15).pad(4);
        };
    }

    public static final Stat
            magnetic = new Stat("magnetic"),
            damageSpread = new Stat("damage-spread"),
            pressure = new Stat("pressure");
}
