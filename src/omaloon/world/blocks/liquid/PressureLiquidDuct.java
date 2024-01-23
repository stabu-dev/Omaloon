package omaloon.world.blocks.liquid;

import arc.*;
import arc.graphics.g2d.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.liquid.*;
import omaloon.utils.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

public class PressureLiquidDuct extends LiquidRouter {
	public PressureConfig pressureConfig = new PressureConfig();

	public TextureRegion[] topRegions;

	public PressureLiquidDuct(String name) {
		super(name);
		rotate = true;
	}

	@Override
	public TextureRegion[] icons() {
		return new TextureRegion[]{region};
	}

	@Override
	public void load() {
		super.load();
		topRegions = OlUtils.split(name + "-tiles", 32, 0);
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


	public class PressureLiquidDuctBuild extends LiquidRouterBuild implements HasPressure {
		public int tiling = 0;
		PressureModule pressure = new PressureModule();

//		@Override
//		public boolean canDumpLiquid(Building to, Liquid liquid) {
//
//			return super.canDumpLiquid(to, liquid) && to instanceof HasPressure toPressure &&
//				       canDumpPressure(toPressure, 0);
//		}
		@Override
		public boolean canDumpLiquid(Building to, Liquid liquid) {
			return super.canDumpLiquid(to, liquid) && to.liquids.get(liquid) < liquids.get(liquid);
		}

		@Override
		public boolean canDumpPressure(HasPressure to, float pressure) {

			return HasPressure.super.canDumpPressure(to, pressure) &&
				to instanceof PressureLiquidDuctBuild ?
				  (front() == to || back() == to || to.front() == this || to.back() == this) || !proximity.contains((Building) to) :
				  to.connects(this);
		}

		@Override
		public boolean connects(HasPressure to) {
			return to instanceof PressureLiquidDuctBuild ?
			  (front() == to || back() == to || to.front() == this || to.back() == this) :
				to.connects(this);
		}

		@Override
		public void draw() {
			Draw.rect(bottomRegion, x, y);
			if (liquids().currentAmount() > 0.01f) {
				Draw.color(liquids.current().color);
				Draw.alpha(liquids().currentAmount() / liquidCapacity);
				Draw.rect(liquidRegion, x, y);
				Draw.color();
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
			super.updateTile();
			dumpPressure();
			for (HasPressure build : proximity.select(b -> b instanceof HasPressure && (b == front() || b == back())).<HasPressure>as()) {
				if (liquids.currentAmount() > 0.0001f) {
					moveLiquidPressure(build, liquids.current());
				}
			}
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
