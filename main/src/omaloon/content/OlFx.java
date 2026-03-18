package omaloon.content;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import omaloon.entities.bullet.FallingRockBulletType.*;
import omaloon.math.*;
import omaloon.graphics.*;
import omaloon.world.blocks.environment.customsshapeproop.*;

import static arc.graphics.g2d.Draw.*;
import static arc.math.Angles.*;

public class OlFx{
    public static final Rand rand = new Rand();
    public static final Vec2 vec = new Vec2();

    public static final Effect

    breakShapedProp = new Effect(23, e -> {
        if(!(e.data instanceof MultiPropGroup group)) return;

        float scl = Math.max(e.rotation, 1);
        color(Tmp.c1.set(e.color).mul(1.1f));

        for(Tile tile : group.group){
            randLenVectors(e.id + tile.pos(), 2, 19f * e.finpow() * scl, (x, y) -> {
                float wx = tile.worldx() + x;
                float wy = tile.worldy() + y;
                Fill.circle(wx, wy, e.fout() * 3.5f * scl + 0.3f);
            });
        }
    }).layer(Layer.debris),

    collectorWaves = new Effect(300, e -> {
        Draw.color(e.color);

        for(int i = 0; i < 5; i++) {
            float p = ((Time.time + 300/5f * i) / 300) % 1;
            Draw.alpha(0.3f * (1f - p) * Interp.smooth.apply(e.fslope()));
            Fill.circle(e.x, e.y, e.rotation * p);
        }

        Draw.alpha(Interp.exp5.apply(e.fslope()));
        rand.setSeed(e.id + 8);
        for(int i = 0; i < 6; i++) {
            int finalI = i;
            Angles.randLenVectors(e.id + i, 6, e.rotation, e.fin(Interp.smooth) * 360 * (i + 1) * 2 / 6, 360, (x, y) -> {
                Physics.parallax(vec.set(e.x + x, e.y + y), (finalI + 1) / 6f * e.finpowdown());
                float size = rand.random(0.5f, 3f);

                Fill.rect(vec.x, vec.y, size, size * 3);
                Fill.rect(vec.x, vec.y, size, size * 3, 90);
            });
        }
    }),

    compositeCraft = new Effect(60f, e -> {
        rand.setSeed(e.id);
        Draw.color(Color.valueOf("7545D5").mul(1.5f));
        randLenVectors(e.id, 10, 8 * e.finpow(), (x, y) -> {
            vec.trns(Mathf.angle(x, y), 8f).add(x + e.x, y + e.y);
            float rad = (3 + rand.range(2));
            Drawf.light(vec.x, vec.y, (rad + 8f) * e.fout(), Color.valueOf("7545D5"), 0.3f);
            Fill.circle(vec.x, vec.y, rad * e.fout());
        });

        if(e.time <= 5) Effect.shake(0.5f, 5f, e.x, e.y);
    }),

    drillHammerHit = new Effect(80f, e -> {
        color(e.color, Color.gray, e.fin());
        alpha(0.6f);
        Draw.z(Layer.block);

        rand.setSeed(e.id);
        for(int i = 0; i < 3; i++){
            float len = rand.random(6f), rot = rand.range(40f) + e.rotation;

            e.scaled(e.lifetime * rand.random(0.3f, 1f), e2 -> {
                vec.trns(rot, len * e2.finpow());

                Fill.square(e2.x + vec.x, e2.y + vec.y, 1.5f * e2.fslope() + 0.2f, 45);
            });
        }
    }),

    dynamicHailWave = new Effect(22, e -> {
        Tile tile = Vars.world.tileWorld(e.x, e.y);
        Color color = e.color;
        if(tile != null && tile.floor().isLiquid){
            color = tile.floor().mapColor;
        }
        
        Draw.color(color, 0.7f);
        Lines.stroke(e.fout() * 2f);
        Lines.circle(e.x, e.y, 4f + e.finpow() * e.rotation);
    }),

