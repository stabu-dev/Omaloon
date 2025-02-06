package omaloon.ai.drone;

import arc.math.*;
import arc.util.*;
import mindustry.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import omaloon.ai.*;

public class UtilityDroneAI extends DroneAI{
    public float mineRangeScl = 0.75f;
    public float buildRangeScl = 0.75f;

    public @Nullable Teams.BlockPlan lastPlan;

    public UtilityDroneAI(Unit owner){
        super(owner);
    }

    @Override
    public void updateMovement(){
        unit.updateBuilding = true;

        if(owner.activelyBuilding()){
            unit.plans.clear();

            BuildPlan plan = owner.buildPlan();
            if(!unit.plans.contains(plan)) unit.plans.addFirst(plan);
            lastPlan = null;

            if(unit.buildPlan() != null){
                BuildPlan req = unit.buildPlan();

                if(!req.breaking && timer.get(timerTarget2, 40f)){
                    for(Player player : Groups.player){
                        if(player.isBuilder() && player.unit().activelyBuilding() && player.unit().buildPlan().samePos(req) && player.unit().buildPlan().breaking){
                            unit.plans.removeFirst();
                            unit.team.data().plans.remove(p -> p.x == req.x && p.y == req.y);
                            return;
                        }
                    }
                }

                boolean valid = !(lastPlan != null && lastPlan.removed) &&
                    ((req.tile() != null && req.tile().build instanceof ConstructBlock.ConstructBuild cons && cons.current == req.block) ||
                        (req.breaking ? Build.validBreak(unit.team(), req.x, req.y) :
                            Build.validPlace(req.block, unit.team(), req.x, req.y, req.rotation)));

                if(valid){
                    moveTo(req.tile(), unit.type.buildRange * buildRangeScl, 30f);
                }else{
                    unit.plans.removeFirst();
                    lastPlan = null;
                }
            }
        }else{
            unit.plans.clear();
            if(owner.mineTile() != null && owner.stack.amount != owner.type.itemCapacity &&
                ((owner.getMineResult(owner.mineTile) == owner.stack.item && owner.stack.amount > 0) ||
                    (owner.stack.amount == 0))){
                Tmp.v1.set(owner.mineTile.worldx(), owner.mineTile.worldy());
                if(unit.dst(Tmp.v1) <= unit.type.mineRange) unit.mineTile = owner.mineTile;
                moveTo(Tmp.v1, unit.type.mineRange * mineRangeScl, 30f);
            }else{
                unit.mineTile = null;
                rally();
            }
        }

        if(unit.stack.amount > 0){
            if(!unit.within(unit.closestCore(), owner.type.range) && unit.closestCore() != null){
                for(int i = 0; i < unit.stack.amount; i++){
                    Call.transferItemToUnit(unit.stack.item, unit.x, unit.y, owner);
                }
            }else{
                Call.transferItemTo(unit, unit.stack.item, unit.stack.amount, unit.x, unit.y, unit.closestCore());
            }
            unit.clearItem();
        }
    }

    //TODO: implement ignoring shouldSkip plans

    /** @return whether this plan should be skipped, in favor of the next one. */
    @SuppressWarnings({"unused"})
    boolean shouldSkip(BuildPlan plan, @Nullable Building core){
        //plans that you have at least *started* are considered
        if(Vars.state.rules.infiniteResources || unit.team.rules().infiniteResources || plan.breaking || core == null || plan.isRotation(unit.team) || (unit.isBuilding() && !unit.within(unit.plans.last(), owner.type.buildRange + unit.type.buildRange)))
            return false;

        return (plan.stuck && !core.items.has(plan.block.requirements)) || (Structs.contains(plan.block.requirements, i -> !core.items.has(i.item, Math.min(i.amount, 15)) && Mathf.round(i.amount * Vars.state.rules.buildCostMultiplier) > 0) && !plan.initialized);
    }
}
