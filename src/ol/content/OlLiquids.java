package ol.content;

import arc.graphics.Color;
import mindustry.content.StatusEffects;
import mindustry.type.Liquid;
import ol.type.liquids.OlLiquid;
import ol.type.liquids.OlPolyLiquid;

public class OlLiquids{
	public static Liquid
		liquidOmalite, dalanii;
	public static void load() {
		liquidOmalite = new OlLiquid("liquid-omalite", Color.valueOf("c0ecff")){{
			viscosity = 1f;
			temperature = 0.05f;
			heatCapacity = 2f;
			magnetic = 0.01f;
			barColor = Color.valueOf("c0ecff");
			effect = StatusEffects.freezing;
			lightColor = Color.valueOf("c0ecff").a(0.6f);
		}};

		dalanii = new OlPolyLiquid("dalanii", Color.valueOf("5e929d")){{
			temperature = 0.1f;
			heatCapacity = 0.2f;
			coolant = false;
			effect = OlStatusEffects.slime;
			colorFrom = Color.valueOf("5e929d");
			colorTo = Color.valueOf("3e6067");
		}};
	}
}
