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

public class MillipedeAI extends GroundAI {
	@Override
	public void updateWeapons() {
		if (!(unit instanceof Millipedec millipedec)) return;
		if (!millipedec.isHead()) return;

		float rotation = unit.rotation - 90;
		boolean ret = retarget();

		if(ret) {
			target = findMainTarget(unit.x, unit.y, unit.range(), unit.type.targetAir, unit.type.targetGround);
		}

		noTargetTime += Time.delta;

		if(invalid(target)) {
			target = null;
		}else noTargetTime = 0f;

		unit.isShooting = false;

		cast().distributeActionBack(u -> {
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
					shoot = mount.target.within(mountX, mountY, wrange + (mount.target instanceof Sized s ? s.hitSize()/2f : 0f)) && shouldShoot();

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

	public <T extends Unit & Millipedec> T cast() {
		return (T) unit;
	}
}
