package omaloon.world.blocks.liquid;

import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.blocks.liquid.*;
import omaloon.utils.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

public class PressureLiquidPump extends LiquidBlock {
	public PressureConfig pressureConfig = new PressureConfig() {{
		linksGraph = flows = false;
	}};

	public float pressureTransfer = 0.1f;

	public float pressureDifference = 10;

	public TextureRegion[] tiles;

	public PressureLiquidPump(String name) {
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

		Draw.rect(tiles[tiling.tiling], plan.drawx(), plan.drawy(), (plan.rotation + 1) * 90f % 180 - 90);
		Draw.rect(topRegion, plan.drawx(), plan.drawy(), (plan.rotation) * 90f);
	}

	@Override public TextureRegion[] icons() {
		return new TextureRegion[]{region, topRegion};
	}

	@Override
	public void load() {
		super.load();
		tiles = OlUtils.split(name + "-tiles", 32, 0);
	}

	@Override
	public void setStats() {
		super.setStats();
		pressureConfig.addStats(stats);
	}

	public class PressureLiquidPumpBuild extends LiquidBuild implements HasPressure {
		PressureModule pressure = new PressureModule();

		public int tiling;

		@Override public boolean acceptLiquid(Building source, Liquid liquid) {
			return false;
		}
		@Override public boolean acceptsPressure(HasPressure from, float pressure) {
			return false;
		}

		@Override
		public boolean connects(HasPressure to) {
			return HasPressure.super.connects(to) && (to == front() || to == back());
		}

		@Override
		public void draw() {
			float rot = rotate ? (90 + rotdeg()) % 180 - 90 : 0;
			Draw.rect(tiles[tiling], x, y, rot);
			Draw.rect(topRegion, x, y, rotdeg());
		}

		@Override
		public HasPressure getPressureDestination(HasPressure from, float pressure) {
			if (from == front()) return back() instanceof HasPressure b ? b : this;
			if (from == back()) return front() instanceof HasPressure b ? b : this;
			return this;
		}

		@Override
		public Seq<HasPressure> nextBuilds(boolean flow) {
			return Seq.with();
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

		@Override
		public void updateTile() {
			super.updateTile();
			if (efficiency > 0) {
				HasPressure front = (front() instanceof HasPressure b && b.pressureConfig().linksGraph) ? b : null;
				HasPressure back = (back() instanceof HasPressure b && b.pressureConfig().linksGraph) ? b : null;

				float difference = (front == null ? 0 : front.getPressure()) - (back == null ? 0 : back.getPressure());
				if (difference < pressureDifference) {
					if (front != null) front.handlePressure(pressureTransfer * edelta());
					if (back != null) back.removePressure(pressureTransfer * edelta());
				}
				if (back != null) {
					if (front != null) {
						back.moveLiquidPressure(front, back.liquids().current());
					} else {
						float leakAmount = back.liquids().get(back.liquids().current()) / 1.5f;
						Puddles.deposit(tile.nearby(rotation), tile, back.liquids().current(), leakAmount, true, true);
						back.liquids().remove(back.liquids().current(), leakAmount);
					}
				}
			}
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
		public void write(Writes write) {
			super.write(write);
			pressure.write(write);
		}
	}
}
