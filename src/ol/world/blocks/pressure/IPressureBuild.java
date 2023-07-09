package ol.world.blocks.pressure;

import arc.struct.Seq;
import mindustry.gen.Building;
import ol.pressure.ConsumePressure;
import ol.pressure.PUtil;
import ol.pressure.PressureGraph;
import ol.pressure.PressureModule;
import org.jetbrains.annotations.NotNull;

public interface IPressureBuild {
    @NotNull Building self();

    float tier();
    boolean hasPressure();
    PressureModule pressure();
    ConsumePressure consPressure();

    default float overloadDamageMultiplayer() {
        return PUtil.DEFAULT_ODM;
    }

    default void handleOverload() {
        var p = pressure();
        if(p != null && (p.status > 1 || p.status < -1)) {
            float t = p.status < 0 ? -p.status : p.status;
            var building = self();
            building.damage((t - 1) * overloadDamageMultiplayer() * building.maxHealth());
            //TODO pressure push fx
        }
    }

    default boolean isPressureBlock() {
        return true;
    }

    default Seq<Building> getLinks() {
        return self().proximity;
    }

    default boolean connectable(IPressureBuild build) {
        return PUtil.isPressureBlock(build) && build.self().team == self().team() && PUtil.compareTier(tier(), build.tier());
    }

    default float pressureStoredLocal() { //IT`S MUST BE FINAL (!!!)
        return pressure().status * consPressure().capacityFor(self());
    }

    default void destroyD() {
        var p = pressure();
        if(p != null) {
            p.graph.remove(self());
        }
    }

    default void pickedUpD() {
        var p = pressure();
        if(p != null) {
            p.graph = new PressureGraph();
            p.links.clear();
            p.status = 0;
        }
    }

    default void placedD() {
        var sp = pressure();
        if(sp != null) {
            sp.graph.add(self(), false);
            getLinks().forEach(b -> {
                if(b instanceof IPressureBuild build && connectable(build) && build.connectable(this)) {
                    sp.links.add(build.self().pos());
                    var op = build.pressure();
                    if(op != null) {
                        if(!op.graph.contains(build.self())) {
                            sp.graph.add(build.self(), false);
                        }
                        sp.graph.add(op.graph);
                        op.graph = sp.graph;
                    }
                }
            });
        }
    }

    interface Block {
        default boolean isPressure() {
            return true;
        }
    }
}