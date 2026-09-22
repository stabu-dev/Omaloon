package omaloon.ai;

import arc.math.*;
import arc.util.*;
import mindustry.entities.units.*;
import mindustry.gen.*;

/** Combat drone: mirrors the parent's aim and engages its target, otherwise idles in formation. */
public class AttackDroneAI extends DroneAI{
    protected boolean shouldShoot = false;

    @Override
    public void updateMovement(){
        if(shouldShoot){
            moveTo(targetPos, targetPos.dst(parent.aimX, parent.aimY) > 0 ? 2f : unit.range() / 2f, 50f);
            if(targetPos.dst(unit) < unit.range()){
                unit.lookAt(parent.aimX, parent.aimY);
            }else{
                unit.lookAt(unit.prefRotation());
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
    public void updateVisuals(){
        if(unit.isFlying() && unit.type.wobble){
            unit.wobble();
        }
    }

    @Override
    public void updateWeapons(){
        for(WeaponMount mount : unit.mounts){
            if(!mount.weapon.controllable || !mount.weapon.aiControllable || mount.weapon.noAttack) continue;

            mount.aimX = parent.aimX;
            mount.aimY = parent.aimY;
            mount.shoot = shouldShoot && Mathf.within(unit.x, unit.y, targetPos.x, targetPos.y, mount.weapon.range() + unit.hitSize);
        }
    }
}