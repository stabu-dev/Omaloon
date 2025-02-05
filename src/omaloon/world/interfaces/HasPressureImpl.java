package omaloon.world.interfaces;

import arc.util.io.Reads;
import arc.util.io.Writes;
import omaloon.annotations.AutoImplement;
import omaloon.world.meta.PressureConfig;
import omaloon.world.meta.PressureSection;
import omaloon.world.modules.PressureModule;

import static omaloon.annotations.AutoImplement.Inject.InjectPosition.*;


@AutoImplement
public interface HasPressureImpl extends HasPressure {

    PressureModule pressure = new PressureModule();

    default PressureModule pressure() {
        return pressure;
    }

    @AutoImplement.Inject(Tail)
    default void onProximityUpdate(){
        new PressureSection().mergeFlood(this);
    }

    @AutoImplement.Inject(Tail)
    default void updateTile() {
        updatePressure();
    }
    @AutoImplement.Inject(AfterSuper)
    default void write(Writes writes) {
        pressure.write(writes);
    }

    @AutoImplement.Inject(AfterSuper)
    default void read(Reads reads, byte b) {
        pressure.read(reads);
    }

    @Override
    default PressureConfig pressureConfig() {
        return AutoImplement.Util.Param("pressureConfig", "pressureConfig");
    }
}
