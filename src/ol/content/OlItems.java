package ol.content;

import arc.Events;
import arc.graphics.Color;
import arc.math.Mathf;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.game.EventType;
import mindustry.type.Item;
import mindustry.world.meta.StatValues;
import ol.world.meta.OlStat;

public class OlItems {
	public static Item
            grumon,
            magneticCombination,
            zarini,
            valkon,
            omalite,
            omaliteAlloy;

    public static final Seq<Item>
            omaloonItems = new Seq<>(),
            omaloonOnlyItems = new Seq<>();

    public static void load() {
        grumon = new Item("grumon", Color.valueOf("5e5a90")) {{
            cost = 2;
            hardness = 6;
            charge = 0.98f;
        }};

        magneticCombination = new Item("magnetic-combination", Color.valueOf("9f849d")){{
            cost = 3;
            hardness = 7;
            charge = 0.56f;
        }};

        zarini = new Item("zarini", Color.valueOf("59ae90")){{
           cost = 4;
           hardness = 3;
           flammability = 0.05f;
           radioactivity = 0.14f;
        }};

        valkon = new Item("valkon", Color.valueOf("905452")){{
            cost = 5;
            hardness = 8;
            radioactivity = 0.012f;
            charge = 0.78f;
        }};

        omalite = new Item("omalite", Color.valueOf("abcdef")) {{
            radioactivity = 0.70f;
            cost = 2;
            hardness = 5;
            flammability = -0.10f;
        }};

        omaliteAlloy = new Item("omalite-alloy", Color.valueOf("00ffff")) {{
            radioactivity = 0.10f;
            cost = 2;
            charge = 0.02f;
            hardness = 8;
            flammability = -0.40f;
        }};

        omaloonItems.addAll(
                grumon, omalite, zarini, valkon, magneticCombination, omaliteAlloy
        );

        omaloonOnlyItems.removeAll(Items.serpuloItems);
        omaloonOnlyItems.removeAll(Items.erekirItems);
        omaloonOnlyItems.addAll(omaloonItems);

        //Generate magnetic susceptibility for all items and liquids
        Events.on(EventType.ContentInitEvent.class, ignored -> {
            Vars.content.items().each(element -> {
                element.stats.addPercent(
                        OlStat.magnetic,

                        Math.max(
                                Mathf.randomSeed(
                                        element.id,

                                        (
                                                element.charge * 4
                                                        - element.flammability
                                                        - element.radioactivity
                                                        - element.explosiveness

                                        ) * element.id / 100
                                ),

                                0
                        )
                );
            });

            Vars.content.liquids().each(element -> {
                element.stats.addPercent(
                        OlStat.magnetic,

                        Math.max(
                                Mathf.randomSeed(
                                        element.id,

                                        (
                                                element.viscosity * 4
                                                        - element.temperature
                                                        - element.flammability
                                                        - element.explosiveness
                                                        - element.heatCapacity
                                        ) * element.id / 100
                                ),

                                0
                        )
                );
            });
        });
    }
}
