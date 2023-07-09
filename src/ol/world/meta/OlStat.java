package ol.world.meta;

import arc.*;
import arc.scene.ui.layout.*;

import mindustry.gen.*;
import mindustry.world.meta.*;

public class OlStat {
    //public static StatValue omaloonPressure(float min, float max) {
    //    return table -> {
    //        addStat(table, , "ol.op.info.min");
    //        addStat(table, , "ol.op.info.max");
    //    };
    //}

    private static void addStat(Table table, float pressure, String bundleKey) {
        table.add("[accent] -" + pressure + ' ' + Core.bundle.format("ol.op.val"));
        table.table(t -> {
            t.add("i");
            t.background(Tex.button);
        }).tooltip(Core.bundle.format(bundleKey)).size(15).pad(4);
    }

    public static final Stat
            magnetic = new Stat("magnetic"),
            damageSpread = new Stat("damage-spread"),
            pressure = new Stat("pressure");
}
