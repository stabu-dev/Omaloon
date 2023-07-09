package ol.pressure;

import arc.struct.Seq;
import mindustry.gen.Building;
import mindustry.world.blocks.power.PowerGraph;
import ol.world.blocks.pressure.IPressureBuild;
import ol.world.blocks.pressure.PressureBlock.PressureBuild;

public class PressureGraph {
    //WARNING: DO NOT CHANGE VALUES IN THESE FIELDS IF YOU DON`T KNOW WHAT YOU'RE DOING
    public Seq<Building> consumerAbleBuilds = new Seq<>();
    public Seq<PressureModule> pressureModules = new Seq<>();
    public Seq<Building> pressureBuilds = new Seq<>();
    public Seq<Building> builds = new Seq<>();

    //WARNING: EMERGENCY USE ONLY
    public void reloadCaches() {
        consumerAbleBuilds.clear();
        pressureModules.clear();
        pressureBuilds.clear();
        Seq<Building> reserve = builds.copy();
        builds.clear();
        add(reserve);
    }

    public void add(Building build) {
        add(build, true);
    }

    public void add(Building build, boolean set) {
        if(!PUtil.isPressureBlock(build) || !(build instanceof IPressureBuild)) {
            return;
        }

        builds.add(build);
        var inst = (IPressureBuild) build;
        var pressure = inst.pressure();
        boolean _pressure = pressure != null;
        var cons = inst.consPressure();

        if(_pressure) {
            pressureModules.add(pressure);
            if(set) {
                pressure.graph = this;
            }
        }

        if(cons != null) {
            consumerAbleBuilds.add(build);
            if(_pressure) {
                pressureBuilds.add(build);
            }
        }
    }

    public void add(Seq<Building> builds) {
        builds.forEach(this::add);
    }

    public void add(PressureGraph pressureGraph) {
        add(pressureGraph.builds);
        pressureGraph.clear();
    }

    public void remove(Building building) {
        remove(building, true);
    }

    public void remove(Building building, boolean identity) {
        if(PUtil.isPressureBlock(building)) {
            if(builds.contains(building, identity)) {
                builds.remove(building, identity);
            }
            if(pressureBuilds.contains(building, identity)) {
                pressureBuilds.remove(building, identity);
            }
            if(consumerAbleBuilds.contains(building, identity)) {
                consumerAbleBuilds.remove(building, identity);
            }
            var pressure = ((IPressureBuild) building).pressure();
            if(pressure != null && pressureModules.contains(pressure, identity)) {
                pressureModules.remove(pressure, identity);
            }
        }
    }

    public boolean contains(Building build) {
        return contains(build, true);
    }

    public boolean contains(Building build, boolean identity) {
        return builds.contains(build, identity) || pressureBuilds.contains(build, identity) ||
                consumerAbleBuilds.contains(build, identity);
    }

    public float getPressureCapacity() {
        return consumerAbleBuilds.sumf(build -> ((IPressureBuild) build).consPressure().capacityFor(build));
    }

    public float getPressureStoredDelta() {
        return pressureModules.sumf(mod -> mod.status) / pressureModules.size;
    }

    public float getPressureStored() {
        return getPressureStoredDelta() * getPressureCapacity();
    }

    public float getTotalRequestedPressure() {
        return pressureBuilds.sumf(b -> {
            var i = (IPressureBuild) b;
            return i.consPressure().requestedPressure(i);
        });
    }

    public void dischargePressure(float amount) {
        //TODO remake this method
        if(amount < 0) {
            pushPressure(-amount);
            return;
        }
        if(amount == 0) return;
        float tot = 0;
        float delta = amount / pressureBuilds.size;
        for(var build : pressureBuilds) {
            var b = (IPressureBuild) build;
            float cap = b.consPressure().capacityFor(build);
            float removed = Math.min(delta, b.pressureStoredLocal());
            if(removed != delta) {
                delta = (amount - removed) / pressureBuilds.size;
            }
            tot += removed;
            b.pressure().status -= removed / cap;
        }
        if(tot != amount) {
            float excess = amount - tot;
            delta = excess / pressureBuilds.size;
            for(var build : pressureBuilds) {
                ((IPressureBuild) build).pressure().status -= delta;
            }
        }
    }

    public void pushPressure(float amount) {
        //TODO remake this method
        if(amount < 0) {
            dischargePressure(-amount);
            return;
        }
        if(amount == 0) return;
        float tot = 0;
        float delta = amount / pressureBuilds.size;
        for(var build : pressureBuilds) {
            var b = (IPressureBuild) build;
            float cap = b.consPressure().capacityFor(build);
            float accepted = Math.min(delta, cap - b.pressureStoredLocal());
            if(accepted != delta) {
                delta = (amount - accepted) / pressureBuilds.size;
            }
            tot += accepted;
            b.pressure().status += accepted / cap;
        }
        if(tot != amount) {
            float excess = amount - tot;
            delta = excess / pressureBuilds.size;
            for(var build : pressureBuilds) {
                ((IPressureBuild) build).pressure().status += delta;
            }
        }
    }

    public boolean isOverload() {
        float delta = getPressureStoredDelta();
        return delta > 1 || delta < -1;
    }

    public void clear() {
        consumerAbleBuilds.clear();
        pressureModules.clear();
        pressureBuilds.clear();
        builds.clear();
    }
}