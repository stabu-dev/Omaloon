package omaloon.world.meta;

import arc.*;
import arc.graphics.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.meta.*;

import static mindustry.Vars.*;

public class OlStats {
	public static final StatCat pressure = new StatCat("omaloon-pressure");

	public static final Stat
		minSpeed = new Stat("omaloon-min-speed"),
		maxSpeed = new Stat("omaloon-max-speed"),

		addFluid = new Stat("omaloon-add-fluid", StatCat.crafting),
		removeFluid = new Stat("omaloon-remove-fluid", StatCat.crafting),

		density = new Stat("omaloon-density"),

		pressureFlow = new Stat("omaloon-pressureflow", pressure),

		maxPressure = new Stat("omaloon-maxPressure", pressure),
		minPressure = new Stat("omaloon-minPressure", pressure),
		consumePressure = new Stat("omaloon-consumePressure", pressure),
		pressureRange = new Stat("omaloon-pressurerange", pressure),
		optimalPressure = new Stat("omaloon-optimal-pressure", pressure),
		outputPressure = new Stat("omaloon-outputPressure", pressure);

	public static final StatUnit
		liquidPerWorldUnit = new StatUnit("omaloon-liquidPerWorldUnit"),

		pressureUnits = new StatUnit("omaloon-pressureUnits", "\uC357"),
		pressureSecond = new StatUnit("omaloon-pressureSecond", "\uC357");

	public static StatValue fluid(@Nullable Liquid liquid, float amount, float time, boolean showContinuous) {
		return table -> {
			table.table(display -> {
				display.add(new Stack() {{
					add(new Image(liquid != null ? liquid.uiIcon : Core.atlas.find("omaloon-pressure-icon")).setScaling(Scaling.fit));

					if (amount * 60f/time != 0) {
						Table t = new Table().left().bottom();
						t.add(Strings.autoFixed(amount * 60f/time, 2)).style(Styles.outlineLabel);
						add(t);
					}
				}}).size(iconMed).padRight(3 + (amount * 60f/time != 0 && Strings.autoFixed(amount * 60f/time, 2).length() > 2 ? 8 : 0));

				if(showContinuous){
					display.add(StatUnit.perSecond.localized()).padLeft(2).padRight(5).color(Color.lightGray).style(Styles.outlineLabel);
				}

				display.add(liquid != null ? liquid.localizedName : "@air");
			});
		};
	}
}
