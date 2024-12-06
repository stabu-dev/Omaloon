package omaloon.world.blocks.liquid;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.blocks.sandbox.*;
import omaloon.ui.elements.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

public class PressureLiquidGauge extends LiquidBlock {
	public PressureConfig pressureConfig = new PressureConfig();

	public Color maxColor = Color.white, minColor = Color.white;

	public TextureRegion[] tileRegions;
	public TextureRegion gaugeRegion;

	public PressureLiquidGauge(String name) {
		super(name);
		rotate = true;
	}

	@Override
	public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
		var tiling = new Object() {
			int tiling = 0;
		};
		Point2
			front = new Point2(1, 0).rotate(plan.rotation).add(plan.x, plan.y),
			back = new Point2(-1, 0).rotate(plan.rotation).add(plan.x, plan.y);

		boolean inverted = plan.rotation == 1 || plan.rotation == 2;
		list.each(next -> {
			if (new Point2(next.x, next.y).equals(front) && next.block.outputsLiquid) tiling.tiling |= inverted ? 2 : 1;
			if (new Point2(next.x, next.y).equals(back) && next.block.outputsLiquid) tiling.tiling |= inverted ? 1 : 2;
		});

		Draw.rect(tileRegions[tiling.tiling], plan.drawx(), plan.drawy(), (plan.rotation + 1) * 90f % 180 - 90);
		Draw.rect(gaugeRegion, plan.drawx(), plan.drawy());
	}

	@Override
	public TextureRegion[] icons() {
		return new TextureRegion[]{region};
	}

	@Override
	public void load() {
		super.load();
		tileRegions = Core.atlas.find(name + "-tiles").split(32, 32)[0];
		gaugeRegion = Core.atlas.find(name + "-pointer");
	}

	@Override
	public void setBars() {
		super.setBars();
		pressureConfig.addBars(this);
		addBar("pressure", entity -> {
			HasPressure build = (HasPressure)entity;

			return new CenterBar(
				() -> Core.bundle.get("bar.pressure") + (build.pressure().getPressure(build.pressure().getMain()) < 0 ? "-" : "+") + Strings.autoFixed(Math.abs(build.pressure().getPressure(build.pressure().getMain())), 2),
				() -> build.pressure().getPressure(build.pressure().getMain()) > 0 ? maxColor : minColor,
				() -> Mathf.map(build.pressure().getPressure(build.pressure().getMain()), pressureConfig.minPressure, pressureConfig.maxPressure, -1, 1)
			);
		});
	}

	@Override
	public void setStats() {
		super.setStats();
		pressureConfig.addStats(stats);
	}

	public class PressureLiquidGaugeBuild extends LiquidBuild implements HasPressure {
		PressureModule pressure = new PressureModule();

		public int tiling;

		@Override
		public boolean acceptLiquid(Building source, Liquid liquid) {
			return hasLiquids;
		}

		@Override
		public boolean canDumpLiquid(Building to, Liquid liquid) {
			return super.canDumpLiquid(to, liquid) || to instanceof LiquidVoid.LiquidVoidBuild;
		}

		@Override
		public boolean connects(HasPressure to) {
			return HasPressure.super.connects(to) && to instanceof PressureLiquidValve.PressureLiquidValveBuild ?
				       (front() == to || back() == to) && (to.front() == this || to.back() == this) :
				       (front() == to || back() == to);
		}

		@Override
		public void draw() {
			Draw.rect(tileRegions[tiling], x, y, tiling != 0 ? 0 : (rotdeg() + 90) % 180 - 90);
			float p = Mathf.map(pressure().getPressure(pressure().getMain()), pressureConfig.minPressure, pressureConfig.maxPressure, -1, 1);
			Draw.color(
				Color.white,
				pressure().getPressure(pressure().getMain()) > 0 ? maxColor : minColor,
				p
			);
			Draw.rect(gaugeRegion, x, y, rotdeg() + (p > 1 ? Mathf.randomSeed((long) Time.time, -360f, 360f) : p * 180f));
		}

		@Override
		public void onProximityUpdate() {
			super.onProximityUpdate();
			tiling = 0;
			boolean inverted = rotation == 1 || rotation == 2;
			if (front() instanceof HasPressure front && connected(front)) tiling |= inverted ? 2 : 1;
			if (back() instanceof HasPressure back && connected(back)) tiling |= inverted ? 1 : 2;
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
		}

		@Override
		public void updatePressure() {
			HasPressure.super.updatePressure();
		}
		@Override
		public void updateTile() {
			updatePressure();
			nextBuilds(true).each(b -> moveLiquidPressure(b, liquids.current()));
			dumpPressure();
		}

		@Override
		public void write(Writes write) {
			super.write(write);
			pressure.write(write);
		}
	}
}
