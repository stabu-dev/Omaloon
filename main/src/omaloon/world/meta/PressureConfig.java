package omaloon.world.meta;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.math.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import omaloon.ui.elements.*;
import omaloon.world.consumers.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.PressureTank.*;

public class PressureConfig{
    /**
     * Whether the block supports pressurized fluids.
     * @apiNote when false, the resulting building will not have a PressureModule
     */
    public boolean hasPressure = false;

    /**
     * Whether the block accepts or outputs pressure
     * @apiNote Should not define static connections, as most blocks do not distinguish accepting fluids or outputting fluids.
     */
    public boolean acceptsPressure, outputsPressure;

    /**
     * Whether the fluids inside this block react to one another.
     */
    public boolean fluidReacts;

    /**
     * Internal fluid capacity of the block. Does not define how much fluid it can contain. But is used instead to determine pressure.
     */
    public float fluidCapacity = 8f;

    /**
     * Minimum or maximum pressure of this block. Going beyond this will cause the building to be damaged.
     */
    public float minPressure = -50f, maxPressure = 50;

    /**
     * Damage dealt to certain buildings if pressure is over maxPressure or under minPressure.
     */
    public float underPressureDamage = 0.1f, overPressureDamage = 0.1f;

    /**
     * Group of fluid section. Connected buildings with the same group will act as one singular tank.
     * @apiNote A null group will not create tanks with nearby buildings.
     */
    public TankGroup group;

    /**
     * An extra filter that allows/denies connections based on block types.
     * should return true if the block type can connect.
     */
    public Boolf<Block> blockFilter = block -> true;

    public void addBars(Block block){
        if(!hasPressure) return;
        block.removeBar("liquid");

        boolean added = false;

        // add bars for each consumed fluid
        for(Consume cons : block.consumers){
            if(cons instanceof ConsumeFluid consFluid && block.consumers.length > 1){
                String barName = "omaloon-fluid-bar-" + (consFluid.fluid == null ? "air" : consFluid.fluid);

                block.addBar(barName, build -> {
                    HasPressure e = (HasPressure)build;
                    return new Bar(
                    () -> e.pressure().getMain() == null ?
                    Core.bundle.format("bar.omaloon-air-bar", OlStats.formatValue(e.getFluid(e.pressure().getMain()), 2, false)) :
                    Core.bundle.format("bar.omaloon-fluid-bar", e.pressure().getMain().localizedName, OlStats.formatValue(e.getFluid(e.pressure().getMain()), 2, false), OlStats.formatValue(e.getFluid(null), 2, false)),
                    () -> e.pressure().getMain() == null ? Color.white : e.pressure().getMain().color,
                    () -> e.pressure().getMain() == null ? 0f : Mathf.clamp(e.getFluid(e.pressure().getMain()))
                    );
                });

                added = true;
            }
        }

        // default to generic bar if there's only one liquid consumed
        if(!added){
            block.addBar("omaloon-fluid-bar", build -> {
                HasPressure e = (HasPressure)build;
                return new Bar(
                () -> e.pressure().getMain() == null ?
                Core.bundle.format("bar.omaloon-air-bar", OlStats.formatValue(e.getFluid(e.pressure().getMain()), 2, false)) :
                Core.bundle.format("bar.omaloon-fluid-bar", e.pressure().getMain().localizedName, OlStats.formatValue(e.getFluid(e.pressure().getMain()), 2, false), OlStats.formatValue(e.getFluid(null), 2, false)),
                () -> e.pressure().getMain() == null ? Color.white : e.pressure().getMain().color,
                () -> Mathf.clamp(e.pressure().getMain() == null ? 0f : e.getFluid(e.pressure().getMain()) / Math.max(1f, e.getFluid(e.pressure().getMain()) + Math.abs(e.getFluid(null))))
                );
            });
        }else{
            block.addBar("omaloon-fluid-bar-air", build -> {
                HasPressure e = (HasPressure)build;
                return new Bar(
                () -> Core.bundle.format("bar.omaloon-air-bar", OlStats.formatValue(e.getFluid(e.pressure().getMain()), 2, false)),
                () -> Color.white,
                () -> 0f
                );
            });
        }

        block.addBar("omaloon-pressure-bar", build -> {
            HasPressure e = (HasPressure)build;
            return new CenterBar(
            () -> Core.bundle.format("bar.omaloon-pressure-bar", OlStats.formatValue(e.pressure().sumPressure(), 2, false)),
            () -> e.pressure().sumPressure() > 0 ? Color.white : Color.gray,
            () -> Mathf.map(e.pressure().sumPressure(), minPressure, maxPressure, -1f, 1f)
            );
        });
    }

    public void addStats(Block block, Stats stats){
        if(!hasPressure) return;
        stats.remove(Stat.liquidCapacity);
        stats.add(Stat.liquidCapacity, fluidCapacity / 8f, OlStats.blocksCubed);

        stats.add(OlStats.minPressure, OlStats.number(minPressure, OlStats.pressureUnit, false));
        stats.add(OlStats.maxPressure, OlStats.number(maxPressure, OlStats.pressureUnit, false));
    }
}
