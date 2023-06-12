package ol.entity;

import arc.util.Log;
import arc.util.io.Reads;
import arc.util.io.Writes;
import me13.core.units.XeonUnitEntity;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.type.ItemStack;
import mindustry.world.blocks.production.Drill;
import mindustry.world.meta.BlockStatus;
import ol.ai.MiningUnitAI;
import ol.world.blocks.storage.MiningUnloadPoint;
import ol.world.unit.MiningUnitType;

public class MiningUnitEntity extends XeonUnitEntity {
    public Drill.DrillBuild instance;

    public MiningUnitType getType() {
        return (MiningUnitType) type;
    }

    public void unloadTo(MiningUnloadPoint.MiningUnloadPointBuild build) {
        if(isEject() && build != null && build.canAcceptItem(stack.item) && !isBlock()) {
            int count = Math.min(build.getMaximumAccepted(stack.item) - build.items.get(stack.item), stack.amount);
            build.items.add(stack.item, count);
            count = stack.amount - count;
            stack = new ItemStack(stack.item, count);
        }
    }

    public void eject() {
        if(isBlock()) {
            stack = getDrillItem();
            instance.tile.setNet(Blocks.air);
            instance = null;
        }
    }

    public ItemStack getDrillItem() {
        if(isBlock()) {
            var it = instance.dominantItem;
            return new ItemStack(it, instance.items.get(it));
        } else {
            return null;
        }
    }

    public Drill getDrill() {
        return getType().placedDrill;
    }

    public boolean isBlock() {
        return instance != null;
    }

    public boolean isFull() {
        return isBlock() && instance.status() == BlockStatus.noOutput;
    }

    public boolean isEject() {
        return stack != null && stack.amount > 0;
    }

    @Override
    public void draw() {
        if(!isBlock()) {
            super.draw();
        }
    }

    @Override
    public boolean hittable() {
        return super.hittable() && !isBlock();
    }

    @Override
    public void write(Writes write) {
        super.write(write);
        write.i(instance == null ? -1 : instance.pos());
    }

    @Override
    public void read(Reads read) {
        super.read(read);
        int pos = read.i();
        instance = pos == -1 ? null : (Drill.DrillBuild) Vars.world.tile(pos).build;
        if(instance != null && controller instanceof MiningUnitAI ai) {
            ai.target = instance.tile;
        }
    }

    @Override
    public void killed() {
        super.killed();
        if(isBlock()) {
            instance.kill();
        }
    }
}
