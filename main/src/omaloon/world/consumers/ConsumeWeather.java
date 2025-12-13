package omaloon.world.consumers;

import arc.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import omaloon.world.meta.*;

public class ConsumeWeather extends Consume{
    public ObjectFloatMap<Weather> multipliers = new ObjectFloatMap<>();
    public float defaultValue = 0.25f;

    private static float e;

    @Override
    public void display(Stats stats){
        stats.add(Stat.booster, table -> {
            if(table.getCells().size > 0) table.getCells().peek().growX(); //Expand the spacer on the row above to push everything to the left
            table.row();
            table.table(weathers -> multipliers.each(weatherEntry -> {
                // TODO hide hidden weathers
                if (weatherEntry.key.unlockedNow()) table.table(Styles.grayPanel, weather -> {
                    weather.image(weatherEntry.key.uiIcon.found() ? weatherEntry.key.uiIcon : null).size(40f).pad(10f).left().scaling(Scaling.fit);
                    weather.add(weatherEntry.key.localizedName).left().grow();
                    weather.add((booster ? "*" : "+") + Core.bundle.format("stat.efficiency", OlStats.formatValue((weatherEntry.value - (booster ? -1 : 0)) * 100f, 2, false))).right().pad(10f).padRight(15f);
                }).growX().pad(5).row();
            })).growX().colspan(table.getColumns()).row();
        });
    }

    @Override
    public float efficiency(Building build){
        e = Mathf.num(booster);
        Groups.weather.each(w -> multipliers.containsKey(w.weather), w -> {
            if (booster) {
                e *= (1 + multipliers.get(w.weather, defaultValue));
            } else {
                e += multipliers.get(w.weather, defaultValue);
            }
        });

        return e;
    }

    @Override
    public float efficiencyMultiplier(Building build){
        return efficiency(build);
    }
}
