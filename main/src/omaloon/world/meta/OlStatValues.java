package omaloon.world.meta;

import arc.*;
import arc.graphics.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.meta.*;

import static mindustry.Vars.iconMed;

public class OlStatValues{

    public static StatValue fluid(@Nullable Liquid liquid, float amount, float time, boolean showContinuous){
        return table ->
        table.table(display -> {
            display.add(new Stack(){{
                add(new Image(liquid != null ? liquid.uiIcon : Core.atlas.find("omaloon-air")).setScaling(Scaling.fit));

                if(amount * 60f / time != 0){
                    Table t = new Table().left().bottom();
                    t.add(Strings.autoFixed(amount * 60f / time, 2)).style(Styles.outlineLabel);
                    add(t);
                }
            }}).size(iconMed).padRight(3 + (amount * 60f / time != 0 && Strings.autoFixed(amount * 60f / time, 2).length() > 2 ? 8 : 0));

            if(showContinuous){
                display.add(StatUnit.perSecond.localized()).padLeft(2).padRight(5).color(Color.lightGray).style(Styles.outlineLabel);
            }

            display.add(liquid != null ? liquid.localizedName : "@air");
        });
    }

    public static StatValue range(float min, float max){
        return table -> table.add(Core.bundle.format("stat.omaloon-range-format", (int)min, (int)max));
    }
}
