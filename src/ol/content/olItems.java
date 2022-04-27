package ol.content;

import arc.graphics.Color;
import mindustry.type.Item;

public class olItems{
	public static Item
		omalite, omaliteAlloy;

    public static void load(){
    omalite = new Item("omalite", Color.valueOf("abcdef")) {{
           radioactivity = 0.70f;
           cost = 2;
           charge = 0.04f;
           hardness = 5;
           flammability = -0.10f;
			     explosiveness = 0f;
    }};
    omaliteAlloy = new Item("omalite-alloy", Color.valueOf("00ffff")) {{
                radioactivity = 0.10f;
                cost = 2;
                charge = 0.02f;
                hardness = 8;
                flammability = -0.40f;
			          explosiveness = 0f;
	      }};

	}
}
