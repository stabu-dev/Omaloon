package omaloon.content;

import arc.graphics.*;
import arc.struct.*;
import mindustry.content.*;
import mindustry.type.*;

public class OlItems{
    public static Item
        cobalt, carborundum,

    end;

    public static Seq<Item> glasmoreItems = new Seq<>();

    public static void load(){
        cobalt = new Item("cobalt", Color.valueOf("85939d")){{
            hardness = 1;
            cost = 0.5f;
        }};

        carborundum = new Item("carborundum", Color.valueOf("614a7f")){{
            cost = 0.7f;
        }};

        glasmoreItems.addAll(
            cobalt, carborundum, Items.beryllium, Items.coal, Items.graphite
        );
    }
}
