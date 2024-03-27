package omaloon.world.blocks.production;

import arc.util.io.*;
import mindustry.world.blocks.production.*;
import mindustry.world.consumers.*;
import omaloon.world.blocks.defense.Deflector.*;
import omaloon.world.blocks.production.OlGenericCrafter.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

public class OlDrill extends Drill {
	public PressureConfig pressureConfig = new PressureConfig();

	public boolean useConsumerMultiplier = true;

	public OlDrill(String name) {
		super(name);
	}

	@Override
	public void init() {
		super.init();
		pressureConfig.linkBlackList.add(DeflectorBuild.class, OlGenericCrafterBuild.class);
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

	public class OlDrillBuild extends DrillBuild implements HasPressure {
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
			updateDeath();
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
