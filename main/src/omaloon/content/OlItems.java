package omaloon.content;

import mindustry.type.*;

import static arc.graphics.Color.valueOf;

public class OlItems{
    public static Item cobalt, composite, nickel;

    public static void load(){
        cobalt = new Item("cobalt", valueOf("85939D"));
        composite = new Item("composite", valueOf("485674"));
        nickel = new Item("nickel", valueOf("699B87"));
    }
}
