package omaloon.world.blocks.production;

import arc.struct.*;
import arc.util.io.*;
import mindustry.world.blocks.production.*;
import mindustry.world.consumers.*;
import omaloon.world.blocks.defense.Shelter.*;
import omaloon.world.blocks.production.OlDrill.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

public class OlGenericCrafter extends GenericCrafter {
	public PressureConfig pressureConfig = new PressureConfig();

	public boolean useConsumerMultiplier = true;

	public float outputPressure = -1;

	public OlGenericCrafter(String name) {
		super(name);
	}

	@Override
	public void init() {
		super.init();
		pressureConfig.linkBlackList.add(ShelterBuild.class, OlGenericCrafterBuild.class, OlDrillBuild.class);
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
		if (outputPressure < 0) stats.add(OlStats.outputPressure, outputPressure, OlStats.pressureUnits);
	}

	public class OlGenericCrafterBuild extends GenericCrafterBuild implements HasPressure {
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

		@Override public Seq<HasPressure> nextBuilds(boolean flow) {
			return HasPressure.super.nextBuilds(flow).retainAll(build -> !(build instanceof OlGenericCrafter));
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
			dumpPressure();
		}

		@Override
		public void write(Writes write) {
			super.write(write);
			pressure.write(write);
		}
	}
}
