package omaloon.world.interfaces;

import mindustry.gen.*;
import omaloon.world.graph.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

/**
 * @author Liz
 */
public interface HasPressure{
    /**
     * Mutually exclusive static connection, should not be influenced by current pressure in a build.
     */
    static boolean connects(HasPressure from, HasPressure to) {
        return from.connects(to) && to.connects(from);
    }

    default Building toBuilding() {
        return (Building) this;
    }

    /**
     * One sided static connection, should not be influenced by current pressure in a build.
     */
    default boolean connects(HasPressure to) {
        return pressureConfig().hasPressure;
    }

    /**
     * All builds that this block will connect to. By default it returns all blocks available in the Building's {@code proximity} seq.
     */
    default HasPressure[] connections() {
        return toBuilding().proximity
        .select(b -> b instanceof HasPressure p && connects(this, p))
        .toArray(HasPressure.class);
    }

    PressureModule pressure();
    PressureConfig pressureConfig();
    default PressureGraph pressureGraph() {
        return pressure().graph;
    }
}
