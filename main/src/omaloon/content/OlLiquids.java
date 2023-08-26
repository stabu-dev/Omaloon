package omaloon.content;

import mindustry.content.*;
import mindustry.type.*;
import omaloon.type.liquid.*;

import static arc.graphics.Color.*;

public class OlLiquids {
    public static Liquid
            dalani, tiredDalani,

    end;

    public static void load(){
        dalani = new CrystalLiquid("dalani", valueOf("5e929d")){{
            temperature = 0.1f;
            heatCapacity = 0.2f;

            coolant = false;

            colorFrom = valueOf("5e929d");
            colorTo = valueOf("3e6067");

            canStayOn.add(Liquids.water);
        }};

        tiredDalani = new Liquid("tired-dalani", valueOf("456c74")){{
            temperature = 0.1f;
            heatCapacity = 0.2f;

            coolant = false;

            canStayOn.add(Liquids.water);
        }};

    }

}
