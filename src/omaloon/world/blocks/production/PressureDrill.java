package omaloon.world.blocks.production;

import arc.util.io.*;
import mindustry.world.blocks.production.*;
import mindustry.world.consumers.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

public class PressureDrill extends Drill {
	public PressureConfig pressureConfig = new PressureConfig();

	public boolean useConsumerMultiplier = true;

	public PressureDrill(String name) {
		super(name);
		pressureConfig.isWhitelist = true;
	}

	@Override
	public void init() {
		super.init();

		if (pressureConfig.fluidGroup == null) pressureConfig.fluidGroup = FluidGroup.drills;
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

	public class PressureDrillBuild extends DrillBuild implements HasPressureImpl {

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
	}
}
