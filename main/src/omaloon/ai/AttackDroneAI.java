package omaloon.ai;

import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import omaloon.entities.abilities.*;
import omaloon.gen.*;

public class AttackDroneAI extends AIController{
    protected Unit parent;
    protected Vec2 targetPos = new Vec2();
    protected boolean shouldShoot = false;

    public void updateIdle(){
        DroneAbility ability = (DroneAbility) parent.abilities[((DroneTetherc) unit).abilityIndex()];

        moveTo(targetPos.trns(parent.rotation - 90f, ability.idleX, ability.idleY).add(parent), 2f, 20);
        if(unit.within(targetPos, unit.hitSize)){
            unit.lookAt(parent.rotation);
        }else unit.lookAt(targetPos);
    }

    @Override
    public void updateMovement(){
        if(shouldShoot){
            moveTo(targetPos, targetPos.dst(parent.aimX, parent.aimY) > 0 ? 2f :  unit.range() / 2f, 50f);
            if(targetPos.dst(unit) < unit.range()){
                unit.lookAt(targetPos);
            }else{
                unit.lookAt(this.unit.prefRotation());
            }
        }else{
            updateIdle();
        }
    }

    @Override
    public void updateTargeting(){
        shouldShoot = parent.isShooting;
        targetPos.set(Tmp.v1.set(parent.aimX, parent.aimY)).sub(parent).limit(parent.range()).add(parent);

        updateWeapons();
    }

    @Override
    public void updateUnit(){
        if(unit instanceof DroneTetherc drone) parent = drone.parent();
        super.updateUnit();
    }

    @Override
    public void updateVisuals(){
        if(unit.isFlying() && unit.type.wobble){
            unit.wobble();
        }
    }

    @Override
    public void updateWeapons(){
        for(WeaponMount mount : unit.mounts){
            if(!mount.weapon.controllable || !mount.weapon.aiControllable || mount.weapon.noAttack) continue;

            mount.aimX = targetPos.x;
            mount.aimY = targetPos.y;
            mount.shoot = shouldShoot && Mathf.within(unit.x, unit.y, targetPos.x, targetPos.y, mount.weapon.range() + unit.hitSize);
        }
    }
}