    fellStone = new Effect(120f, e -> {
        if(!(e.data instanceof RockData data)) return;

        rand.setSeed(e.id);
        vec.trns(rand.random(360f), data.bullet.lifetime / 2f + rand.random(data.bullet.lifetime));

        Tile startTile = Vars.world.tileWorld(e.x, e.y);
        boolean startLiquid = startTile != null && startTile.floor().isLiquid;
        Tile endTile = Vars.world.tileWorld(e.x + vec.x, e.y + vec.y);
        float hitFactor = 1f;

        if(!startLiquid){
            for(int i = 1; i <= 10; i++){
                float f = i / 10f;
                Tile t = Vars.world.tileWorld(e.x + vec.x * f, e.y + vec.y * f);
                if(t != null && t.floor().isLiquid){
                    hitFactor = f;
                    endTile = t;
                    break;
                }
            }
        }else{
            hitFactor = 0f;
            endTile = startTile;
        }

        float curFin = e.finpow();
        boolean sunken = endTile != null && curFin >= hitFactor;
        float finalPos = Math.min(curFin, hitFactor);

        float x = e.x + (vec.x * finalPos);
        float y = e.y + (vec.y * finalPos);
        float rot = vec.angle();

        if(sunken){
            Draw.z(Layer.debris);
            Draw.color(e.color);

            float sinkTime = (hitFactor >= 0.999f) ? 0f : (curFin - hitFactor) / (1f - hitFactor);

            boolean deep = !endTile.floor().shallow;

            Draw.mixcol(endTile.floor().mapColor, 0.2f + 0.6f * sinkTime);
            Draw.alpha(e.fout());

            float sinkY = deep ? (-Interp.pow2In.apply(sinkTime) * 8f) : Math.max(-Interp.pow2In.apply(sinkTime) * 8f, -3f);

            Draw.rect(data.region, x, y + sinkY, rot);
            Draw.mixcol();
        }else{
            float scl = Interp.bounceIn.apply(e.fout() - 0.3f);

            Draw.z(Layer.power + 0.1f);
            Draw.mixcol(Pal.shadow, 1f);
            Draw.alpha(Math.min(e.fout(), Pal.shadow.a));
            Draw.rect(data.region, x, y, rot);
            Draw.mixcol();

            Draw.z(Layer.power + 0.2f);
            Draw.color(e.color);
            Draw.alpha(e.fout());
            Draw.rect(data.region, x, y + (scl * data.bullet.lifetime / 2f), rot);
        }
    }),

    glacied = new Effect(80f, e -> {
        color(OlStatusEffects.glacied.color);
        alpha(Mathf.clamp(e.fin() * 2f));

        Fill.circle(e.x, e.y, e.fout());
    }),

    hailStoneSplashSmall = new Effect(50f, e -> {
        Tile tile = Vars.world.tileWorld(e.x, e.y);
        if(tile == null || !tile.floor().isLiquid) return;

        boolean deep = !tile.floor().shallow;
        Color fluidCol = tile.floor().mapColor;
        Color sprayCol = Tmp.c1.set(fluidCol).mul(1.2f);

        float intensity = deep ? 1f : 1.4f;

        Draw.z(Layer.debris);

        Draw.color(fluidCol);
        Lines.stroke(e.fout() * 1.2f);
        Lines.circle(e.x, e.y, (2f + e.finpow() * 12f) * intensity);

        rand.setSeed(e.id);
        int crownPoints = deep ? 4 : 7;
        for(int i = 0; i < crownPoints; i++){
            float ang = rand.random(360f);
            float len = rand.random(2f, 6f) * intensity;
            Tmp.v1.trns(ang, (1f + e.finpow() * 3f) * intensity);
            Drawf.tri(e.x + Tmp.v1.x, e.y + Tmp.v1.y, 2f * e.fout() * intensity, len * e.fout(), ang);
        }

        Draw.color(sprayCol);
        int dropCount = deep ? 6 : 10;
        for(int i = 0; i < dropCount; i++){
            rand.setSeed(e.id + i);
            float angle = rand.random(360f);
            float dist = rand.random(10f, 30f) * intensity;
            float peakH = rand.random(8f, 16f) * (deep ? 1.2f : 0.7f);
            float lifeScl = rand.random(0.6f, 1f);

            float curFin = Math.min(e.fin() / lifeScl, 1f);
            if(curFin >= 1f) continue;

            float fout = 1f - curFin;
            Tmp.v1.trns(angle, dist * curFin);
            float z = Mathf.sin(curFin * Mathf.PI) * peakH;

            Fill.circle(e.x + Tmp.v1.x, e.y + Tmp.v1.y + z, (fout + 0.1f) * intensity);
        }
    }),

