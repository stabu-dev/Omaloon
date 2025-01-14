package omaloon.content;

import arc.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.type.*;
import mindustry.world.meta.*;
import omaloon.type.liquid.*;
import omaloon.world.meta.*;

import static arc.graphics.Color.*;

public class OlLiquids {
    public static Liquid
            glacium, tiredGlacium,

    end;

    public static ObjectFloatMap<Liquid> densities = new ObjectFloatMap<>();
    public static ObjectFloatMap<Liquid> viscosities = new ObjectFloatMap<>();

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
				addViscosity(Liquids.water, 10);
				addDensity(Liquids.slag, 1600);
				addViscosity(Liquids.slag, 250);
				addDensity(Liquids.oil, 700);
				addViscosity(Liquids.oil, 50);
				addDensity(Liquids.cryofluid, 200);
				addViscosity(Liquids.cryofluid, 1.2f);
				addDensity(glacium, 1300);
				addViscosity(glacium, 13);
				addDensity(tiredGlacium, 1300);
	      addViscosity(tiredGlacium, 13);
    }

		public static void addDensity(Liquid liquid, float density) {
			densities.put(liquid, density);
			liquid.stats.add(OlStats.density, density, OlStats.densityUnit);
		}

		public static void addViscosity(Liquid liquid, float viscosity) {
			viscosities.put(liquid, viscosity);
			liquid.stats.remove(Stat.viscosity);
			liquid.stats.add(Stat.viscosity, Strings.autoFixed(viscosity/60f, 2), OlStats.viscosityUnit);
		}

		public static float getDensity(@Nullable Liquid liquid) {
			return densities.get(liquid, 1f);
		}

		public static float getViscosity(@Nullable Liquid liquid) {
			return viscosities.get(liquid, 1f);
		}
}
