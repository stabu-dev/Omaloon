package omaloon.world.blocks.liquid;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
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

public class PressureLiquidValve extends LiquidBlock {
	public PressureConfig pressureConfig = new PressureConfig();

	public TextureRegion[] tiles;
	public TextureRegion valveRegion;

	public float pressureLoss = 0.1f;

	public PressureLiquidValve(String name) {
		super(name);
		rotate = true;
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
				Core.bundle.get("pressure"),
				Pal.accent,
				build::getPressureMap
			);
		});
	}

	public class PressureLiquidValveBuild extends LiquidBuild implements HasPressure {
		PressureModule pressure = new PressureModule();

		public float draining;
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
			float rotation = rotate ? (90 + rotdeg()) % 180 - 90 : 0;
			Draw.rect(bottomRegion, x, y, rotation);

			if(liquids.currentAmount() > 0.001f){
				Drawf.liquid(liquidRegion, x, y, liquids.currentAmount() / liquidCapacity, liquids.current().color);
			}

			Draw.rect(tiles[tiling], x, y, rotation);
			Draw.rect(valveRegion, x, y, draining * -90 + rotation);
		}

		@Override
		public void onProximityUpdate() {
			super.onProximityUpdate();
			tiling = 0;
			boolean inverted = rotation == 1 || rotation == 2;
			if (front() instanceof HasPressure front && connects(front)) tiling |= inverted ? 2 : 1;
			if (back() instanceof HasPressure back && connects(back)) tiling |= inverted ? 1 : 2;
		}

		@Override
		public void updateDeath() {
			switch (getPressureState()) {
				case overPressure -> {
					removePressure(pressureLoss * Time.delta);
					draining = Mathf.approachDelta(draining, 1, 0.014f);
				}
				case underPressure -> {
					handlePressure(pressureLoss * Time.delta);
					draining = Mathf.approachDelta(draining, 1, 0.014f);
				}
				default -> draining = Mathf.approachDelta(draining, 0, 0.014f);
			}
		}

		@Override
		public void updateTile() {
			super.updateTile();
			dumpPressure();
			if(liquids.currentAmount() > 0.01f){
				dumpLiquid(liquids.current());
			}
			updateDeath();
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
