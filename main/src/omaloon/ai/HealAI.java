package omaloon.ai;

import arc.math.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.blocks.ConstructBlock.*;
import omaloon.gen.*;

public class HealAI extends AIController{
    protected Teamc healTarget;

    @Override
    public void updateTargeting(){
        if(timer.get(timerTarget2, 15)){
            healTarget = findHealTarget();
            if(healTarget != null && !aimable(healTarget)){
                healTarget = null;
            }
        }

        if(healTarget == null){
            if(target != null && target.team() == unit.team){
                target = null;
            }
            super.updateTargeting();
        }else{
            this.target = healTarget;
        }
    }

    @Override
    public void updateMovement(){
        if(target != null && target.team() == unit.team){
            unit.aim(target);
            unit.controlWeapons(target.within(unit, unit.range()));
        }else{
            super.updateMovement();
        }
    }

    protected Teamc findHealTarget(){
        Unit ally = Units.closest(unit.team, unit.x, unit.y, unit.range(), u -> u != unit && !sameChain(u) && u.damaged() && u.checkTarget(unit.type.targetAir, unit.type.targetGround));
        if(ally != null) return ally;

        if(unit.type.targetGround){
            return Units.findAllyTile(unit.team, unit.x, unit.y, unit.range(), b -> b.damaged() && !(b instanceof ConstructBuild));
        }
        return null;
    }

    protected boolean aimable(Teamc target){
        float angle = unit.angleTo(target);
        for(var mount : unit.mounts){
            Weapon weapon = mount.weapon;
            if(weapon.rotate){
                if(Angles.angleDist(angle - unit.rotation, weapon.baseRotation) <= weapon.rotationLimit / 2f){
                    return true;
                }
            }else if(Angles.within(unit.rotation + weapon.baseRotation, angle, weapon.shootCone)){
                return true;
            }
        }
        return false;
    }

    protected boolean sameChain(Unit u){
        return unit instanceof Chainedc c && u instanceof Chainedc other && c.head() != null && c.head() == other.head();
    }
}