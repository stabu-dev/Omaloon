package omaloon.world.blocks.liquid;

import arc.*;
import arc.audio.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import omaloon.content.*;
import omaloon.math.*;
import omaloon.utils.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

import static mindustry.Vars.*;
import static mindustry.type.Liquid.*;

public class PressureLiquidValve extends Block {
	public PressureConfig pressureConfig = new PressureConfig();

	public TextureRegion[] tiles;
	public TextureRegion[][] liquidRegions;
	public TextureRegion valveRegion, topRegion, bottomRegion;

	public Effect jamEffect = Fx.explosion;
	public Sound jamSound = OlSounds.jam;

	public Effect pumpingEffectOut = Fx.none;
	public Effect pumpingEffectIn = Fx.none;
	public float pumpingEffectInterval = 15;

	public float pressureLoss = 1f;
	public float minPressureLoss = 0.05f;

	public float openMin = -15f;
	public float openMax = 15f;
	public float jamPoint = -45f;

	public float liquidPadding = 3f;

	public PressureLiquidValve(String name) {
		super(name);
		rotate = true;
		update = true;
		destructible = true;
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

		Draw.rect(bottomRegion, plan.drawx(), plan.drawy(), 0);
		Draw.rect(tiles[tiling.tiling], plan.drawx(), plan.drawy(), (plan.rotation + 1) * 90f % 180 - 90);
		Draw.rect(valveRegion, plan.drawx(), plan.drawy(), (plan.rotation + 1) * 90f % 180 - 90);
		Draw.rect(topRegion, plan.drawx(), plan.drawy());
	}

	@Override
	public TextureRegion[] icons() {
		return new TextureRegion[]{bottomRegion, region};
	}

	@Override
	public void init() {
		super.init();

		if (pressureConfig.fluidGroup == null) pressureConfig.fluidGroup = FluidGroup.transportation;
	}

	@Override
	public void load() {
		super.load();
		tiles = OlUtils.split(name + "-tiles", 32, 0);
		valveRegion = Core.atlas.find(name + "-valve");
		topRegion = Core.atlas.find(name + "-top");
		bottomRegion = Core.atlas.find(name + "-bottom", "omaloon-liquid-bottom");

		liquidRegions = new TextureRegion[2][animationFrames];
		if(renderer != null){
			var frames = renderer.getFluidFrames();

			for (int fluid = 0; fluid < 2; fluid++) {
				for (int frame = 0; frame < animationFrames; frame++) {
					TextureRegion base = frames[fluid][frame];
					TextureRegion result = new TextureRegion();
					result.set(base);

					result.setHeight(result.height - liquidPadding);
					result.setWidth(result.width - liquidPadding);
					result.setX(result.getX() + liquidPadding);
					result.setY(result.getY() + liquidPadding);

					liquidRegions[fluid][frame] = result;
				}
			}
		}
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
		stats.add(OlStats.pressureFlow, Mathf.round(pressureLoss * 60f, 2), OlStats.pressureSecond);
	}

	public class PressureLiquidValveBuild extends Building implements HasPressure {
		PressureModule pressure = new PressureModule();

		public float draining;
		public float effectInterval;
		public int tiling;
		public float smoothAlpha;

		public boolean jammed;

		@Override
		public boolean acceptsPressurizedFluid(HasPressure from, @Nullable Liquid liquid, float amount) {
			return HasPressure.super.acceptsPressurizedFluid(from, liquid, amount) && (liquid == pressure.getMain() || liquid == null || pressure.getMain() == null || from.pressure().getMain() == null);
		}

		@Override
		public boolean connects(HasPressure to) {
			return HasPressure.super.connects(to) && to instanceof PressureLiquidValveBuild ?
				       (front() == to || back() == to) && (to.front() == this || to.back() == this) :
				       (front() == to || back() == to);
		}

