package omaloon.world.blocks.production;

import arc.util.*;
import arc.util.io.*;
import mindustry.type.*;
import mindustry.world.blocks.production.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

public class PressureCrafter extends GenericCrafter {
	public PressureConfig pressureConfig = new PressureConfig();

	public boolean useConsumerMultiplier = true;

	/**
	 * Set this to false every time you want the outputs to use the pressure liquid system instead.
	 * Alternatively, creating ConsumeFluids with negative values for it's amount will work too.
	 */
	public boolean useVanillaLiquids = true;

	/**
	 * Internal variable used to output pressurized liquids, Do not modify manually.
	 */
	private @Nullable LiquidStack[] outputPressurizedLiquids;

	public float outputAir;

	public PressureCrafter(String name) {
		super(name);
		pressureConfig.isWhitelist = true;
	}

	@Override
	public void init() {
		if (!useVanillaLiquids) {
			outputPressurizedLiquids = outputLiquids;
			outputLiquids = null;
		}

		super.init();
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

		if(outputPressurizedLiquids != null) {
			stats.add(Stat.output, StatValues.liquids(1f, outputPressurizedLiquids));
		}
		if (outputAir > 0) {
			stats.add(Stat.output, OlStats.fluid(null, outputAir, 1f, true));
		}
	}

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
		public boolean shouldConsume() {
			if(outputPressurizedLiquids != null && !ignoreLiquidFullness) {
				boolean allFull = true;
				if (pressure.get(null) >= pressureConfig.fluidCapacity - 0.001f) {
					if (!dumpExtraLiquid) return false;
				} else allFull = false;
				for(var output : outputPressurizedLiquids) {
					if(pressure.get(output.liquid) >= pressureConfig.fluidCapacity - 0.001f) {
						if(!dumpExtraLiquid) return false;
					} else allFull = false;
				}

				//if there is no space left for any liquid, it can't reproduce
				if(allFull) return false;
			}
			return super.shouldConsume();
		}

		@Override
		public void updateTile() {
			super.updateTile();
			if(efficiency > 0) {
				float inc = getProgressIncrease(1f);
				if (outputPressurizedLiquids != null) for(var output : outputPressurizedLiquids) addFluid(output.liquid, output.amount * inc);
				if (outputAir > 0) addFluid(null, outputAir * inc);
			}
		}
	}
}
