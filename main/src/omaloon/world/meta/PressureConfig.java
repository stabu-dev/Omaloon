package omaloon.world.meta;

import arc.*;
import arc.graphics.*;
import arc.math.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import omaloon.ui.elements.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.PressureTank.*;

public class PressureConfig{
    /**
     * Whether or not the block supports pressurized fluids.
     * @apiNote when false, the resulting building will not have a PressureModule
     */
    public boolean hasPressure = false;

    /**
     * Whether or not the block accepts or outputs pressure
     * @apiNote Should not define static connections, as most blocks do not distinguish accepting fluids or outputing fluids.
     */
    public boolean acceptsPressure, outputsPressure;

    /**
     * Internal fluid capacity of the block. Does not define how much fluid it can contain. But is used instead to determine pressure.
     */
    public float fluidCapacity = 8f;

    /**
     * Minimum or maximum pressure of this block. Going beyond this will cause the building to be damaged.
     */
    public float minPressure = -50f, maxPressure = 50;

    /**
     * Group of fluid section. Connected buildings with the same group will act as one singular tank.
     * @apiNote A null group will not create tanks with nearby buildings.
     */
    public TankGroup group;

    public void addBars(Block block){
        if (!hasPressure) return;
        block.removeBar("liquid");
        block.addBar("omaloon-fluid-bar", build -> {
            HasPressure e = (HasPressure) build;
            Liquid liq = e.pressure().getMain();
            return new Bar(
                () -> liq == null ?
                Core.bundle.format("bar.omaloon-air-bar", OlStats.formatValue(e.getFluid(liq), 2, false)) :
                Core.bundle.format("bar.omaloon-fluid-bar", liq.localizedName, OlStats.formatValue(e.getFluid(liq), 2, false), OlStats.formatValue(e.getFluid(null), 2, false)),
                () -> liq == null ? Color.white : liq.color,
                () -> liq == null ? 0f : e.getFluid(liq) / Math.max(1f, Math.abs(e.getFluid(null)))
            );
        });
        block.addBar("omaloon-pressure-bar", build -> {
            HasPressure e = (HasPressure) build;
            return new CenterBar(
                () -> Core.bundle.format("bar.omaloon-pressure-bar", OlStats.formatValue(e.pressure().sumPressure(), 2, false)),
                () -> e.pressure().sumPressure() > 0 ? Color.white : Color.gray,
                () -> Mathf.map(e.pressure().sumPressure(), minPressure, maxPressure, -1f, 1f)
            );
        });
    }

    public void addStats(Block block, Stats stats){
        if (!hasPressure) return;
        stats.remove(Stat.liquidCapacity);
        stats.add(Stat.liquidCapacity, fluidCapacity, StatUnit.liquidUnits);

        stats.add(OlStats.minPressure, OlStats.formatValue(minPressure, 2, false), OlStats.pressureUnit);
        stats.add(OlStats.maxPressure, OlStats.formatValue(maxPressure, 2, false), OlStats.pressureUnit);
    }
}
