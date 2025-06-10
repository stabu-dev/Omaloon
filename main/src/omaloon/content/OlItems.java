package omaloon.content;

import arc.struct.*;
import mindustry.type.*;

import static arc.graphics.Color.*;
import static mindustry.content.Items.*;

public class OlItems{
    public static Item
    cobalt, composite, nickel;

    public static Seq<Item>
    glasmoreItems = new Seq<>();

    public static void load(){
        cobalt = new Item("cobalt", valueOf("85939D"));
        nickel = new Item("nickel", valueOf("699B87"));
        composite = new Item("composite", valueOf("485674"));

        glasmoreItems.addAll(
        cobalt, nickel, composite, coal, graphite
        );
    }
}
