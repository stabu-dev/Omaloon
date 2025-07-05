package omaloon.world.meta;

import mindustry.world.*;
import mindustry.world.meta.*;
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
     * Group of fluid section. Connected buildings with the same group will act as one singular tank.
     * @apiNote A null group will not create tanks with nearby buildings.
     */
    public TankGroup group;

    public void addBars(Block block){

    }

    public void addStats(Block block, Stats stats){

    }
}
