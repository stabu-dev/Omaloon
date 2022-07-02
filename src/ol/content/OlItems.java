package ol.content;

import arc.graphics.Color;
import mindustry.type.Item;
import ol.type.items.OlItem;

public class OlItems {
	public static Item
		omalite, grumon, omaliteAlloy, magneticCombination, zarini, valkon;

    public static void load() {
        omalite = new OlItem("omalite", Color.valueOf("abcdef")) {{
            radioactivity = 0.70f;
            cost = 2;
            charge = 0.04f;
            hardness = 5;
            flammability = -0.10f;
            magnetic = 0.01f;
        }};
        grumon = new OlItem("grumon", Color.valueOf("5e5a90")) {{
            magnetic = 0.85f;
            cost = 2;
            hardness = 6;
        }};
        omaliteAlloy = new OlItem("omalite-alloy", Color.valueOf("00ffff")) {{
            radioactivity = 0.10f;
            cost = 2;
            charge = 0.02f;
            hardness = 8;
            flammability = -0.40f;
            magnetic = 0.08f;
        }};
        magneticCombination = new OlItem("magnetic-combination", Color.valueOf("9f849d")){{
            magnetic = 0.67f;
            cost = 3;
            hardness = 7;
            charge = 0.01f;
        }};
        zarini = new OlItem("zarini", Color.valueOf("59ae90")){{
           cost = 4;
           hardness = 3;
           magnetic = 0.09f;
           flammability = 0.05f;
           radioactivity = 0.14f;
        }};
        valkon = new OlItem("valkon", Color.valueOf("905452")){{
            cost = 5;
            hardness = 8;
            radioactivity = 0.012f;
            magnetic = 0.04f;
        }};
    }
}
