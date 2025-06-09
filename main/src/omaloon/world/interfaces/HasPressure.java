package omaloon.world.interfaces;

import arc.struct.*;
import mindustry.gen.*;
import omaloon.world.graph.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

/**
 * @author Liz
 */
public interface HasPressure{
    /**
     * Mutually exclusive static connection, should not be influenced by current pressure in a Building.
     */
    static boolean connects(HasPressure from, HasPressure to) {
        return from.connects(to) && to.connects(from);
    }

    /**
     * All builds that this block will connect to. By default it returns all blocks available in the Building's {@code proximity} seq.
     */
    default Seq<HasPressure> connections() {
        return toBuilding().proximity
        .select(b -> b instanceof HasPressure p && connects(this, p))
        .as();
    }

    /**
     * One sided static connection, should not be influenced by current pressure in a Building.
     */
    default boolean connects(HasPressure to) {
        return
        pressureConfig().hasPressure &&
        to.toBuilding().team == toBuilding().team;
    }

    /**
     * Called whenever a new building is added / removed from this Building's graph.
     */
    default void onPressureGraphUpdate() {

    }

    PressureModule pressure();
    PressureConfig pressureConfig();
    default PressureGraph pressureGraph() {
        return pressure().graph;
    }

    default Building toBuilding() {
        return (Building) this;
    }
}
