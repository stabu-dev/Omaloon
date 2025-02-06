package omaloon.entities.bullet;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;

/* IS JUST POR HAILSTONE WEATHER */
public class HailStoneBulletType extends FallingBulletType{
    public TextureRegion[] variantsRegion;
    public int variants;

    public HailStoneBulletType(String sprite, int variants){
        super(sprite);
        this.variants = variants;
        this.lightRadius = 0;
        this.layer = Layer.flyingUnit;
    }

    @Override
    public void init(Bullet b){
        super.init(b);
        b.data = new HailStoneData(getRegion(b), fallTime);
    }

    @Override
    public void load(){
        variantsRegion = new TextureRegion[variants];

        for(int i = 0; i < variants; i++){
            variantsRegion[i] = Core.atlas.find(sprite + "-" + i);
        }
    }

    @Override
    public void draw(Bullet b){
        Draw.alpha(255);
        drawTrail(b);

        if(b.data instanceof HailStoneData data){
            drawFalling(b, data.region, regionColor);
        }
    }

    @Override
    public void despawned(Bullet b){
        if(despawnHit){
            hit(b);
        }else{
            createUnits(b, b.x, b.y);
        }

        if(!fragOnHit){
            createFrags(b, b.x, b.y);
        }

        if(!b.absorbed) despawnEffect.at(b.x, b.y, b.rotation(), hitColor, b.data);
        if(!b.absorbed) despawnSound.at(b);

        Effect.shake(despawnShake, despawnShake, b);
    }

    public TextureRegion getRegion(Bullet b){
        return variantsRegion[Mathf.floor(Mathf.randomSeed(b.id) * (variants - 1))];
    }

    @Override
    public void hit(Bullet b, float x, float y){
        if(!b.absorbed) hitEffect.at(x, y, b.rotation(), hitColor);
        if(!b.absorbed) hitSound.at(x, y, hitSoundPitch, hitSoundVolume);

        Effect.shake(hitShake, hitShake, b);

        if(fragOnHit){
            createFrags(b, x, y);
        }
        createPuddles(b, x, y);
        createIncend(b, x, y);
        createUnits(b, x, y);

        if(suppressionRange > 0){
            //bullets are pooled, require separate Vec2 instance
            Damage.applySuppression(b.team, b.x, b.y, suppressionRange, suppressionDuration, 0f, suppressionEffectChance, new Vec2(b.x, b.y));
        }

        createSplashDamage(b, x, y);

        for(int i = 0; i < lightning; i++){
            Lightning.create(b, lightningColor, lightningDamage < 0 ? damage : lightningDamage, b.x, b.y, b.rotation() + Mathf.range(lightningCone / 2) + lightningAngle, lightningLength + Mathf.random(lightningLengthRand));
        }
    }

    public static class HailStoneData{
        public TextureRegion region;
        public float fallTime;

        public HailStoneData(TextureRegion region, float fallTime){
            this.region = region;
            this.fallTime = fallTime;
        }
    }
}
