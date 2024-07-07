package omaloon.world.meta;

import arc.*;
import arc.math.*;
import arc.struct.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import omaloon.world.blocks.liquid.*;
import omaloon.world.interfaces.*;

public class PressureConfig {
	public boolean
		acceptsPressure = true,
		outputsPressure = true,
	/**
	 * Determines whether linkList will function as a whitelist
	 */
	    isWhitelist = false;
	public float
		overPressureDamage = 0.33f,
	    underPressureDamage = 0.66f,
		minPressure = -50,
		maxPressure = 50;

	/**
	 * List of blocks that are allowed/disallowed (depends on isWhitelist true/false) for pressure connections.
	 * <p>
	 * Note: If block A filters block B, also filter block A in block B's config.
	 * <p>
	 * Without mutual filtering, connections may be inconsistent:
	 * Block A might connect to Block B, but Block B might not connect to Block A.
	 * This can create confusing pressure networks where connections
	 * work from one side but not the other.
	 */
	public Seq<Block> linkList = new Seq<>();

	/**
	 * Always allowed block types
	 */
	private static final Class<?>[] alwaysAllowed = {
			PressureLiquidBridge.class,
			PressureLiquidConduit.class,
			PressureLiquidJunction.class,
			PressureLiquidPump.class,
			PressureLiquidValve.class
	};

	public void addStats(Stats stats) {
		stats.add(OlStats.minPressure, minPressure, OlStats.pressureUnits);
		stats.add(OlStats.maxPressure, maxPressure, OlStats.pressureUnits);
	}

	public void addBars(Block block) {
		block.addBar("pressure", entity -> {
			HasPressure build = (HasPressure) entity;
			return new Bar(
				() -> Core.bundle.get("pressure") + Mathf.round(build.getPressure()),
				() -> build.getPressure() > 0 ? Pal.accent : Pal.lancerLaser,
				() -> Math.abs(Mathf.map(build.getPressure(), minPressure, maxPressure, -1, 1))
			);
		});
	}

	public boolean isAllowed(Block block) {
		for (Class<?> alwaysAllowedClass : alwaysAllowed) {
			if (alwaysAllowedClass.isAssignableFrom(block.getClass())) {
				return true;
			}
		}

		boolean inList = linkList.contains(block);
		return isWhitelist == inList;
	}
}