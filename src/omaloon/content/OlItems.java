package omaloon.content;

import arc.graphics.Color;
import mindustry.type.*;

public class OlItems {
    public static Item
            cobalt,

    end;

    public static void load(){
        cobalt = new Item("cobalt", Color.valueOf("85939d")){{
            hardness = 1;
            cost = 0.5f;
            alwaysUnlocked = true;
        }};
    }
}