    hailStoneImpact = new Effect(80f, e -> {
        Tile tile = Vars.world.tileWorld(e.x, e.y);
        boolean liquid = tile != null && tile.floor().isLiquid;

        if(liquid){
            boolean deep = !tile.floor().shallow;
            Color fluidCol = tile.floor().mapColor;
            Color sprayCol = Tmp.c1.set(fluidCol).mul(1.2f);

            float intensity = deep ? 1f : 1.7f;

            Draw.z(Layer.debris);

            Draw.color(fluidCol);
            Lines.stroke(e.fout() * 1.5f);
            Lines.circle(e.x, e.y, (4f + e.finpow() * 20f) * (deep ? 1f : 1.4f));

            rand.setSeed(e.id);
            int crownPoints = deep ? 6 : 11;
            for(int i = 0; i < crownPoints; i++){
                float ang = rand.random(360f);
                float len = rand.random(4f, 12f) * intensity;
                Tmp.v1.trns(ang, (2f + e.finpow() * 6f) * intensity);
                Drawf.tri(e.x + Tmp.v1.x, e.y + Tmp.v1.y, 3f * e.fout() * intensity, len * e.fout(), ang);
            }

            Draw.color(sprayCol);
            int dropCount = deep ? 12 : 26;
            for(int i = 0; i < dropCount; i++){
                rand.setSeed(e.id + i + 1);
                float angle = rand.random(360f);
                float dist = rand.random(20f, 60f) * intensity;
                float peakH = rand.random(20f, 40f) * (deep ? 1.3f : 0.6f) * intensity;
                float lifeScl = rand.random(0.6f, 1f);

                float curFin = Math.min(e.fin() / lifeScl, 1f);
                if(curFin >= 1f) continue;

                float fout = 1f - curFin;
                Tmp.v1.trns(angle, dist * curFin);
                float z = Mathf.sin(curFin * Mathf.PI) * peakH;

                Fill.circle(e.x + Tmp.v1.x, e.y + Tmp.v1.y + z, (1.2f * fout + 0.2f) * intensity);
            }

        }else{
            Color waveColor = Color.lightGray;
            Color smokeColor = Color.gray;

            Draw.z(Layer.power);
            Draw.color(waveColor);
            Lines.stroke(e.fout() * 2.5f);
            Lines.circle(e.x, e.y, e.finpow() * 24f);

            Draw.z(Layer.effect);

            Draw.color(Color.white, e.color, e.fin());
            rand.setSeed(e.id);
            for(int i = 0; i < 8; i++){
                float ang = rand.random(360f);
                float len = rand.random(6f, 14f);
                Tmp.v1.trns(ang, e.finpow() * 18f);
                Drawf.tri(e.x + Tmp.v1.x, e.y + Tmp.v1.y, 4f * e.fout(), len * e.fout(), ang);
            }

            Draw.color(smokeColor);
            randLenVectors(e.id, 7, 3f + 22f * e.finpow(), (x, y) -> {
                Fill.circle(e.x + x, e.y + y, e.fout() * 3.5f + 0.5f);
            });

            Draw.color(e.color, smokeColor, e.fin());
            Lines.stroke(2f * e.fout());
            randLenVectors(e.id + 1, 10, 2f + 28f * e.finpow(), (x, y) -> {
                Lines.lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 2f + e.fout() * 4f);
            });

            if(e.time <= 1f) Effect.shake(2f, 2f, e.x, e.y);
        }
    }),

    hitSage = new Effect(120f, e -> {
        rand.setSeed(e.id);
        Draw.color(e.color, 0.7f);
        Angles.randLenVectors(e.id, 10, 32f * Interp.pow5Out.apply(Mathf.clamp(e.fin() * 2f)), (x, y) ->
        Fill.circle(e.x + x, e.y + y, e.foutpowdown() * rand.random(3f, 7f))
        );
    }),

    lightPillar = new Effect(180f, e -> {
        float radius = e.rotation;
        float hGrow = Mathf.curve(e.fin(), 0f, 0.15f);
        float alpha = e.fout(Interp.pow2Out);

        Draw.z(Layer.scorch);
        Draw.color(e.color, alpha * 0.12f);
        Fill.circle(e.x, e.y, radius);

        Draw.z(Layer.effect);
        int layers = 12;
        for(int i = 0; i < layers; i++){
            float f = i / (float)(layers - 1);
            float layerAlpha = alpha * (1f - f) * 0.8f;
            if(layerAlpha <= 0.01f) continue;

            Vec2 p = Physics.parallax(Tmp.v1.set(e.x, e.y), radius * 0.088f * hGrow * f);

            Draw.color(e.color, layerAlpha * 0.08f);
            Fill.circle(p.x, p.y, radius);

            if(i == layers - 1){
                Draw.color(e.color, layerAlpha * 0.6f);
                Lines.stroke(layerAlpha);
                Lines.circle(p.x, p.y, radius);
            }
        }
    }),

    pumpOut = new Effect(60f, e -> {
        Draw.color(e.color);
        Draw.alpha(e.fout() / 5);
        vec.trns(e.rotation, 4f).add(e.x, e.y);
        Angles.randLenVectors(e.id, 3, 16 * e.fin(), e.rotation, 10, (x, y) -> {
            Fill.circle(vec.x + x, vec.y + y, 3 * e.fin());
        });
        Draw.alpha(e.fout() / 7);
        vec.trns(e.rotation, 4f).add(e.x, e.y);
        Angles.randLenVectors(e.id + 3, 3, 16 * e.fin(), e.rotation, 20, (x, y) -> {
            Fill.rect(vec.x + x, vec.y + y, 5 * e.fin(), e.fin(), vec.angleTo(vec.x + x, vec.y + y));
        });
    }),
    pumpIn = new Effect(60f, e -> {
        Draw.color(e.color);
        Draw.alpha(e.fin() / 5);
        vec.trns(e.rotation, 4f).add(e.x, e.y);
        Angles.randLenVectors(e.id, 3, 16 * e.fout(), e.rotation, 10, (x, y) -> {
            Fill.circle(vec.x + x, vec.y + y, 3 * e.fout());
        });
        Draw.alpha(e.fin() / 7);
        Angles.randLenVectors(e.id + 3, 3, 16 * e.fout(), e.rotation, 20, (x, y) -> {
            Fill.rect(vec.x + x, vec.y + y, 5 * e.fout(), e.fout(), vec.angleTo(vec.x + x, vec.y + y));
        });
    }),

    sageFire = new Effect(180f, e -> {
        float radius = e.rotation;
        float alpha = e.fout(Interp.pow2Out);

        rand.setSeed(e.id);
        for(int i = 0; i < 55; i++){
            float ang = rand.random(360f);
            float len = rand.random(radius * 0.85f);
            float vx = e.x + Angles.trnsx(ang, len);
            float vy = e.y + Angles.trnsy(ang, len);

            float pTime = (Time.time + rand.random(100f)) / 8f;
            float pSize = Mathf.absin(pTime, 1f, 1f);

            Draw.color(e.color, Color.white, rand.random(0.2f));
            Draw.alpha(alpha * pSize);
            Fill.circle(vx, vy, (1.1f + rand.random(3f)) * pSize * alpha);
        }
    }),

    sageWeaponShoot = new Effect(15f, e -> {
        Draw.color(Color.valueOf("d1efff"), Color.valueOf("8ca9e8"), e.fin());

        randLenVectors(e.id, 4, 12f * e.fin(), (x, y) -> {
            Fill.square(e.x + x, e.y + y, 2f * e.fout(), 45f);
        });

        Drawf.light(e.x, e.y, 20f * e.fout(), Color.valueOf("8ca9e8"), 0.5f);
    }),

    sageWeaponHit = new Effect(20f, e -> {
        Draw.color(Color.valueOf("d1efff"), Color.valueOf("8ca9e8"), e.fin());

        Lines.stroke(e.fout() * 1.6f);
        Lines.circle(e.x, e.y, 2f + 10f * e.finpow());

        rand.setSeed(e.id);

        for(int i = 0; i < 4; i++){
            float ang = rand.range(360f);
            float len = rand.random(3f, 18f) * e.finpow();
            vec.trns(ang, len);
            Drawf.tri(e.x + vec.x, e.y + vec.y, 1.5f * e.fout(), 5f * e.fout(), ang);
        }

        for(int i = 0; i < 3; i++){
            float ang = rand.random(360f);
            float len = rand.random(2f, 9f);
            Draw.color(Color.valueOf("d1efff"));
            Lines.stroke(e.fout() * 1.1f);
            Lines.lineAngle(e.x, e.y, ang, len * e.fout());
        }

        Drawf.light(e.x, e.y, 30f * e.fout(), Color.valueOf("8ca9e8"), 0.5f);
    }),

    sageWeaponTrail = new Effect(25f, e -> {
        rand.setSeed(e.id);
        Color color1 = Color.valueOf("8ca9e8");
        Color color2 = Color.valueOf("d1efff");
        float fout = e.fout();
        float width = (1.2f + rand.random(0.8f)) * fout;
        float length = (2f + rand.random(4f)) * fout;

        if(rand.chance(0.25)){
            Draw.color(color1);
            Fill.circle(e.x, e.y, width);
            Draw.color(color2);
            Fill.circle(e.x, e.y, width * 0.6f);
        }else{
            Draw.color(color1);
            Fill.rect(e.x, e.y, length, width, e.rotation);
            Draw.color(color2);
            Fill.rect(e.x, e.y, length * 0.5f, width, e.rotation);
        }
    }),

    scratchMarks = new Effect(110f, e -> {
        float ox = 0, oy = 0;
        if(e.data instanceof Bullet b && b.owner() instanceof Unit u){
            ox = u.vel.x * e.time;
            oy = u.vel.y * e.time;
        }

        for(int i : Mathf.signs){
            float tx = e.x + ox + Angles.trnsx(e.rotation - 90, 1.6f * i);
            float ty = e.y + Angles.trnsy(e.rotation - 90, 1.6f * i);
            
            Tmp.c1.set(Color.white).lerp(Pal.lightOrange, Mathf.clamp(e.fin() * 4f)).lerp(Color.black, e.fin());
            Draw.color(Tmp.c1);
            
            Lines.stroke(1.2f * e.fout(Interp.pow2Out));
            Lines.lineAngle(tx, ty, e.rotation, -4f);
        }

        Draw.color(Pal.lightOrange, Color.white, e.fin());
        final float fox = ox, foy = oy;
        if(rand.chance(0.5)){
            randLenVectors(e.id, 1, 12f * e.finpow(), e.rotation, 40f, (x, y) -> {
                Fill.circle(e.x + fox + x, e.y + foy + y, e.fout() * 1.1f);
            });
        }
    }).layer(Layer.blockOver),

    // TODO make it work without that library
    shootShockwave = new Effect(60f, e -> {
        Draw.color(Color.valueOf("8CA9E8"));
        float fin = Interp.circleOut.apply(e.fout());
        float fin2 = (new Interp.ExpOut(10f, 10f)).apply(e.fin());
        float fout = (new Interp.ExpOut(10f, 10f)).apply(e.fout());
        float progress = e.fin();
        float cover = 280f * fin2 - 40f * Mathf.slope(Interp.circleOut.apply(e.fin()));
        vec.trns(e.rotation, 5.5f - 15f * fin).add(e.x, e.y);
        OlDraw.donutEllipse(vec.x, vec.y, 4f * progress * fout, 7f * fout, 2f * progress * fout, 6f * fout, cover / 360f, -cover / 2f, e.rotation);
    }).followParent(true).rotWithParent(true),

    staticStone = new Effect(250f, e -> {
        if(!(e.data instanceof RockData data)) return;

        Tile tile = Vars.world.tileWorld(e.x, e.y);
        boolean liquid = tile != null && tile.floor().isLiquid;
        boolean deep = tile != null && !tile.floor().shallow;

        if(liquid){
            Draw.z(Layer.debris);
            Draw.color(e.color);
            Draw.mixcol(tile.floor().mapColor, 0.2f + 0.6f * e.fin());
            Draw.alpha(e.fout());

            float sinkTime = e.finpow();

            if(deep){
                float sinkY = -12f * sinkTime;

                float sway = Mathf.randomSeedRange(e.id, 5f) * sinkTime;
                float rot = Mathf.randomSeed(e.id) * 360 + Mathf.randomSeedRange(e.id + 1, 20f) * sinkTime;

                Draw.rect(data.region, e.x + sway, e.y + sinkY, rot);
            }else{
                Draw.rect(data.region, e.x, e.y + Math.max(-Interp.pow2In.apply(sinkTime) * 8f, -3f), Mathf.randomSeed(e.id) * 360);
            }

            Draw.mixcol();
            return;
        }

        Draw.z(Layer.power + 0.1f);
        Draw.color(e.color);
        Draw.alpha(e.fout());
        Draw.rect(data.region, e.x, e.y, Mathf.randomSeed(e.id) * 360);
    });
}