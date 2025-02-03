package omaloon.world.interfaces;

import arc.util.io.Reads;
import arc.util.io.Writes;
import omaloon.annotations.AutoImplement;
import omaloon.world.meta.PressureConfig;
import omaloon.world.modules.PressureModule;

import static omaloon.annotations.AutoImplement.Inject.InjectPosition.AfterSuper;


@AutoImplement
public interface HasPressureImpl extends HasPressure {

    PressureModule __pressure__ = new PressureModule();

    default PressureModule pressure() {
        return __pressure__;
    }

    @AutoImplement.Inject(AfterSuper)
    default void write(Writes writes) {
        __pressure__.write(writes);
    }

    @AutoImplement.Inject(AfterSuper)
    default void read(Reads reads, byte b) {
        __pressure__.read(reads);
    }

    @Override
    default PressureConfig pressureConfig() {
        return AutoImplement.Util.Param("pressureConfig", "pressureConfig");
    }
}
