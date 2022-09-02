package ol.type.bullets;

import arc.math.Angles;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.entities.Units;
import mindustry.entities.bullet.BasicBulletType;
import mindustry.gen.Bullet;
import mindustry.gen.Posc;
import mindustry.gen.Teamc;
import mindustry.gen.Unitc;
import mindustry.logic.Ranged;
import mindustry.world.blocks.defense.turrets.Turret.TurretBuild;
import ol.type.bullets.BulletControl.Targeting;

public class OlBulletType extends BasicBulletType {

    public OlBulletType(float speed, float damage){
        super(speed, damage);
    }

    @Override
    public void update(Bullet bullet) {
        updateTrail(bullet);

        if(homingPower > 0.0001f && bullet.time >= homingDelay){
            Teamc target;
            //home in on allies if possible
            if(healPercent > 0){
                target = Units.closestTarget(null, bullet.x, bullet.y, homingRange,
                        e -> e.checkTarget(collidesAir, collidesGround) && e.team != bullet.team && !bullet.hasCollided(e.id),
                        t -> collidesGround && (t.team != bullet.team || t.damaged()) && !bullet.hasCollided(t.id)
                );
            }else{
                target = Units.closestTarget(bullet.team, bullet.x, bullet.y, homingRange, e -> e.checkTarget(collidesAir, collidesGround) && !bullet.hasCollided(e.id), t -> collidesGround && !bullet.hasCollided(t.id));
            }

            if(target != null){
                bullet.vel.setAngle(Angles.moveToward(bullet.rotation(), bullet.angleTo(target), homingPower * Time.delta * 50f));
            }
        }

        if(weaveMag > 0){
            bullet.vel.rotate(Mathf.sin(bullet.time + Mathf.PI * weaveScale/2f, weaveScale, weaveMag * (Mathf.randomSeed(bullet.id, 0, 1) == 1 ? -1 : 1)) * Time.delta);
        }

        if(trailChance > 1){
            if(Mathf.chanceDelta(trailChance)){
                trailEffect.at(bullet.x, bullet.y, trailRotation ? bullet.rotation() : trailParam, trailColor);
            }
        }

        if(trailInterval > 1f){
            if(bullet.timer(0, trailInterval)){
                trailEffect.at(bullet.x, bullet.y, trailRotation ? bullet.rotation() : trailParam, trailColor);
            }
        }

        if(!(bullet.owner instanceof Ranged)) return;
        Tmp.v1.set(bullet.x, bullet.y);

        if(bullet.owner instanceof Targeting){
            Tmp.v1.set(((Targeting) bullet.owner).targetPos());
        }
        else if(bullet.owner instanceof TurretBuild) {
            Tmp.v1.set(((TurretBuild) bullet.owner).targetPos.x, ((TurretBuild) bullet.owner).targetPos.y);
        }
        else if (bullet.owner instanceof Unitc){
            Tmp.v1.set(((Unitc) bullet.owner).aimX(), ((Unitc) bullet.owner).aimY());
        }
        Tmp.v3.set(((Posc) bullet.owner()).x(), ((Posc) bullet.owner()).y());
        Tmp.v1.sub(Tmp.v3).clamp(0, ((Ranged) bullet.owner).range()).add(Tmp.v3);
        bullet.vel.add(Tmp.v2.trns(bullet.angleTo(Tmp.v1), bullet.type.homingPower * Time.delta)).clamp(0, bullet.type.speed);
        if(bullet.dst(Tmp.v3.x, Tmp.v3.y) >= ((Ranged) bullet.owner).range() + bullet.type.speed + 3) bullet.time += bullet.lifetime/100 * Time.delta;
    }
}
