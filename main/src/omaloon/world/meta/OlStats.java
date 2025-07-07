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

public class OlStats{
    public static StatUnit
    blocksCubed = new StatUnit("omaloon-blocks-cubed"),
    densityUnit = new StatUnit("omaloon-density-unit"),
    viscosityUnit = new StatUnit("omaloon-viscosity-unit"),
    pressureUnit = new StatUnit("omaloon-pressure-unit");

    public static StatCat pressure = new StatCat("omaloon-pressure");

    public static Stat
    density = new Stat("omaloon-density"),

    minPressure = new Stat("omaloon-min-pressure", pressure),
    maxPressure = new Stat("omaloon-max-pressure", pressure),

    pumpStrength = new Stat("omaloon-pump-strength"),
    pressureGradient = new Stat("omaloon-pressure-gradient");

    public static String formatValue(float value, int decimals, boolean addPlus){
        String format = Strings.autoFixed(Math.abs(value), decimals);
        return (value < 0 ? "-" : (addPlus ? "+" : "")) + format;
    }

    public static StatValue fluid(@Nullable Liquid liquid, float amount, float time, boolean showContinuous){
        return table -> {
            table.table(display -> {
                display.add(new Stack(){{
                    add(new Image(liquid != null ? liquid.uiIcon : Core.atlas.find("omaloon-pressure-icon")).setScaling(Scaling.fit));

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
        };
    }

    public static StatValue number(float value, StatUnit unit, boolean merge){
        return table -> {
            String l1 = (unit.icon == null ? "" : unit.icon + " ") + formatValue(value, 2, false), l2 = (unit.space ? " " : "") + unit.localized();

            if(merge){
                table.add(l1 + l2).left();
            }else{
                table.add(l1).left();
                table.add(l2).left();
            }
        };
    }
}
