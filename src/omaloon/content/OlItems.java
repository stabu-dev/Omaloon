package omaloon.content;

import arc.graphics.*;
import mindustry.type.*;

public class OlItems {
    public static Item
            cobalt, carborundum,

    end;

    public static void load(){
        cobalt = new Item("cobalt", Color.valueOf("85939d")){{
            hardness = 1;
            cost = 0.5f;
            alwaysUnlocked = true;
        }};

        carborundum = new Item("carborundum", Color.valueOf("614a7f")){{
            cost = 0.7f;
            alwaysUnlocked = true;
        }};
    }
}
