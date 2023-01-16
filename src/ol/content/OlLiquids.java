package ol.content;

import arc.graphics.*;

import mindustry.content.*;
import mindustry.type.*;

import ol.type.liquids.*;

public class OlLiquids{
	public static Liquid
			liquidOmalite,
			dalnii,
			liquidDalanii;

	public static void load() {
		liquidOmalite = new Liquid("liquid-omalite", Color.valueOf("c0ecff")){{
			viscosity = 0.50f;
			temperature = 0.05f;
			heatCapacity = 1.2f;

			barColor = Color.valueOf("c0ecff");
			effect = StatusEffects.freezing;
			lightColor = Color.valueOf("c0ecff").a(0.6f);
		}};

		dalnii = new Liquid("dalanii-gas", Color.valueOf("5e929d")){{
			gas = true;

			temperature = 0.4f;
			heatCapacity = 0.1f;

			coolant = false;
		}};

		liquidDalanii = new OlPolyLiquid("dalanii", Color.valueOf("5e929d")){{
			temperature = 0.1f;
			heatCapacity = 0.2f;

			coolant = false;

			effect = OlStatusEffects.slime;

			colorFrom = Color.valueOf("5e929d");
			colorTo = Color.valueOf("3e6067");

			canStayOn.add(Liquids.water);
		}};
	}
}
