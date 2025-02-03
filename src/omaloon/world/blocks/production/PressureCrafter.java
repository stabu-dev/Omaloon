package omaloon.world.blocks.production;

import arc.util.*;
import arc.util.io.*;
import asmlib.annotations.DebugAST;
import mindustry.world.blocks.production.*;
import mindustry.world.consumers.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

public class PressureCrafter extends GenericCrafter {
	public PressureConfig pressureConfig = new PressureConfig();

	public boolean useConsumerMultiplier = true;

	public float outputPressure = 0;

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
		if (outputPressure != 0) stats.add(OlStats.outputPressure, Strings.autoFixed(outputPressure, 2), OlStats.pressureUnits);
	}
@DebugAST
	public class PressureCrafterBuild extends GenericCrafterBuild implements HasPressureImpl {

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
		public void updateTile() {
			super.updateTile();
			updatePressure();
			dumpPressure();
		}
	}
}
