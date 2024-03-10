package omaloon.world.blocks.liquid;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
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
		linksGraph = flows =  false;
	}};

	public float pressureTransfer = 0.1f;
	public float frontMaxPressure = 100f;
	public float backMinPressure = -100f;

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

		@Override
		public boolean acceptLiquid(Building source, Liquid liquid) {
			return hasLiquids;
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
			if (efficiency > 0
				&& front() instanceof HasPressure front
				&& back() instanceof HasPressure back
			) {
				if (
					front.getPressure() < frontMaxPressure &&
					back.getPressure() > backMinPressure
				) {
					front.handlePressure(pressureTransfer * edelta());
					back.removePressure(pressureTransfer * edelta());
					float flow = Math.min(pressureTransfer * Time.delta, front.liquids().currentAmount());

					if (back.acceptLiquid(front.as(), front.liquids().current()) && front.canDumpLiquid(back.as(), back.liquids().current())) {
						front.liquids().remove(front.liquids().current(), flow);
						back.handleLiquid(front.as(), front.liquids().current(), flow);
					}
					Liquid buildLiquid = front.liquids().current();
					Liquid otherLiquid = back.liquids().current();
					if (buildLiquid.blockReactive && otherLiquid.blockReactive) {
						if (
							(!(otherLiquid.flammability > 0.3f) || !(buildLiquid.temperature > 0.7f)) &&
								(!(buildLiquid.flammability > 0.3f) || !(otherLiquid.temperature > 0.7f))
						) {
							if (
								buildLiquid.temperature > 0.7f && otherLiquid.temperature < 0.55f ||
									otherLiquid.temperature > 0.7f && buildLiquid.temperature < 0.55f
							) {
								front.liquids().remove(buildLiquid, Math.min(front.liquids().get(buildLiquid), 0.7f * Time.delta));
								if (Mathf.chanceDelta(0.1f)) {
									Fx.steam.at(front.x(), front.y());
								}
							}
						} else {
							front.damageContinuous(1f);
							back.damageContinuous(1f);
							if (Mathf.chanceDelta(0.1f)) {
								Fx.fire.at(front.x(), front.y());
							}
						}
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
