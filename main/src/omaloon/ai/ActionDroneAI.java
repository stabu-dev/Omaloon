package omaloon.ai;

import arc.math.geom.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.blocks.storage.*;
import omaloon.entities.abilities.*;
import omaloon.gen.*;

import static mindustry.Vars.*;

public class ActionDroneAI extends AIController{
    protected Unit parent;
    protected Vec2 targetPos = new Vec2();
    protected Tile mineTile;

    // TODO multiplayer
    public void updateBuilding(){
        if(parent.activelyBuilding()){
            if(!unit.plans.contains(parent.buildPlan())){
                unit.plans.clear();
                unit.plans.add(parent.buildPlan());
            }
        }else unit.plans.clear();
    }

    public void updateIdle(){
        DroneAbility ability = (DroneAbility) parent.abilities[((DroneTetherc) unit).abilityIndex()];

        moveTo(targetPos.trns(parent.rotation - 90f, ability.idleX, ability.idleY).add(parent), 2f, 20);
        if(unit.within(targetPos, unit.hitSize)){
            unit.lookAt(parent.rotation);
        }else unit.lookAt(targetPos);
    }

    // TODO you can make the item transform into another, fix needed?
    // TODO multiplayer
    public void updateMining(){
        if(parent.mineTile != null){
            mineTile = parent.mineTile == mineTile ? null : parent.mineTile;
            parent.mineTile = null;
        }
        if(mineTile != null && !parent.within(mineTile.worldx(), mineTile.worldy(), parent.type.mineRange)) mineTile = null;
        if(mineTile != null && unit.within(mineTile.worldx(), mineTile.worldy(), unit.type.mineRange)) unit.mineTile = mineTile;
        if(mineTile != null && unit.mineTile != mineTile) mineTile = null;
        if(
            mineTile == null ||
            parent.stack.amount >= parent.type.itemCapacity ||
            parent.activelyBuilding()
        ){
            mineTile = unit.mineTile = null;
        }
        if(unit.stack.amount > 0){
            for(int i = 0; i < unit.stack.amount; i++){
                if(parent.stack.amount >= parent.type.itemCapacity) break;

                CoreBlock.CoreBuild core = unit.core();

                if(core != null && parent.within(core, mineTransferRange) && core.acceptStack(unit.stack.item, 1, unit) == 1 && parent.offloadImmediately()){
                    Call.transferItemTo(unit, unit.stack.item, 1, unit.x, unit.y, core);
                }else{
                    Call.transferItemToUnit(unit.stack.item, unit.x, unit.y, parent);
                }

            }
            unit.stack.amount = 0;
        }
    }

    @Override
    public void updateMovement(){
        if(parent.activelyBuilding() && unit.buildPlan() != null){
            moveTo(targetPos.set(unit.buildPlan().drawx(), unit.buildPlan().drawy()), unit.type.buildRange - unit.hitSize, 50);
        }else if (mineTile != null){
            moveTo(targetPos.set(mineTile.worldx(), mineTile.worldy()), unit.type.mineRange - unit.hitSize, 50);
        }else{
            updateIdle();
        }
    }

    @Override
    public void updateUnit(){
        if(unit instanceof DroneTetherc drone) parent = drone.parent();
        super.updateUnit();
    }

    @Override
    public void updateTargeting(){
        updateMining();
        updateBuilding();
    }
}
