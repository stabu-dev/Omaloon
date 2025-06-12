package omaloon.content;

import arc.struct.*;
import mindustry.type.*;
import omaloon.type.liquid.*;

import static arc.graphics.Color.valueOf;
import static mindustry.content.Liquids.water;

public class OlLiquids{
    public static Liquid
    glacium, tiredGlacium;

    public static ObjectFloatMap<Liquid> densities = new ObjectFloatMap<>(), viscosities = new ObjectFloatMap<>();

    public static float getDensity(Liquid liquid) {
        return densities.get(liquid, 1/8f);
    }
    public static float getViscosity(Liquid liquid) {
        return viscosities.get(liquid, 1f);
    }

    public static void load(){
        glacium = new CrystalLiquid("glacium", valueOf("5e929d")){{
            effect = OlStatusEffects.glacied;
            temperature = 0.1f;
            heatCapacity = 0.2f;
            densities.put(this, 1/8f);
            viscosities.put(this, 1f);

            coolant = false;

            colorFrom = valueOf("5e929d");
            colorTo = valueOf("3e6067");

            canStayOn.add(water);
        }};

        tiredGlacium = new Liquid("tired-glacium", valueOf("456c74")){{
            effect = OlStatusEffects.glacied;
            temperature = 0.1f;
            heatCapacity = 0.2f;
            densities.put(this, 1/8f);
            viscosities.put(this, 1f);

            coolant = false;

            canStayOn.add(water);
        }};
    }
}
