package omaloon.ai.drone;

import arc.graphics.g2d.*;
import arc.struct.Queue;
import arc.util.Time;
import arc.util.Tmp;
import arclibrary.graphics.*;
import mindustry.*;
import mindustry.content.Fx;
import mindustry.entities.units.BuildPlan;
import mindustry.gen.Call;
import mindustry.gen.Unit;
import mindustry.graphics.*;
import mindustry.type.Item;
import mindustry.ui.*;
import mindustry.world.Tile;
import mindustry.world.blocks.ConstructBlock;
import mindustry.world.blocks.ConstructBlock.*;
import mindustry.world.blocks.storage.CoreBlock;
import ol.gen.OlCall;
import omaloon.ai.DroneAI;
import omaloon.utils.*;

import static mindustry.Vars.mineTransferRange;

public class UtilityDroneAI extends DroneAI {
    public float mineRangeScl = 0.75f;
    public float buildRangeScl = 0.75f;
    public float buildRangeSclInv =1-buildRangeScl;

    public UtilityDroneAI(Unit owner) {
        super(owner);
    }

    @Override
    public void updateMovement() {
        unit.updateBuilding = false;
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
            core = owner.closestCore();
            if(owner.within(core,mineTransferRange)){
                OlCall.chainTransfer(unit.stack.item,unit.x,unit.y,owner,core);
            }else{
                for (int i = 0; i < unit.stack.amount; i++) {
                    Call.transferItemToUnit(unit.stack.item, unit.x, unit.y, owner);
                }
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
        if (!owner.updateBuilding) return false;

        CoreBlock.CoreBuild core = unit.team.core();


        int totalSkipped = 0;
        if(DebugDraw.isDraw()){
            DrawText.defaultFont= Fonts.def;
            DebugDraw.request(()->{
                Draw.draw(Layer.end,()->{
                    Draw.color(Pal.heal);
                    Lines.circle(owner.x,owner.y,owner.type.buildRange);
                    Draw.color(Pal.berylShot);
                    Lines.circle(unit.x,unit.y,unit.type.buildRange);
                    EFill.polyCircle(unit.x,unit.y, Vars.tilesize/4f);
                });
            });
            for(int i = 0; i < plans.size; i++){
                BuildPlan plan = plans.get(i);
                int i1 = i;
                DebugDraw.request(()->{
                    Draw.draw(Layer.end,()->{
                        DrawText.drawText(plan,""+i1);
                    });
                });
            }
        }
        //IMPORTANT unit.plans.size must be 0
        for (int i = 0; i < plans.size; i++) {
            BuildPlan buildPlan = plans.first();

            if (!unit.shouldSkip(buildPlan, core) && owner.within(buildPlan, owner.type.buildRange)) {
//                moveTo(buildPlan.tile(), unit.type.buildRange * buildRangeScl, 30f);
                break;
            }
            plans.removeFirst();
            Fx.fireSmoke.at(buildPlan);
            if(buildPlan.stuck){
//                Fx.heal.at();
            }
            plans.addLast(buildPlan);
            totalSkipped++;
        }
        var currentPlan=plans.first();

        boolean withinOwner= owner.within(currentPlan,owner.type.buildRange);
        boolean isConstructing =withinOwner && currentPlan.tile().build instanceof ConstructBuild;
        if (totalSkipped == plans.size && !isConstructing)
            return false;

        float myRange =  unit.type.buildRange;
        moveTo(currentPlan.tile(), myRange * buildRangeScl, 30f);
        if(!unit.within(currentPlan, myRange - myRange * buildRangeSclInv / 2))
            return true;
        unit.plans = plans;
        unit.updateBuilding = true;
        unit.updateBuildLogic();
        unit.lookAt(currentPlan);
        for (BuildPlan plan : plans) {
            if (plan.tile().build instanceof ConstructBlock.ConstructBuild it) {
                if (it.progress > 0 && !plan.initialized) {
                    plan.initialized = true;
                }
            }
        }
        unit.updateBuilding = false;
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