		@Override
		public void draw() {
			float rot = rotate ? (90 + rotdeg()) % 180 - 90 : 0;
			Draw.rect(bottomRegion, x, y, rotation);
			Liquid main = pressure.getMain();

			smoothAlpha = Mathf.approachDelta(smoothAlpha, main == null ? 0f : pressure.liquids[main.id]/(pressure.liquids[main.id] + pressure.air), PressureModule.smoothingSpeed);

			if (smoothAlpha > 0.01f) {
				int frame = pressure.current.getAnimationFrame();
				int gas = pressure.current.gas ? 1 : 0;

				float xscl = Draw.xscl, yscl = Draw.yscl;
				Draw.scl(1f, 1f);
				Drawf.liquid(liquidRegions[gas][frame], x, y, Mathf.clamp(smoothAlpha), pressure.current.color.write(Tmp.c1).a(1f));
				Draw.scl(xscl, yscl);
			}
			Draw.rect(tiles[tiling], x, y, rot);
			Draw.rect(topRegion, x, y);
			Draw.rect(valveRegion, x, y, draining * (rotation % 2 == 0 ? -90f : 90f) + rot);
		}

		@Override
		public void onProximityUpdate() {
			super.onProximityUpdate();

			tiling = 0;
			boolean inverted = rotation == 1 || rotation == 2;
			if (front() instanceof HasPressure front && connected(front)) tiling |= inverted ? 2 : 1;
			if (back() instanceof HasPressure back && connected(back)) tiling |= inverted ? 1 : 2;

			new PressureSection().mergeFlood(this);
		}

		@Override
		public boolean outputsPressurizedFluid(HasPressure to, Liquid liquid, float amount) {
			return HasPressure.super.outputsPressurizedFluid(to, liquid, amount) && (liquid == to.pressure().getMain() || liquid == null || pressure.getMain() == null || to.pressure().getMain() == null);
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
			jammed = read.bool();
			draining = read.f();
			smoothAlpha = read.f();
		}

		@Override
		public void updatePressure() {
			HasPressure.super.updatePressure();

			float pressureAmount = pressure.getPressure(pressure.getMain());

			if (pressureAmount > jamPoint) jammed = false;
			if (jammed) return;
			if (pressureAmount < openMin) {
				effectInterval += delta();
				addFluid(null, Math.min(openMin - pressureAmount, OlMath.flowRate(
					0,
					pressureAmount,
					5,
					pressureConfig.fluidCapacity,
					OlLiquids.getDensity(null),
					OlLiquids.getViscosity(null)
				)));
				draining = Mathf.approachDelta(draining, 1, 0.014f);
			}
			if (pressureAmount > openMax) {
				effectInterval += delta();
				removeFluid(pressure.getMain(), Math.min(pressureAmount - openMax, OlMath.flowRate(
					pressureAmount,
					0,
					pressureConfig.fluidCapacity,
					5,
					OlLiquids.getDensity(null),
					OlLiquids.getViscosity(null)
				)));
				draining = Mathf.approachDelta(draining, 1, 0.014f);
			}
			if (effectInterval > pumpingEffectInterval) {
				effectInterval = 0;
				if (pressureAmount > openMax) {
					pumpingEffectOut.at(x, y, draining * (rotation % 2 == 0 ? -90f : 90f) + (rotate ? (90 + rotdeg()) % 180 - 90 : 0), pressure.getMain() == null ? Color.white : pressure.getMain().color);
				} else {
					pumpingEffectIn.at(x, y, draining * (rotation % 2 == 0 ? -90f : 90f) + (rotate ? (90 + rotdeg()) % 180 - 90 : 0));
				}
			}

			if (pressureAmount >= openMin && pressureAmount <= openMax) {
				draining = Mathf.approachDelta(draining, 0, 0.014f);
			}

			if (pressureAmount < jamPoint) {
				jammed = true;
				draining = 0f;
				jamEffect.at(x, y, draining * (rotation%2 == 0 ? -90 : 90) + (rotate ? (90 + rotdeg()) % 180 - 90 : 0), valveRegion);
				jamSound.at(x, y);
			}
		}
		@Override
		public void updateTile() {
			updatePressure();
		}

		@Override
		public void write(Writes write) {
			super.write(write);
			pressure.write(write);
			write.bool(jammed);
			write.f(draining);
			write.f(smoothAlpha);
		}
	}
}
