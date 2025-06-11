package omaloon.world.meta;

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
}
