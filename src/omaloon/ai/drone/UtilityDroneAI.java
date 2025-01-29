package omaloon.ai.drone;

import arc.struct.Queue;
import arc.util.Nullable;
import arc.util.Tmp;
import mindustry.entities.units.BuildPlan;
import mindustry.game.Teams;
import mindustry.gen.Call;
import mindustry.gen.Unit;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock;
import omaloon.ai.DroneAI;

public class UtilityDroneAI extends DroneAI {
    public float mineRangeScl = 0.75f;
    public float buildRangeScl = 0.75f;

    public UtilityDroneAI(Unit owner) {
        super(owner);
    }

    @Override
    public void updateMovement() {
        unit.updateBuilding = true;
        unit.mineTile = null;
        tryTransportItems();

        if (tryBuild()) return;
        if (tryMine()) return;
        rally();
    }

    private void tryTransportItems() {
        if (unit.stack.amount <= 0) return;

        CoreBlock.CoreBuild core = unit.closestCore();

        if (core != null && !unit.within(core, owner.type.range)) {
            for (int i = 0; i < unit.stack.amount; i++) {
                Call.transferItemToUnit(unit.stack.item, unit.x, unit.y, owner);
            }
        } else {
            Call.transferItemTo(unit, unit.stack.item, unit.stack.amount, unit.x, unit.y, core);
        }
        unit.clearItem();
    }

    private boolean tryBuild() {
        Queue<BuildPlan> prev = unit.plans;
        prev.clear();

        Queue<BuildPlan> plans = owner.plans;
        if (plans.isEmpty()) return false;

        CoreBlock.CoreBuild core = unit.team.core();


        int totalSkipped = 0;
        //IMPORTANT unit.plans.size must be 0
        for (int i = 0; i < plans.size; i++) {
            BuildPlan buildPlan = plans.first();
            if (!unit.shouldSkip(buildPlan, core) && owner.within(buildPlan,owner.type.buildRange)) {
                moveTo(buildPlan.tile(), unit.type.buildRange * buildRangeScl, 30f);
                break;
            }
            plans.removeFirst();
            plans.addLast(buildPlan);
            totalSkipped++;
        }
        if (totalSkipped == plans.size) return false;

        unit.plans = plans;
        unit.updateBuildLogic();
        unit.plans = prev;
        return true;
    }

    protected boolean tryMine() {
        Tile mineTile = owner.mineTile();
        if (mineTile == null) return false;
        if (owner.stack.amount == owner.type.itemCapacity) return false;
        if ((owner.getMineResult(owner.mineTile) != owner.stack.item || owner.stack.amount <= 0) && (owner.stack.amount != 0))
            return false;


        if (!owner.within(mineTile.worldx(), mineTile.worldy(), owner.type.mineRange)) return false;
        unit.mineTile = owner.mineTile;
        moveTo(Tmp.v1.set(mineTile.worldx(), mineTile.worldy()), unit.type.mineRange * mineRangeScl, 30f);
        return true;
    }

}
