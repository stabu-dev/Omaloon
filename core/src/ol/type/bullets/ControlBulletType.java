package ol.type.bullets;

import arc.math.*;
import arc.math.geom.*;
import arc.util.*;

import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.logic.*;
import mindustry.world.blocks.defense.turrets.Turret.*;

public class ControlBulletType extends BasicBulletType {
    public interface Targeting {
        Vec2 targetPos();
    }

    public ControlBulletType(float speed, float damage) {
        super(speed, damage);
    }

    @Override
    public void update(Bullet bullet) {
        updateTrail(bullet);

        if(homingPower > 0.0001f && bullet.time >= homingDelay) {
            Teamc target = Units.closestTarget(
                    bullet.team,
                    bullet.x,
                    bullet.y,
                    homingRange,
                    e -> e.checkTarget(collidesAir, collidesGround) && !bullet.hasCollided(e.id),
                    t -> collidesGround && (t.team != bullet.team || t.damaged()) && !bullet.hasCollided(t.id)
            );

            if(target != null) {
                bullet.vel.setAngle(Angles.moveToward(
                        bullet.rotation(),
                        bullet.angleTo(target),
                        homingPower * Time.delta * 50f
                ));
            }
        }

        if(!(bullet.owner instanceof Ranged)) return;

        Vec2 targetPos = ((bullet.owner instanceof Targeting) ?
                ((Targeting) bullet.owner).targetPos() :
                ((bullet.owner instanceof TurretBuild) ?
                        new Vec2(((TurretBuild) bullet.owner).targetPos.x, ((TurretBuild) bullet.owner).targetPos.y) :
                        new Vec2(((Unitc) bullet.owner).aimX(), ((Unitc) bullet.owner).aimY())
                )
        );

        Vec2 ownerPos = Tmp.v3.set(((Posc) bullet.owner()).x(), ((Posc) bullet.owner()).y());

        Tmp.v1.set(targetPos).sub(ownerPos).clamp(0, ((Ranged) bullet.owner).range()).add(ownerPos);

        bullet.vel.add(
                Tmp.v2.trns(
                        bullet.angleTo(Tmp.v1),
                        bullet.type.homingPower * Time.delta
                )
        ).clamp(0, bullet.type.speed);

        if(bullet.dst(ownerPos.x, ownerPos.y) >= ((Ranged) bullet.owner).range() + bullet.type.speed + 3) {
            bullet.time += bullet.lifetime/100 * Time.delta;
        }
    }
}
