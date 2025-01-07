package omaloon.content;

import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.type.*;
import omaloon.type.liquid.*;
import omaloon.world.meta.*;

import static arc.graphics.Color.*;

public class OlLiquids {
    public static Liquid
            glacium, tiredGlacium,

    end;

    public static ObjectFloatMap<Liquid> densities = new ObjectFloatMap<>();

    public static void load(){
        glacium = new CrystalLiquid("glacium", valueOf("5e929d")){{
            effect = OlStatusEffects.glacied;
            temperature = 0.1f;
            heatCapacity = 0.2f;

            coolant = false;

            colorFrom = valueOf("5e929d");
            colorTo = valueOf("3e6067");

            canStayOn.add(Liquids.water);
        }};

        tiredGlacium = new Liquid("tired-glacium", valueOf("456c74")){{
            effect = OlStatusEffects.glacied;
            temperature = 0.1f;
            heatCapacity = 0.2f;

            coolant = false;

            canStayOn.add(Liquids.water);
        }};

        addDensity(Liquids.water, 1000);
				addDensity(Liquids.slag, 1600);
				addDensity(Liquids.oil, 700);
				addDensity(Liquids.cryofluid, 200);
				addDensity(glacium, 1300);
				addDensity(tiredGlacium, 1300);
    }

		public static void addDensity(Liquid liquid, float density) {
			densities.put(liquid, density);
			liquid.stats.add(OlStats.density, density, OlStats.liquidPerWorldUnit);
		}

		public static float getDensity(@Nullable Liquid liquid) {
			return densities.get(liquid, 1.2f);
		}
}
