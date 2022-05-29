package Ol.content;

import arc.graphics.Color;
import mindustry.content.StatusEffects;
import mindustry.type.Liquid;

public class OlLiquids{
	public static Liquid
		liquidOmalite, test;
	public static void load() {
		liquidOmalite = new Liquid("liquid-omalite", Color.valueOf("c0ecff")){{
			viscosity = 0.50f;
			temperature = 0.05f;
			heatCapacity = 1f;
			barColor = Color.valueOf("c0ecff");
			effect = StatusEffects.freezing;
			lightColor = Color.valueOf("c0ecff").a(0.6f);
		}};
	}
}
