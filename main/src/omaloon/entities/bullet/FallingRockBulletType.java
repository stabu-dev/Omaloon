package omaloon.entities.bullet;

import arc.*;
import arc.audio.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import omaloon.math.*;

public class FallingRockBulletType extends BulletType{
    public String name;

    public float fallDistance = 0;
    public float fallHeight = 0;

    public Sound spawnSound = Sounds.none;
    public float spawnSoundVolume = 1f;

    public int variants = 0;

    public TextureRegion[] variantRegions;

    public FallingRockBulletType(String name){
        this.name = name;
        damage = speed = 0;
        layer = Layer.flyingUnit;
        lightOpacity = 1f;
        lightRadius = 0f;
        collides = hittable = reflectable = keepVelocity = false;
    }

    @Override
    public void despawned(Bullet b){
        if(despawnHit){
            if(!b.absorbed) hit(b);
        }else{
            createUnits(b, b.x, b.y);
        }

        if(!fragOnHit){
            createFrags(b, b.x, b.y);
        }

        if(!b.absorbed){
            despawnEffect.at(b.x, b.y, b.rotation(), hitColor, new RockData(){{
                region = variantRegions[variant(b)];
                bullet = (FallingRockBulletType)b.type;
            }});
            despawnSound.at(b);
        }

        Effect.shake(despawnShake, despawnShake, b);
    }

    @Override
    public void hit(Bullet b, float x, float y){
        if(!b.absorbed){
            hitEffect.at(x, y, b.rotation(), hitColor);
            hitSound.at(x, y, hitSoundPitch, hitSoundVolume);
            createSplashDamage(b, x, y);
        }

        Effect.shake(hitShake, hitShake, b);

        if(fragOnHit) createFrags(b, x, y);
        createPuddles(b, x, y);
        createIncend(b, x, y);
        createUnits(b, x, y);
    }

    @Override
    public void draw(Bullet b){
        super.draw(b);

        float elevation = fallHeight * b.fout();
        float ox = b.x + Physics.xOffset(b.x, elevation);
        float oy = b.y + Physics.yOffset(b.y, elevation) + fallDistance * b.fout();
        float scl = 1f + fallHeight * b.fout() / 40f;
        int v = variant(b);

        Draw.z(Layer.darkness);
        Draw.mixcol(Pal.shadow, 1f);
        Draw.alpha(b.fin() * Pal.shadow.a);
        Draw.rect(variantRegions[v], b.x, b.y);
        Draw.mixcol();

        Draw.z(Layer.flyingUnit + Physics.layerOffset(ox, oy));
        Draw.alpha(Mathf.clamp(b.fin()));
        Draw.scl(scl);
        Draw.rect(variantRegions[v], ox, oy);
        Draw.scl();
        Draw.reset();
    }

    @Override
    public void init(Bullet b){
        super.init(b);
        spawnSound.at(b.x, b.y, 1f, spawnSoundVolume);
    }

    @Override
    public void load(){
        super.load();

        if(variants > 0){
            variantRegions = new TextureRegion[variants];
            for(int i = 0; i < variants; i++){
                variantRegions[i] = Core.atlas.find(name + "-" + i);
            }
        }else{
            variantRegions = new TextureRegion[]{Core.atlas.find(name)};
        }
    }

    public int variant(Bullet b){
        return Mathf.randomSeed(b.id, 0, Math.max(0, variantRegions.length - 1));
    }

    public static class RockData{
        public TextureRegion region;
        public FallingRockBulletType bullet;
    }
}
