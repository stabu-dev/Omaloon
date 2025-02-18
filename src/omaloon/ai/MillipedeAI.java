package omaloon.ai;

import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.ai.types.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;
import omaloon.gen.*;

public class MillipedeAI extends GroundAI{
    protected Vec2 commandPosition = new Vec2();
    protected Teamc commandTarget;

    @Override
    public void updateWeapons(){
        if(!(unit instanceof Chainedc millipedec)) return;
        if(!millipedec.isHead()) return;

        float rotation = unit.rotation - 90;
        boolean ret = retarget();

        if(ret){
            target = findMainTarget(unit.x, unit.y, unit.range(), unit.type.targetAir, unit.type.targetGround);
        }

        noTargetTime += Time.delta;

        if(invalid(target)){
            target = null;
        }else noTargetTime = 0f;

        unit.isShooting = false;

        cast().consBackwards(u -> {
            for(WeaponMount mount : u.mounts){
                Weapon weapon = mount.weapon;
                float wrange = weapon.range();

                if(!weapon.controllable || weapon.noAttack) continue;
                if(!weapon.aiControllable){
                    mount.rotate = false;
                    continue;
                }

                float
                mountX = u.x + Angles.trnsx(rotation, weapon.x, weapon.y),
                mountY = u.y + Angles.trnsy(rotation, weapon.x, weapon.y);

                if(unit.type.singleTarget){
                    mount.target = target;
                }else{
                    if(ret) mount.target = findTarget(mountX, mountY, wrange, weapon.bullet.collidesAir, weapon.bullet.collidesGround);
                    if(checkTarget(mount.target, mountX, mountY, wrange)) mount.target = null;
                }

                boolean shoot = false;

                if(mount.target != null){
                    shoot = mount.target.within(mountX, mountY, wrange + (mount.target instanceof Sized s ? s.hitSize() / 2f : 0f)) && shouldShoot();

                    Vec2 to = Predict.intercept(unit, mount.target, weapon.bullet.speed);
                    mount.aimX = to.x;
                    mount.aimY = to.y;
                }

                unit.isShooting |= (mount.shoot = mount.rotate = shoot);

                if(mount.target == null && !shoot && !Angles.within(mount.rotation, mount.weapon.baseRotation, 0.01f) && noTargetTime >= rotateBackTimer){
                    mount.rotate = true;
                    Tmp.v1.trns(u.rotation + mount.weapon.baseRotation, 5f);
                    mount.aimX = mountX + Tmp.v1.x;
                    mount.aimY = mountY + Tmp.v1.y;
                }

                if(shoot){
                    unit.aimX = mount.aimX;
                    unit.aimY = mount.aimY;
                }
            }
        });
    }

    @Override
    public void updateMovement(){
        if(commandTarget != null && !unit.within(commandTarget, unit.type.range * 0.8f)){
            moveTo(commandTarget, unit.type.range * 0.8f);
        }else if(!commandPosition.isZero()){
            moveTo(commandPosition, 0);
        }else{
            super.updateMovement();
        }

        faceTarget();
    }

    @Override
    public Teamc findTarget(float x, float y, float range, boolean air, boolean ground){
        Teamc target = commandTarget != null && commandTarget.within(x, y, range) &&
        commandTarget.team() == unit.team && commandTarget.isNull() ? commandTarget : null;

        return target != null ? target : super.findTarget(x, y, range, air, ground);
    }

    @Override
    public void commandPosition(Vec2 pos){
        this.commandPosition.set(pos);
        this.commandTarget = null;
    }

    @Override
    public void commandTarget(Teamc moveTo){
        this.commandTarget = moveTo;
        this.commandPosition.setZero();
    }

    public <T extends Unit&Chainedc> T cast(){
        return (T)unit;
    }
}