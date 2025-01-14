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

		fluidCapacity = new Stat("omaloon-fluid-capacity", StatCat.liquids),

		density = new Stat("omaloon-density"),

		pressureFlow = new Stat("omaloon-pressureflow", pressure),

		pumpStrength = new Stat("omaloon-pump-strength", pressure),
		pressureGradient = new Stat("omaloon-pressure-gradient", pressure),

		maxPressure = new Stat("omaloon-max-pressure", pressure),
		minPressure = new Stat("omaloon-min-pressure", pressure),

		pressureRange = new Stat("omaloon-pressure-range", pressure),
		optimalPressure = new Stat("omaloon-optimal-pressure", pressure);

	public static final StatUnit
		blocksCubed = new StatUnit("omaloon-blocks-cubed"),

		densityUnit = new StatUnit("omaloon-density-unit", "\uC357"),
		viscosityUnit = new StatUnit("omaloon-viscosity-unit", "\uC357"),

		pressureUnit = new StatUnit("omaloon-pressure-unit", "\uC357"),
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
