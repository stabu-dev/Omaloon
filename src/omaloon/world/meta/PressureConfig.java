package omaloon.world.meta;

import arc.*;
import arc.graphics.Color;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.Building;
import mindustry.graphics.*;
import mindustry.type.Liquid;
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
		maxPressure = 50,
		minPressure = -50f;

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
		stats.add(OlStats.maxPressure, Mathf.round(maxPressure, 2), OlStats.pressureUnits);
		stats.add(OlStats.minPressure, Mathf.round(minPressure, 2), OlStats.pressureUnits);
	}

	public void addBars(Block block) {
		block.removeBar("liquid");
		block.addBar("pressure-liquid", (Building entity) -> new Bar(
				() -> {
					HasPressure build = (HasPressure)entity;
					Liquid current = entity.liquids.current();
					String liquidName = current == null || entity.liquids.get(current) <= 0.001f ? Core.bundle.get("bar.air") : current.localizedName;
					return Core.bundle.format("bar.pressure-liquid",
							liquidName,
							Mathf.round(build.getPressure(), 1),
							Mathf.round(build.getPressure() > 0 ? build.pressureConfig().maxPressure : build.pressureConfig().minPressure, 1));
				},
				() -> {
					Liquid current = entity.liquids.current();
					return current != null && entity.liquids.get(current) > 0.001f ? current.color : Color.clear;
				},
				() -> {
					Liquid current = entity.liquids.current();
					return current != null ? entity.liquids.get(current) / block.liquidCapacity : 0f;
				}
		));
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