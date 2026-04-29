package omaloon.content;

import arc.struct.*;
import mindustry.type.*;

import static arc.graphics.Color.valueOf;
import static mindustry.content.Items.*;

public class OlItems{
    public static Item
    cobalt, composite, nickel, magnetite, quartzLens, quartzSand;

    public static Seq<Item>
    glasmoreItems = new Seq<>();

    public static void load(){
        cobalt = new Item("cobalt", valueOf("85939D")){{
            hardness = 1;
        }};
        nickel = new Item("nickel", valueOf("699B87")){{
            hardness = 2;
        }};
        composite = new Item("composite", valueOf("485674"));
        magnetite = new Item("magnetite", valueOf("444444")){{
            hidden = true;
        }};

        quartzLens = new Item("quartz-lens");
        quartzSand = new Item("quartz-sand");

        glasmoreItems.addAll(
        cobalt, nickel, composite, coal, graphite, magnetite
        );
    }
}
