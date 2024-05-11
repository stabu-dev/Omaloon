package omaloon.world.blocks.sandbox;

import arc.*;
import arc.graphics.g2d.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.sandbox.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

import static arc.scene.ui.TextField.TextFieldFilter.*;
import static arc.util.Strings.*;

public class PressureLiquidSource extends LiquidSource {
	public PressureConfig pressureConfig = new PressureConfig();

	public PressureLiquidSource(String name) {
		super(name);
	}

	@Override
	public void drawPlanConfig(BuildPlan plan, Eachable<BuildPlan> list) {
		Draw.rect(crossRegion, plan.drawx(), plan.drawy());
		drawPlanConfigCenter(plan, plan.config, "center", false);
	}

	@Override
	public void load() {
		super.load();
		bottomRegion = Core.atlas.find(name + "-bottom");
		crossRegion = Core.atlas.find(name + "-cross");
	}

	@Override
	public void setBars() {
		super.setBars();
		pressureConfig.addBars(this);
	}

	public class PressureLiquidSourceBuild extends LiquidSourceBuild implements HasPressure {
		PressureModule pressure = new PressureModule();
		public float pressureTarget;
		public boolean negative;

		@Override public boolean acceptLiquid(Building source, Liquid liquid) {
			return false;
		}
		@Override public boolean acceptsPressure(HasPressure from, float pressure) {
			return false;
		}

		@Override
		public void buildConfiguration(Table table) {
			super.buildConfiguration(table);
			table.row();
			table.table(Styles.black6, cont -> {
				cont.field(pressureTarget + "", floatsOnly, s -> pressureTarget = parseFloat(s, 0f)).row();
				cont.check(Core.bundle.get("ol-source-negative"), b -> negative = b);
			}).growX();
		}

		@Override public PressureModule pressure() {
			return pressure;
		}
		@Override public PressureConfig pressureConfig() {
			return pressureConfig;
		}

		@Override
		public void read(Reads read, byte revision) {
			super.read(read, revision);
			pressure.read(read);
			pressureTarget = read.f();
			negative = read.bool();
		}

		@Override
		public void updateTile() {
			super.updateTile();
			pressure.pressure = pressureTarget * (negative ? -1f : 1f);
			nextBuilds(true).each(b -> b.pressure().pressure = pressureTarget * (negative ? -1 : 1f));
			dumpPressure();
		}

		@Override
		public void write(Writes write) {
			super.write(write);
			pressure.write(write);
			write.f(pressureTarget);
			write.bool(negative);
		}
	}
}
