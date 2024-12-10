package omaloon.world.blocks.production;

import arc.*;
import arc.Graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.draw.*;
import mindustry.world.meta.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

public class Pressurizer extends Block {
	public PressureConfig pressureConfig = new PressureConfig();

	public DrawBlock drawer = new DrawDefault();

	public TextureRegion overlayRegion;

	public float progressTime = 60f;
	public float warmupSpeed = 0.14f;

	public float outputPressure = 0f;

	public boolean continuous = false;

	public Pressurizer(String name) {
		super(name);
		update = true;
		destructible = true;
		solid = true;

		config(Boolean.class, (PressurizerBuild build, Boolean bool) -> build.reverse = bool);
	}

	@Override
	public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
		drawer.drawPlan(this, plan, list);
		if (plan.config instanceof Boolean bool && bool) Draw.rect(overlayRegion, plan.drawx(), plan.drawy(), rotate ? plan.rotation * 90f : 0);
	}

	@Override public void getRegionsToOutline(Seq<TextureRegion> out) {
		drawer.getRegionsToOutline(this, out);
	}

	@Override
	protected TextureRegion[] icons() {
		return drawer.finalIcons(this);
	}

	@Override
	public void load() {
		super.load();

		drawer.load(this);

		overlayRegion = Core.atlas.find(name + "-overlay");
	}

	@Override
	public void setBars() {
		super.setBars();

		pressureConfig.addBars(this);
	}

	@Override
	public void setStats() {
		super.setStats();

		pressureConfig.addStats(stats);
		if (outputPressure != 0) stats.add(OlStats.outputPressure, Strings.autoFixed(outputPressure * (continuous ? 60f : 1f), 2), continuous ? OlStats.pressureSecond : OlStats.pressureUnits);
		if (!continuous) stats.add(Stat.productionTime, Strings.autoFixed(progressTime/60f, 2), StatUnit.seconds);
	}

	public class PressurizerBuild extends Building implements HasPressure {
		public PressureModule pressure = new PressureModule();

		public boolean reverse;

		public float warmup, progress, totalProgress;

		@Override public Cursor getCursor() {
			return Cursor.SystemCursor.hand;
		}

		@Override
		public void draw() {
			drawer.draw(this);
			if (reverse) Draw.rect(overlayRegion, x, y, drawrot());
		}
		@Override
		public void drawLight() {
			super.drawLight();
			drawer.drawLight(this);
		}

		@Override public HasPressure getSectionDestination(HasPressure from) {
			return null;
		}

		@Override
		public void onProximityUpdate() {
			super.onProximityUpdate();

			new PressureSection().mergeFlood(this);
		}

		@Override public PressureModule pressure() {
			return pressure;
		}
		@Override public PressureConfig pressureConfig() {
			return pressureConfig;
		}

		@Override public float progress() {
			return progress;
		}

		@Override
		public void read(Reads read, byte revision) {
			super.read(read, revision);
			pressure.read(read);

			progress = read.f();
			totalProgress = read.f();
			warmup = read.f();

			reverse = read.bool();
		}

		@Override public void tapped() {
			configure(!reverse);
		}

		@Override public float totalProgress() {
			return totalProgress;
		}

		@Override
		public void updateTile() {
			updatePressure();

			if (efficiency > 0) {
				progress += getProgressIncrease(progressTime);
				warmup = Mathf.approachDelta(warmup, 1, warmupSpeed);
				totalProgress += warmup * edelta();

				if (continuous) addFluid(null, outputPressure * warmup * (reverse ? -1f : 1f));
			} else warmup = Mathf.approachDelta(warmup, 0, warmupSpeed);

			if(progress >= 1f){
				if (!continuous) addFluid(null, outputPressure * warmup * (reverse ? -1f : 1f));
				progress %= 1f;
			}
		}

		@Override public float warmup() {
			return warmup;
		}

		@Override
		public void write(Writes write) {
			super.write(write);
			pressure.write(write);

			write.f(progress);
			write.f(totalProgress);
			write.f(warmup);

			write.bool(reverse);
		}
	}
}
