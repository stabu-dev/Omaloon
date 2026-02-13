package omaloon.entities.bullet;

import arc.audio.*;
import mindustry.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;

public class LingeringBulletType extends BulletType{
    // do not set to 0
    public float splashDamageInterval = 5f;

    public Sound activeSound = Sounds.none;
    public float activeSoundVolume = 1;

    public LingeringBulletType(float damage, float radius){
        speed = 0;
        this.damage = damage;
        collidesTiles = false;
        pierce = true;
        splashDamage = damage;
        splashDamageRadius = radius;
    }

    @Override
    public float continuousDamage(){
        return splashDamage * 60f / splashDamageInterval;
    }

    @Override
    public void init(Bullet b){
        super.init(b);

        createSplashDamage(b, b.x, b.y);
    }

    @Override
    public float estimateDPS(){
        return splashDamage * 60f / splashDamageInterval;
    }

    @Override
    public void update(Bullet b){
        updateTrail(b);
        updateHoming(b);
        updateWeaving(b);
        updateTrailEffects(b);
        updateBulletInterval(b);

        if(b.timer(1, splashDamageInterval)){
            createSplashDamage(b, b.x, b.y);
            if(healPercent > 0 || healAmount > 0){
                Units.nearby(b.team, b.x, b.y, splashDamageRadius, u -> {
                    u.heal(healPercent / 100f * u.maxHealth() + healAmount);
                });
            }
        }

        if(activeSound != Sounds.none) Vars.control.sound.loop(activeSound, b, b.fslope() * activeSoundVolume);
    }
}
