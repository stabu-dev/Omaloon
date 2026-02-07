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
        layer = Layer.effect + 1f;
        collides = false;
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

        despawnEffect.at(b.x, b.y, b.rotation(), hitColor, new RockData(){{
            region = variantRegions[variant(b)];
            bullet = (FallingRockBulletType)b.type;
        }});
        despawnSound.at(b);

        Effect.shake(despawnShake, despawnShake, b);
    }

    @Override
    public void draw(Bullet b){
        super.draw(b);

        Physics.parallax(Tmp.v1.set(b.x, b.y), fallHeight * b.fout());
        float ox = Tmp.v1.x;
        float oy = Tmp.v1.y + fallDistance * b.fout();

        Draw.mixcol(Pal.shadow, 1f);
        Draw.alpha(b.fin() * Pal.shadow.a);
        Draw.rect(variantRegions[variant(b)], b.x, b.y);
        Draw.mixcol();

        Draw.alpha(Mathf.clamp(b.fin() * 5f));
        Draw.rect(variantRegions[variant(b)], ox, oy);
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
