package omaloon.world.blocks.liquid;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.blocks.liquid.*;
import mindustry.world.blocks.sandbox.*;
import omaloon.utils.*;
import omaloon.world.blocks.liquid.PressureLiquidPump.*;
import omaloon.world.blocks.liquid.PressureLiquidValve.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

import static mindustry.Vars.*;
import static mindustry.type.Liquid.*;

public class PressureLiquidDuct extends LiquidRouter {
	public PressureConfig pressureConfig = new PressureConfig();

	public TextureRegion[] topRegions;
	public TextureRegion[][] liquidRegions;

	public float liquidPadding = 3f;

	public PressureLiquidDuct(String name) {
		super(name);
		rotate = true;
	}

	@Override
	public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
		int tiling = 0;
		BuildPlan[] proximity = new BuildPlan[4];

		list.each(next -> {
			for(int i = 0; i < 4; i++) {
				Point2 side = new Point2(plan.x, plan.y).add(Geometry.d4[i]);
				if (
					new Point2(next.x, next.y).equals(side) &&
					(
						(next.block instanceof PressureLiquidDuct || next.block instanceof PressureLiquidPump || next.block instanceof PressureLiquidValve) ?
							(plan.rotation%2 == i%2 || next.rotation%2 == i%2) : (next.block.outputsLiquid)
					)
				) {
					proximity[i] = next;
					break;
				}
			}
		});

		for(int i = 0; i < 4; i++) {
			if (proximity[i] != null) tiling |= (1 << i);
		}

		Draw.rect(bottomRegion, plan.drawx(), plan.drawy(), 0);
		if (tiling == 0) {
			Draw.rect(topRegions[tiling], plan.drawx(), plan.drawy(), (plan.rotation + 1) * 90f % 180 - 90);
		} else {
			Draw.rect(topRegions[tiling], plan.drawx(), plan.drawy(), 0);
		}
	}

	@Override
	public TextureRegion[] icons() {
		return new TextureRegion[]{region};
	}

	@Override
	public void load() {
		super.load();
		topRegions = OlUtils.split(name + "-tiles", 32, 0);
		if (!bottomRegion.found()) bottomRegion = Core.atlas.find("omaloon-liquid-bottom");

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
	}

	public class PressureLiquidDuctBuild extends LiquidRouterBuild implements HasPressure {
		public int tiling = 0;
		PressureModule pressure = new PressureModule();

		@Override
		public boolean canDumpLiquid(Building to, Liquid liquid) {
			return super.canDumpLiquid(to, liquid) || to instanceof LiquidVoid.LiquidVoidBuild;
		}

		@Override
		public boolean connects(HasPressure to) {
			return (to instanceof PressureLiquidDuctBuild || to instanceof PressureLiquidPumpBuild || to instanceof PressureLiquidValveBuild) ?
			  (front() == to || back() == to || to.front() == this || to.back() == this) :
				to != null && (to.pressureConfig().outputsPressure || to.pressureConfig().acceptsPressure);
		}

		@Override
		public void draw() {
			Draw.rect(bottomRegion, x, y);
			if (liquids().currentAmount() > 0.01f) {
				int frame = liquids.current().getAnimationFrame();
				int gas = liquids.current().gas ? 1 : 0;

				float xscl = Draw.xscl, yscl = Draw.yscl;
				Draw.scl(1f, 1f);
				Drawf.liquid(liquidRegions[gas][frame], x, y, liquids.currentAmount()/liquidCapacity, liquids.current().color.write(Tmp.c1).a(1f));
				Draw.scl(xscl, yscl);
			}
			Draw.rect(topRegions[tiling], x, y, tiling != 0 ? 0 : (rotdeg() + 90) % 180 - 90);
		}

		@Override
		public float moveLiquid(Building next, Liquid liquid) {
			if (next instanceof HasPressure p) return moveLiquidPressure(p, liquid);
			return 0f;
		}

		@Override
		public void onProximityAdded() {
			super.onProximityAdded();
			pressureGraph().addBuild(this);
		}

		@Override
		public void onProximityRemoved() {
			super.onProximityRemoved();
			pressureGraph().removeBuild(this, true);
		}

		@Override
		public void onProximityUpdate() {
			super.onProximityUpdate();
			tiling = 0;
			for (int i = 0; i < 4; i++) {
				HasPressure build = nearby(i) instanceof HasPressure ? (HasPressure) nearby(i) : null;
				if (
					build != null && connects(build)
				) tiling |= (1 << i);
			}
			pressureGraph().removeBuild(this, false);
		}

		@Override public PressureModule pressure() {
			return pressure;
		}
		@Override public PressureConfig pressureConfig() {
			return pressureConfig;
		}

		@Override
		public void updateTile() {
			updateDeath();
			nextBuilds(true).each(b -> moveLiquidPressure(b, liquids.current()));
			dumpPressure();
		}

		@Override
		public void read(Reads read, byte revision) {
			super.read(read, revision);
			pressure.read(read);
		}
		@Override
		public void write(Writes write) {
			super.write(write);
			pressure.write(write);
		}
	}
}
