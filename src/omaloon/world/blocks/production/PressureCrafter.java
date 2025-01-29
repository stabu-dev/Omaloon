package omaloon.world.blocks.production;

import arc.util.io.*;
import mindustry.world.blocks.production.*;
import mindustry.world.consumers.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

public class PressureCrafter extends GenericCrafter {
	public PressureConfig pressureConfig = new PressureConfig();

	public boolean useConsumerMultiplier = true;

	public PressureCrafter(String name) {
		super(name);
		pressureConfig.isWhitelist = true;
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

	public class PressureCrafterBuild extends GenericCrafterBuild implements HasPressure {
		PressureModule pressure = new PressureModule();

		public float efficiencyMultiplier() {
			float val = 1;
			if (!useConsumerMultiplier) return val;
			for (Consume consumer : consumers) {
				val *= consumer.efficiencyMultiplier(this);
			}
			return val;
		}

		@Override public float efficiencyScale() {
			return super.efficiencyScale() * efficiencyMultiplier();
		}

		@Override public float getProgressIncrease(float baseTime) {
			return super.getProgressIncrease(baseTime) * efficiencyMultiplier();
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

		@Override
		public void read(Reads read, byte revision) {
			super.read(read, revision);
			pressure.read(read);
		}

		@Override
		public void updateTile() {
			super.updateTile();
			updatePressure();
		}

		@Override
		public void write(Writes write) {
			super.write(write);
			pressure.write(write);
		}
	}
}
