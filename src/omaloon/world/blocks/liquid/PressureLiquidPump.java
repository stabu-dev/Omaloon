package omaloon.world.blocks.liquid;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.blocks.liquid.*;
import omaloon.annotations.Load;
import omaloon.content.*;
import omaloon.utils.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

import static mindustry.Vars.*;
import static mindustry.type.Liquid.*;

public class PressureLiquidPump extends LiquidBlock {
	public PressureConfig pressureConfig = new PressureConfig();

	public float pressureTransfer = 0.1f;

	public float pressureDifference = 10;

	public Effect pumpEffectForward = OlFx.pumpFront, pumpEffectBackward = OlFx.pumpBack;
	public float pumpEffectInterval = 15f;

	public float liquidPadding = 3f;

	public TextureRegion[][] liquidRegions;
	public TextureRegion[] tiles;
	@Load("@-arrow")
	public TextureRegion arrowRegion;

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
			if (!(next.block instanceof PressureLiquidPump)) {
				if (new Point2(next.x, next.y).equals(front) && next.block.outputsLiquid) tiling.tiling |= inverted ? 2 : 1;
				if (new Point2(next.x, next.y).equals(back) && next.block.outputsLiquid) tiling.tiling |= inverted ? 1 : 2;
			}
		});

		Draw.rect(bottomRegion, plan.drawx(), plan.drawy(), 0);
		if (tiling.tiling != 0) Draw.rect(arrowRegion, plan.drawx(), plan.drawy(), (plan.rotation) * 90f);
		Draw.rect(tiles[tiling.tiling], plan.drawx(), plan.drawy(), (plan.rotation + 1) * 90f % 180 - 90);
		if (tiling.tiling == 0) Draw.rect(topRegion, plan.drawx(), plan.drawy(), (plan.rotation) * 90f);
	}

	@Override public TextureRegion[] icons() {
		return new TextureRegion[]{region, topRegion};
	}

	@Override
	public void load() {
		super.load();
		tiles = OlUtils.split(name + "-tiles", 32, 0);
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
	public void setStats() {
		super.setStats();
		pressureConfig.addStats(stats);
		stats.add(OlStats.pressureFlow, Mathf.round(pressureTransfer * 60f, 2), OlStats.pressureSecond);
	}

	public class PressureLiquidPumpBuild extends LiquidBuild implements HasPressureImpl {


		public int tiling;

		public float effectInterval;

		@Override public boolean acceptLiquid(Building source, Liquid liquid) {
			return false;
		}
		@Override public boolean acceptsPressure(HasPressure from, float pressure) {
			return false;
		}

		@Override public boolean connects(HasPressure to) {
			return HasPressureImpl.super.connects(to) && !(to instanceof PressureLiquidPumpBuild) && (front() == to || back() == to);
		}

		@Override
		public void draw() {
			float rot = rotate ? (90 + rotdeg()) % 180 - 90 : 0;
			if (tiling != 0) {
				Draw.rect(bottomRegion, x, y, rotdeg());
				if (liquids().currentAmount() > 0.01f) {
					HasPressure front = (front() instanceof HasPressure b && connected(b)) ? b : null;
					HasPressure back = (back() instanceof HasPressure b && connected(b)) ? b : null;
					float alpha =
						(front == null ? 0 : front.liquids().currentAmount()/front.block().liquidCapacity) +
							(back == null ? 0 : back.liquids().currentAmount()/back.block().liquidCapacity);
					alpha /= ((front == null ? 0 : 1f) + (back == null ? 0 : 1f));

					int frame = liquids.current().getAnimationFrame();
					int gas = liquids.current().gas ? 1 : 0;

					float xscl = Draw.xscl, yscl = Draw.yscl;
					Draw.scl(1f, 1f);
					Drawf.liquid(liquidRegions[gas][frame], x, y, alpha,
						front == null ? back == null ? Liquids.water.color : back.liquids().current().color : front.liquids().current().color
					);
					Draw.scl(xscl, yscl);
				}
				Draw.rect(arrowRegion, x, y, rotdeg());
			}
			Draw.rect(tiles[tiling], x, y, rot);
			if (tiling == 0) Draw.rect(topRegion, x, y, rotdeg());
		}

		@Override
		public Seq<HasPressure> nextBuilds(boolean flow) {
			return Seq.with();
		}

		@Override
		public void onProximityUpdate() {
			super.onProximityUpdate();
			tiling = 0;
			boolean inverted = rotation == 1 || rotation == 2;
			if (front() instanceof HasPressure front && connected(front)) tiling |= inverted ? 2 : 1;
			if (back() instanceof HasPressure back && connected(back)) tiling |= inverted ? 1 : 2;
		}


		@Override
		public void updateTile() {
			super.updateTile();
			if (efficiency > 0) {
				HasPressure front = (front() instanceof HasPressure b && connected(b)) ? b : null;
				HasPressure back = (back() instanceof HasPressure b && connected(b)) ? b : null;
				boolean pumped = false;

				float solid = 1;
				if (front == null && front() != null || back == null && back() != null) solid++;

				float difference = (front == null ? 0 : front.getPressure()) - (back == null ? 0 : back.getPressure());
				if (difference < pressureDifference/solid) {
					if (front != null) front.handlePressure(pressureTransfer * edelta());
					if (back != null) back.removePressure(pressureTransfer * edelta());
					pumped = true;
				} else if (back != null && front == null && front() == null) {
					back.removePressure(pressureTransfer * edelta());
					pumped = true;
				}

				if (pumped) effectInterval += delta();
				if (effectInterval > pumpEffectInterval) {
					if (front() == null) pumpEffectForward.at(x, y, rotdeg());
					if (back() == null) pumpEffectBackward.at(x, y, rotdeg() + 180f);
					effectInterval = 0f;
				}

				if (back != null) {
					if (front != null) {
						back.moveLiquidPressure(front, back.liquids().current());
					} else {
						if (front() == null) {
							float leakAmount = back.liquids().get(back.liquids().current()) / 1.5f;
							Puddles.deposit(tile.nearby(rotation), tile, back.liquids().current(), leakAmount, true, true);
							back.liquids().remove(back.liquids().current(), leakAmount);
						}
					}
				}
			}
		}

	}
}
