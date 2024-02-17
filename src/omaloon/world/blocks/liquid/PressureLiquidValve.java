package omaloon.world.blocks.liquid;

import arc.*;
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
import mindustry.ui.*;
import mindustry.world.blocks.liquid.*;
import omaloon.utils.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

public class PressureLiquidValve extends LiquidBlock {
	public PressureConfig pressureConfig = new PressureConfig();

	public TextureRegion[] tiles;
	public TextureRegion valveRegion;

	// TODO make actual effect
	public Effect disperseEffect = Fx.none;
	public float disperseEffectInterval = 30;

	public float pressureLoss = 0.1f;
	public float liquidLoss = 0.1f;

	public PressureLiquidValve(String name) {
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
			if (new Point2(next.x, next.y).equals(front) &&
				    (next.build() instanceof HasPressure pressure &&
					     (pressure.pressureConfig().acceptsPressure || pressure.pressureConfig().outputsPressure)
				    )
			) tiling.tiling |= inverted ? 2 : 1;
			if (new Point2(next.x, next.y).equals(back) &&
				    (next.build() instanceof HasPressure pressure &&
					     (pressure.pressureConfig().acceptsPressure || pressure.pressureConfig().outputsPressure)
				    )
			) tiling.tiling |= inverted ? 1 : 2;
		});

		Draw.rect(bottomRegion, plan.drawx(), plan.drawy(), 0);
		Draw.rect(tiles[tiling.tiling], plan.drawx(), plan.drawy(), (plan.rotation + 1) * 90f % 180 - 90);
		Draw.rect(valveRegion, plan.drawx(), plan.drawy(), (plan.rotation + 1) * 90f % 180 - 90);
	}

	@Override
	public TextureRegion[] icons() {
		return new TextureRegion[]{bottomRegion, region};
	}

	@Override
	public void load() {
		super.load();
		tiles = OlUtils.split(name + "-tiles", 32, 0);
		valveRegion = Core.atlas.find(name + "-valve");
	}

	@Override
	public void setBars() {
		super.setBars();
		addBar("pressure", entity -> {
			HasPressure build = (HasPressure) entity;
			return new Bar(
				() -> Core.bundle.get("pressure") + Strings.fixed(build.getPressure(), 2),
				build::getBarColor,
				build::getPressureMap
			);
		});
	}

	@Override
	public void setStats() {
		super.setStats();
		pressureConfig.addStats(stats);
	}

	public class PressureLiquidValveBuild extends LiquidBuild implements HasPressure {
		PressureModule pressure = new PressureModule();

		public float draining;
		public float effectInterval;
		public int tiling;

		@Override
		public boolean acceptLiquid(Building source, Liquid liquid){
			return (liquids.current() == liquid || liquids.currentAmount() < 0.2f) && source instanceof HasPressure to && connects(to);
		}

		@Override
		public boolean canDumpLiquid(Building to, Liquid liquid) {
			return super.canDumpLiquid(to, liquid) && to instanceof HasPressure toPressure && canDumpPressure(toPressure, 0);
		}
		@Override
		public boolean canDumpPressure(HasPressure to, float pressure) {
			return HasPressure.super.canDumpPressure(to, pressure) && connects(to);
		}

		@Override
		public boolean connects(HasPressure to) {
			return to instanceof PressureLiquidValveBuild ?
				       (front() == to || back() == to) && (to.front() == this || to.back() == this) :
				       (front() == to || back() == to);
		}

		@Override
		public void draw() {
			float rot = rotate ? (90 + rotdeg()) % 180 - 90 : 0;
			Draw.rect(bottomRegion, x, y, rotation);

			if(liquids.currentAmount() > 0.001f){
				Drawf.liquid(liquidRegion, x, y, liquids.currentAmount() / liquidCapacity, liquids.current().color);
			}

			Draw.rect(tiles[tiling], x, y, rot);
			Draw.rect(valveRegion, x, y, draining * (rotation%2 == 0 ? -90 : 90) + rot);
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
			boolean inverted = rotation == 1 || rotation == 2;
			if (front() instanceof HasPressure front && connects(front)) tiling |= inverted ? 2 : 1;
			if (back() instanceof HasPressure back && connects(back)) tiling |= inverted ? 1 : 2;
			pressureGraph().removeBuild(this, false);
		}

		@Override public PressureModule pressure() {
			return pressure;
		}
		@Override public PressureConfig pressureConfig() {
			return pressureConfig;
		}

		@Override
		public void updateDeath() {
			switch (getPressureState()) {
				case overPressure -> {
					effectInterval += delta();
					removePressure(pressureLoss * Time.delta);
					if (liquids.current() != null) liquids.remove(liquids.current(), liquidLoss * delta());
					draining = Mathf.approachDelta(draining, 1, 0.014f);
				}
				case underPressure -> {
					effectInterval += delta();
					handlePressure(pressureLoss * Time.delta);
					draining = Mathf.approachDelta(draining, 1, 0.014f);
				}
				default -> draining = Mathf.approachDelta(draining, 0, 0.014f);
			}
			if (effectInterval > disperseEffectInterval) {
				effectInterval = 0;
				disperseEffect.at(x, y, draining * (rotation%2 == 0 ? -90 : 90) + (rotate ? (90 + rotdeg()) % 180 - 90 : 0), liquids.current());
			}
		}

		@Override
		public void updateTile() {
			super.updateTile();
			updateDeath();
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
