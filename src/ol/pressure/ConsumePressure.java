package ol.pressure;

import arc.func.Floatf;
import mindustry.gen.Building;
import mindustry.world.Block;
import mindustry.world.consumers.Consume;
import ol.world.blocks.pressure.IPressureBuild;
import ol.world.blocks.pressure.PressureBlock;
import ol.world.blocks.pressure.PressureCrafter;

public class ConsumePressure extends Consume {
    public Floatf<Building> capacity = (ignored) -> 0;
    public Floatf<Building> usage = (ignored) -> 0;

    public float usageFor(Building building) {
        return usage.get(building);
    }

    public float capacityFor(Building building) {
        return capacity.get(building);
    }

    public boolean bufferedFor(Building building) {
        return usageFor(building) == 0;
    }

    public float requestedPressure(IPressureBuild building) {
        return requestedPressure(building, building instanceof PressureCrafter.PressureCrafterBuild);
    }

    public float requestedPressure(IPressureBuild build, boolean isProducer) {
        var s = build.self();
        boolean buffered = bufferedFor(s);
        if(buffered) {
            return isProducer ? 0 : (1f - build.pressure().status) * build.consPressure().capacityFor(s);
        } else {
            return usageFor(s) * (s.shouldConsume() ? 1 : 0);
        }
    }

    @Override
    public void update(Building build) {
        var b = (IPressureBuild) build;
        float used = (usageFor(build) / capacityFor(build)) / 60;
        if(b.pressure().status >= used) {
            b.pressure().status -= used;
        }
    }

    @Override
    public void apply(Block block) {
        if(block instanceof PressureCrafter b) {
            b.hasPressure = true;
            b.consPressure = this;
        } else {
            var b = (PressureBlock) block;
            b.hasPressure = true;
            b.consPressure = this;
        }
    }

    @Override
    public float efficiency(Building build) {
        if(bufferedFor(build)) return 1;
        var b = (IPressureBuild) build;
        return Math.min(1f, (b.pressure().status * b.consPressure().capacityFor(build)) / usageFor(build));
    }
}