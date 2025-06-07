package omaloon.content;

import mindustry.type.*;

import static arc.graphics.Color.valueOf;

public class OlItems{
    public static Item cobalt, composite, nickel;

    public static void load(){
        cobalt = new Item("cobalt", valueOf("85939D"));
        nickel = new Item("nickel", valueOf("699B87"));
        composite = new Item("composite", valueOf("485674"));
    }
}
