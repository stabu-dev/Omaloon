package omaloon.world.blocks.liquid;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.blocks.liquid.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

public class PressureLiquidValve extends LiquidBlock {
	public PressureConfig pressureConfig = new PressureConfig();

	public TextureRegion valveRegion1, valveRegion2;

	public float pressureLoss = 0.1f;

	public PressureLiquidValve(String name) {
		super(name);
	}

	@Override
	public TextureRegion[] icons() {
		return new TextureRegion[]{bottomRegion, region};
	}

	@Override
	public void load() {
		super.load();
		valveRegion1 = Core.atlas.find(name + "-valve1");
		valveRegion2 = Core.atlas.find(name + "-valve2");
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

		@Override
		public void draw() {
			super.draw();
			Drawf.spinSprite(valveRegion1, x, y, draining * 90);
		}

		@Override
		public void updateDeath() {
			HasPressure.super.updateDeath();
			switch (getPressureState()) {
				case overPressure -> {
					removePressure(pressureLoss * Time.delta);
					draining = Mathf.approachDelta(draining, 1, 0.014f);
				}
				case underPressure -> {
					handlePressure(pressureLoss * Time.delta);
					draining = Mathf.approachDelta(draining, 0, 0.014f);
				}
				default -> {}
			}
		}

		@Override
		public void updateTile() {
			super.updateTile();
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
