package ol.content;

import arc.*;
import arc.graphics.*;
import arc.math.*;

import mindustry.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.type.*;

import ol.type.liquids.PoligonalLiquid;
import ol.world.meta.*;

public class OlLiquids {
    public static Liquid
            liquidOmalite,
            angeirum,
            dalanii;

    public static void load() {
        liquidOmalite = new Liquid("liquid-omalite", Color.valueOf("c0ecff")){{
            viscosity = 0.50f;
            temperature = 0.05f;
            heatCapacity = 1.2f;
            barColor = Color.valueOf("c0ecff");
            effect = StatusEffects.freezing;
            lightColor = Color.valueOf("c0ecff").a(0.6f);
        }};

        angeirum = new Liquid("angeirum", Color.valueOf("5e929d")){{
            gas = true;
            temperature = 0.4f;
            heatCapacity = 0.1f;
            coolant = false;
        }};

        dalanii = new PoligonalLiquid("dalanii", Color.valueOf("5e929d")){{
            temperature = 0.1f;
            heatCapacity = 0.2f;
            coolant = false;
            effect = OlStatusEffects.slime;
            colorFrom = Color.valueOf("5e929d");
            colorTo = Color.valueOf("3e6067");
            canStayOn.add(Liquids.water);
        }};

        //Generate magnetic susceptibility for all liquids in game
        Events.on(EventType.ContentInitEvent.class, ignored ->
                Vars.content.liquids().each(element -> element.stats.addPercent(
                OlStat.magnetic, Math.max(Mathf.randomSeed(
                        element.id, (element.viscosity * 4
                                - element.temperature - element.flammability
                                - element.explosiveness - element.heatCapacity
                        ) * element.id / 100
                ), 0))
        ));
    }
}
