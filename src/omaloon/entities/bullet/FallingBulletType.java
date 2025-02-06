package omaloon.entities.bullet;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import omaloon.math.*;

import static mindustry.Vars.headless;

public class FallingBulletType extends BulletType{
    public float fallTime = 50f;
    public float fallSpread = 60;
    public String sprite;
    public TextureRegion region;
    public Color regionColor = Color.white;
    public boolean canCollideFalling = false;
    public float fallingRadius = 20f;
    public float fallingDamage = 100f;
    public Effect hitFallingEffect = Fx.none;
    public Color hitFallingColor = Color.white;
    public boolean fallingHitCollideGround = false;
    public boolean immovable = true;
    public float minDistanceFallingCollide = 10f;


    public FallingBulletType(String sprite){
        super(1f, 0f);

        this.sprite = sprite;

        collides = hittable = reflectable = keepVelocity = backMove = false;
        despawnHit = true;
    }

    @Override
    public void load(){
        super.load();

        region = Core.atlas.find(sprite);
    }

    @Override
    public void init(Bullet b){
        super.init(b);

        if(immovable){
            Tmp.v2.trns(b.rotation(), b.lifetime() * speed);
            b.set(b.x + Tmp.v2.x, b.y + Tmp.v2.y);

            b.vel.setZero();
        }
        b.lifetime(fallTime);
    }

    @Override
    public void draw(Bullet b){
        drawTrail(b);
        drawFalling(b, region, regionColor);
    }

    public void drawFalling(Bullet b, TextureRegion region, Color col){
        float rot = getRotTrajectory(b);
        float sclFall = 1f + getElevation(b) / 4;
        float sclShadow = 0.1f + b.fin();

        Vec2 pos = getTrajectory(b);

        Draw.z(Layer.darkness);
        Draw.scl(sclShadow);
        Drawf.shadow(region, b.x, b.y, rot);
        Draw.scl();

        Draw.z(Layer.flyingUnit + Math3D.layerOffset(pos.x, pos.y));
        Draw.color(col);
        Draw.alpha(Mathf.clamp(b.fin() * 1.5f));
        Draw.scl(sclFall);
        Draw.rect(region, pos.x, pos.y, rot);
        Draw.reset();
    }


    @Override
    public void drawLight(Bullet b){
        if(lightOpacity <= 0f || lightRadius <= 0f) return;
        Drawf.light(getTrajectory(b), (1 + b.fout()) * lightRadius, lightColor, lightOpacity);
    }

    @Override
    public void update(Bullet b){
        super.update(b);
        updateFalling(b);
    }

    public void updateFalling(Bullet b){
        if(canCollideFalling && isLanding(b)){
            Teamc target = Units.closestTarget(b.team, b.x, b.y, fallingRadius,
                e -> e.checkTarget(true, false) && e.team != b.team && !b.hasCollided(e.id) //ONLY AIR UNITS
            );

            Vec2 pos = getTrajectory(b);


            if(target != null && pos.dst(target.x(), target.y()) < minDistanceFallingCollide){
                hitFalling(b);

                if(pierce){
                    b.collided.add(target.id());
                }else{
                    b.remove();
                }
            }
        }
    }

    @Override
    public void updateTrail(Bullet b){
        if(!headless && trailLength > 0){
            if(b.trail == null){
                b.trail = new Trail(trailLength);
            }
            Vec2 pos = getTrajectory(b);
            b.trail.length = trailLength;
            b.trail.update(pos.x, pos.y, trailInterp.apply(b.fin()) * (1f + (trailSinMag > 0 ? Mathf.absin(Time.time, trailSinScl, trailSinMag) : 0f)));
        }
    }

    @Override
    public void updateTrailEffects(Bullet b){
        if(trailChance > 0){
            if(Mathf.chanceDelta(trailChance)){
                Vec2 pos = getTrajectory(b);
                trailEffect.at(pos.x, pos.y, trailRotation ? b.rotation() : trailParam, trailColor);
            }
        }

        if(trailInterval > 0f){
            if(b.timer(0, trailInterval)){
                Vec2 pos = getTrajectory(b);
                trailEffect.at(pos.x, pos.y, trailRotation ? b.rotation() : trailParam, trailColor);
            }
        }

    }

    public void hitFalling(Bullet b){
        hitFalling(b, b.x, b.y);
    }

    public void hitFalling(Bullet b, float x, float y){
        hitFallingEffect.at(x, y, b.rotation(), hitFallingColor);
        hitSound.at(x, y, hitSoundPitch, hitSoundVolume);

        Effect.shake(hitShake, hitShake, b);

        if(b.absorbed) return;
        Damage.damage(b.team, x, y, fallingRadius, fallingDamage * b.damageMultiplier(), splashDamagePierce, true, fallingHitCollideGround, scaledSplashDamage, b);

        if(status != StatusEffects.none){
            Damage.status(b.team, x, y, fallingRadius, status, statusDuration, true, fallingHitCollideGround);
        }
    }

    public Vec2 getTrajectory(Bullet b){
        float elevation = getElevation(b);
        Vec2 off = getOffsetTrajectory(b);
        Vec2 pos = new Vec2(b.x, b.y);
        pos.add(off.x * b.fout(), off.y * b.fout() * 2);
        pos.add(Math3D.xOffset(pos.x, elevation), Math3D.yOffset(pos.y, elevation));

        return pos;
    }

    public Vec2 getOffsetTrajectory(Bullet b){
        return Tmp.v2.trns(90 + (Mathf.randomSeed(b.id + 1) - 0.5f) * fallSpread / 2, fallTime);
    }

    public float getRotTrajectory(Bullet b){
        return Mathf.randomSeed(b.id) * 360f;
    }

    public float getElevation(Bullet b){
        return b.fout() * fallTime / 10;
    }

    public boolean isLanding(Bullet b){
        return b.fin() > 0.75;
    }
}
