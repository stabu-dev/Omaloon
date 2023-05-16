package ol.content;

import arc.*;
import arc.graphics.*;
import arc.math.*;

import mindustry.*;
import mindustry.game.*;
import mindustry.type.*;

import ol.world.meta.*;

public class OlItems {
    public static Item
            grumon,
            magneticCombination,
            zarini,
            valkon,
            omalite,
            omaliteAlloy;

    public static void load() {
        grumon = new Item("grumon", Color.valueOf("5e5a90")) {{
            cost = 2;
            hardness = 6;
            charge = 0.98f;
        }};

        magneticCombination = new Item("magnetic-combination", Color.valueOf("9f849d")) {{
            cost = 3;
            hardness = 7;
            charge = 0.56f;
        }};

        zarini = new Item("zarini", Color.valueOf("59ae90")) {{
            cost = 4;
            hardness = 3;
            flammability = 0.05f;
            radioactivity = 0.14f;
        }};

        valkon = new Item("valkon", Color.valueOf("5e4841")) {{
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

        omaliteAlloy = new Item("omalite-alloy", Color.valueOf("adf0ff")) {{
            radioactivity = 0.10f;
            cost = 2;
            charge = 0.02f;
            hardness = 8;
            flammability = -0.40f;
        }};

        //Generate magnetic susceptibility for all items in game
        Events.on(EventType.ContentInitEvent.class, ignored ->
                Vars.content.items().each(element -> element.stats.addPercent(
                OlStat.magnetic, Math.max(Mathf.randomSeed(
                        element.id, (element.charge * 4
                                - element.flammability - element.radioactivity
                                - element.explosiveness
                        ) * element.id / 100
                ), 0))
        ));
    }
}