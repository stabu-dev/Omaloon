package omaloon.ai;

import arc.math.geom.*;
import arc.util.*;
import mindustry.ai.types.*;
import mindustry.gen.*;
import omaloon.type.weapons.*;

/** Command AI for {@link RamWeapon} units. */
public class RamCommandAI extends CommandAI{
    protected final RamRun run = new RamRun();
    protected boolean moveOn;

    @Override
    public void updateTargeting(){
        moveOn |= run.finished(unit);
        super.updateTargeting();
    }

    @Override
    public boolean retarget(){
        boolean timer = super.retarget();
        if(attackTarget != null || target == null) return timer;
        if(moveOn){
            moveOn = false;
            return true;
        }
        return timer && !run.charging && !run.inLine(unit, target);
    }

    @Override
    public Teamc findTarget(float x, float y, float range, boolean air, boolean ground){
        return nearAttackTarget(x, y, range) ? super.findTarget(x, y, range, air, ground) : run.find(unit, range, air, ground);
    }

    @Override
    public void moveTo(Position target, float circleLength, float smooth, boolean keepDistance, @Nullable Vec2 offset, boolean arrive){
        float speed = unit.vel.len();
        if(run.backingOff(unit)) return;
        super.moveTo(target, circleLength, smooth, keepDistance, offset, arrive && unit.within(target, speed * speed / (2f * unit.type.accel * unit.speed()) + unit.hitSize));
        if(!unit.within(target, unit.hitSize)) run.checkStuck(unit, target);
    }

    @Override
    public void circleAttack(float circleLength){
        if(target != null) run.update(unit, target);
    }
}