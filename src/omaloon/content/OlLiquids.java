package omaloon.content;

import arc.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.type.*;
import mindustry.world.meta.*;
import omaloon.type.liquid.*;
import omaloon.world.meta.*;

import static arc.graphics.Color.valueOf;

public class OlLiquids{
    public static Liquid
    glacium, tiredGlacium,

    end;

    public static ObjectFloatMap<Liquid> densities = new ObjectFloatMap<>();
    public static ObjectFloatMap<Liquid> viscosities = new ObjectFloatMap<>();

    public static void load(){
        glacium = new CrystalLiquid("glacium", valueOf("5e929d")){{
            effect = OlStatusEffects.glacied;
            temperature = 0.1f;
            heatCapacity = 0.2f;

            coolant = false;

            colorFrom = valueOf("5e929d");
            colorTo = valueOf("3e6067");

            canStayOn.add(Liquids.water);
        }};

        tiredGlacium = new Liquid("tired-glacium", valueOf("456c74")){{
            effect = OlStatusEffects.glacied;
            temperature = 0.1f;
            heatCapacity = 0.2f;

            coolant = false;

            canStayOn.add(Liquids.water);
        }};

        addDensity(Liquids.water, 1000);
        addViscosity(Liquids.water, 10);
        addDensity(Liquids.slag, 1600);
        addViscosity(Liquids.slag, 250);
        addDensity(Liquids.oil, 700);
        addViscosity(Liquids.oil, 50);
        addDensity(Liquids.cryofluid, 200);
        addViscosity(Liquids.cryofluid, 1.2f);
        addDensity(glacium, 1300);
        addViscosity(glacium, 13);
        addDensity(tiredGlacium, 1300);
        addViscosity(tiredGlacium, 13);

        if(Core.settings.getBool("omaloon-display-liquid-stats", true)) Events.on(EventType.ContentInitEvent.class, e -> {
            densities.each(map -> {
                map.key.stats.add(OlStats.density, map.value, OlStats.densityUnit);
            });
            viscosities.each(map -> {
                map.key.checkStats();
                map.key.stats.remove(Stat.viscosity);
                map.key.stats.add(Stat.viscosity, Core.bundle.get("stat.omaloon-viscosity"), map.key.viscosity * 100f, Strings.autoFixed(map.value / 60f, 2) + " " + OlStats.viscosityUnit.localized());
            });
        });
    }

    public static void addDensity(Liquid liquid, float density){
        densities.put(liquid, density);
    }

    public static void addViscosity(Liquid liquid, float viscosity){
        viscosities.put(liquid, viscosity);
    }

    public static float getDensity(@Nullable Liquid liquid){
        return densities.get(liquid, 8f);
    }

    public static float getViscosity(@Nullable Liquid liquid){
        return viscosities.get(liquid, 1f);
    }
}
