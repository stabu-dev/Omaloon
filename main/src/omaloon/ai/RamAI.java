package omaloon.ai;

import mindustry.ai.types.*;
import mindustry.entities.*;
import mindustry.gen.*;
import omaloon.type.weapons.*;

/** Ground AI for {@link RamWeapon} units: makes {@link RamRun ram runs} at a target, moving on to another after each one. */
public class RamAI extends GroundAI{
    protected final RamRun run = new RamRun();

    @Override
    public void updateTargeting(){
        if(Units.invalidateTarget(target, unit, unit.range() * 1.5f)) target = null;
        if(run.finished(unit) || (target == null ? retarget() : !run.charging && retarget() && !run.inLine(unit, target))){
            target = findMainTarget(unit.x, unit.y, unit.range(), unit.type.targetAir, unit.type.targetGround);
        }
    }

    @Override
    public void updateMovement(){
        if(target == null){
            if(run.backingOff(unit)) return;
            super.updateMovement();
            Building core = unit.closestEnemyCore();
            if(core != null && !unit.within(core, unit.type.range * 0.5f)) run.checkStuck(unit, core);
        }else{
            run.update(unit, target);
        }
    }

    @Override
    public Teamc findTarget(float x, float y, float range, boolean air, boolean ground){
        return run.find(unit, range, air, ground);
    }
}