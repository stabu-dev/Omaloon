package omaloon.world.meta;

import arc.*;
import arc.graphics.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import omaloon.world.blocks.liquid.*;
import omaloon.world.interfaces.*;

public class PressureConfig{
    /**
     * Group used for the creation of sections. if null, this block will be a section containing only itself.
     */
    public FluidGroup fluidGroup;

    /**
     * If true, this block's section will be able to receive fluids.
     */
    public boolean acceptsPressure = true;
    /**
     * If true, this block's section will be able to output fluids.
     */
    public boolean outputsPressure = true;
    /**
     * Determines whether linkList will function as a whitelist
     */
    public boolean isWhitelist = false;

    /**
     * Damage applied each tick if pressure is higher than maxPressure.
     */
    public float overPressureDamage = 0.33f;
    /**
     * Damage applied each tick if pressure is lower than minPressure.
     */
    public float underPressureDamage = 0.66f;
    /**
     * Maximum pressure that this block can contain before it over-pressurizes.
     */
    public float maxPressure = 50;
    /**
     * Minimum pressure that this block can contain before it under-pressurizes.
     */
    public float minPressure = -50f;

    /**
     * Standard capacity for this block. Does not define max amount that this can hold. That is defined by maxPressure and minPressure.
     */
    public float fluidCapacity = 8;

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

    public void addStats(Stats stats){
        stats.add(OlStats.fluidCapacity, StatValues.number(fluidCapacity / 8f, OlStats.blocksCubed));

        stats.add(OlStats.maxPressure, OlStats.pressure(maxPressure, true));
        stats.add(OlStats.minPressure, OlStats.pressure(minPressure, true));
    }

    public void addBars(Block block){
        block.removeBar("liquid");
//		block.addBar("pressure-liquid", (Building entity) -> new Bar(
//				() -> {
//					HasPressure build = (HasPressure)entity;
//					Liquid current = entity.liquids != null ? entity.liquids.current() : null;
//					String liquidName = (current == null || entity.liquids.get(current) <= 0.001f)
//							? Core.bundle.get("bar.air")
//							: current.localizedName;
//
//					return Core.bundle.format("bar.pressure-liquid",
//							liquidName,
//							Mathf.round(build.getPressure(), 1),
//							Mathf.round(build.getPressure() > 0 ? build.pressureConfig().maxPressure : build.pressureConfig().minPressure, 1));
//				},
//				() -> {
//					Liquid current = entity.liquids != null ? entity.liquids.current() : null;
//					return current != null && entity.liquids.get(current) > 0.001f ? current.color : Color.lightGray;
//				},
//				() -> {
//					Liquid current = entity.liquids != null ? entity.liquids.current() : null;
//					return current != null ? entity.liquids.get(current) / block.liquidCapacity : 0f;
//				}
//		));
        block.addBar("pressure-liquid", (Building entity) -> new Bar(
            () -> {
                HasPressure build = (HasPressure)entity;
                Liquid current = build.pressure().getMain();

                if(current == null) return Core.bundle.get("bar.air") + Strings.autoFixed(build.pressure().air, 2);
                return Core.bundle.format("bar.pressure-liquid",
                    current.localizedName,
                    Strings.autoFixed(build.pressure().liquids[current.id], 2),
                    Strings.autoFixed(build.pressure().air, 2)
                );
            },
            () -> {
                HasPressure build = (HasPressure)entity;
                Liquid current = build.pressure().getMain();
                return current != null ? current.color : Color.lightGray;
            },
            () -> {
                HasPressure build = (HasPressure)entity;
                Liquid current = build.pressure().getMain();
                return current != null ? Mathf.clamp(build.pressure().liquids[current.id] / (build.pressure().liquids[current.id] + build.pressure().air)) : 0;
            }
        ));
    }

    public boolean isAllowed(Block block){
        for(Class<?> alwaysAllowedClass : alwaysAllowed){
            if(alwaysAllowedClass.isAssignableFrom(block.getClass())){
                return true;
            }
        }

        boolean inList = linkList.contains(block);
        return isWhitelist == inList;
    }
}