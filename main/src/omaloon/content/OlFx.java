package omaloon.content;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import omaloon.entities.bullet.FallingRockBulletType.*;
import omaloon.graphics.*;
import omaloon.math.*;
import omaloon.world.blocks.environment.customsshapeproop.*;

import static arc.graphics.g2d.Draw.*;
import static arc.math.Angles.randLenVectors;

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

    collectorShoot = new Effect(30f, e -> {
        Draw.color(e.color);
        Lines.stroke(e.fout() * 2f);
        Lines.circle(e.x, e.y, e.finpow() * 12f);

        rand.setSeed(e.id);
        for(int i = 0; i < 4; i++){
            float size = rand.random(0.4f, 1f) * e.fout();
            float length = size * 3f;
            float rot = e.rotation + rand.range(25f);
            vec.trns(rot, e.finpow() * 24f * rand.random(0.5f, 1f));

            float cx = e.x + vec.x;
            float cy = e.y + vec.y;

            Draw.alpha(e.fout());
            Fill.rect(cx, cy, length, size);
            float hLength = (length - size) / 2f;
            if(hLength > 0){
                Fill.rect(cx, cy + (size + hLength) / 2f, size, hLength);
                Fill.rect(cx, cy - (size + hLength) / 2f, size, hLength);
            }
        }
    }),

    collectorWaves = new Effect(300, e -> {
        Draw.color(e.color);

        rand.setSeed(e.id + 8);
        for(int i = 0; i < 6; i++){
            int finalI = i;
            float expansion = Interp.pow2Out.apply(Math.min(e.time / 40f, 1f));
            Angles.randLenVectors(e.id + i, 6, e.rotation * expansion, (x, y) -> {
                float delay = rand.random(60f);
                float life = rand.random(20f, 50f);
                float size = rand.random(0.3f, 1.5f);

                float localTime = (e.time + delay) % 60f;
                if(localTime < life){
                    float localFin = localTime / life;
                    float localFslope = Math.min(localFin, 1f - localFin) * 2f;
                    float globalFade = Math.min(e.time / 30f, 1f);

                    Draw.alpha(Interp.smooth.apply(localFslope) * Interp.exp5.apply(e.fslope()) * globalFade);
                    Physics.parallax(vec.set(e.x + x, e.y + y), (finalI + 1) / 6f * e.finpowdown());

                    float length = size * 3f;

                    Fill.rect(vec.x, vec.y, length, size);
                    float hLength = (length - size) / 2f;
                    if(hLength > 0){
                        Fill.rect(vec.x, vec.y + (size + hLength) / 2f, size, hLength);
                        Fill.rect(vec.x, vec.y - (size + hLength) / 2f, size, hLength);
                    }
                }
            });
        }
    }),

    collectorHit = new Effect(40f, e -> {
        Draw.color(e.color);
        Lines.stroke(e.fout() * 2f);
        Lines.circle(e.x, e.y, e.finpow() * e.rotation);

        rand.setSeed(e.id);
        for(int i = 0; i < 7; i++){
            float size = rand.random(0.5f, 1.2f) * e.fout();
            float length = size * 3f;
            float rot = rand.random(360f);
            vec.trns(rot, e.finpow() * e.rotation * rand.random(0.3f, 1.1f));

            float cx = e.x + vec.x;
            float cy = e.y + vec.y;

            Draw.alpha(e.fout());
            Fill.rect(cx, cy, length, size);
            float hLength = (length - size) / 2f;
            if(hLength > 0){
                Fill.rect(cx, cy + (size + hLength) / 2f, size, hLength);
                Fill.rect(cx, cy - (size + hLength) / 2f, size, hLength);
            }
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

    lumenCarcass = new Effect(60f, e -> {
        TextureRegion region = OlUnitTypes.lumen.fullIcon;

        rand.setSeed(e.id);
        Draw.alpha(e.foutpowdown());
        for(int i : Mathf.signs){
            Tmp.tr1.set(region);
            Tmp.tr1.setX(region.getX() + (i + 1f) / 2f * region.width / 2f);
            Tmp.tr1.setWidth(region.width / 2f);
            vec.trns(e.rotation - 90f, region.width / 16f * i + i * rand.random(20f, 50f) * e.finpow(), rand.range(16f) * e.finpow());
            Draw.rect(Tmp.tr1, e.x + vec.x, e.y + vec.y, e.rotation - 90f + rand.random(720f) * e.finpow() * -i);
        }
    }).layer(Layer.flyingUnitLow),

    lumenSplash = new Effect(280f, e -> {
        Liquid liquid = Liquids.water;
        for(Liquid li : Vars.content.liquids()){
            if(li.color.rgb888() == e.color.rgb888()){
                liquid = li;
                break;
            }
        }
        if(liquid == Liquids.water){
            if(e.data instanceof Bullet b && b.type instanceof LiquidBulletType l){
                liquid = l.liquid;
            }else if(e.data instanceof Liquid li){
                liquid = li;
            }
        }

        Color fluidCol = liquid.color;
        Color sprayCol = Tmp.c1.set(fluidCol).mul(1.2f);
        float radius = e.rotation;

        rand.setSeed(e.id);
        int poolCircles = 5;
        for(int i = 0; i < poolCircles; i++){
            float ang = rand.random(360f);
            float dist = rand.random(radius * 0.2f, radius * 1.2f);
            float amount = (Puddles.maxLiquid / 1.5f) * rand.random(0.4f, 1f) * e.fout();
            if(amount <= 0.01f) continue;

            Tmp.v1.trns(ang, dist);
            Puddle puddle = Puddle.create();
            puddle.id = e.id + i;
            puddle.x = e.x + Tmp.v1.x;
            puddle.y = e.y + Tmp.v1.y;
            puddle.amount = amount;
            puddle.tile = Vars.world.tileWorld(puddle.x, puddle.y);
            if(puddle.tile == null) puddle.tile = Vars.world.tileWorld(e.x, e.y);
            if(puddle.tile == null) continue;
            puddle.liquid = liquid;

            Draw.z(Layer.debris - 1f);
            liquid.drawPuddle(puddle);
        }

        e.scaled(80f, i -> {
            Draw.z(Layer.debris);
            Draw.color(fluidCol);
            Lines.stroke(i.fout() * 2f);
            Lines.circle(e.x, e.y, 4f + i.finpow() * radius * 1.5f);
        });

        int dropCount = 24;
        for(int j = 0; j < dropCount; j++){
            rand.setSeed(e.id + j + 1);
            float angle = rand.random(360f);
            float dist = rand.random(radius * 0.5f, radius * 2.5f);
            float lifeScl = rand.random(0.6f, 1f);
            float landTime = 80f * lifeScl;

            if(e.time < landTime){
                float peakH = rand.random(radius * 0.8f, radius * 1.8f);
                float curFin = e.time / landTime;
                float fout = 1f - curFin;
                Tmp.v1.trns(angle, dist * curFin);
                float z = Mathf.sin(curFin * Mathf.PI) * peakH;

                Draw.z(Layer.debris);
                Draw.color(sprayCol);
                Fill.circle(e.x + Tmp.v1.x, e.y + Tmp.v1.y + z, (1.2f * fout + 0.2f) * 1.5f);
            }else{
                float fadeProgress = (e.time - landTime) / (e.lifetime - landTime);
                float amount = (Puddles.maxLiquid / 4.5f) * rand.random(0.3f, 0.7f) * (1f - fadeProgress);
                if(amount <= 0.01f) continue;

                Tmp.v1.trns(angle, dist);
                Puddle puddle = Puddle.create();
                puddle.id = e.id + j + 100;
                puddle.x = e.x + Tmp.v1.x;
                puddle.y = e.y + Tmp.v1.y;
                puddle.amount = amount;
                puddle.tile = Vars.world.tileWorld(puddle.x, puddle.y);
                if(puddle.tile == null) puddle.tile = Vars.world.tileWorld(e.x, e.y);
                if(puddle.tile == null) continue;
                puddle.liquid = liquid;

                Draw.z(Layer.debris - 1f);
                liquid.drawPuddle(puddle);
            }
        }
    }).layer(Layer.debris).followParent(false),

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

    sparkTrail = new Effect(25f, e -> {
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
    }).layer(Layer.bullet - 0.01f),

    helixBeam = new Effect(60f, e -> {
        if(!(e.data instanceof Bullet b)) return;
        ContinuousFlameBulletType type = (ContinuousFlameBulletType) b.type;
        float timeFade = e.fin() < 0.5f ? e.fin() / 0.5f : 1f - (e.fin() - 0.5f) / 0.5f;

        float mult = b.fin(type.lengthInterp);
        float realLength = Damage.findLength(b, type.length * mult, type.laserAbsorb, type.pierceCap);

        float z = Draw.z();
        Draw.z(Layer.bullet + 0.001f);

        float cos = Mathf.cosDeg(e.rotation);
        float sin = Mathf.sinDeg(e.rotation);
        float pCos = -sin, pSin = cos;

        rand.setSeed(b.id);
        float randOffset1 = rand.random(360f);
        float randOffset2 = rand.random(360f);
        float freq1 = 0.05f + rand.random(0.04f);
        float freq2 = 0.09f + rand.random(0.06f);

        float step = 1.5f;
        int segments = (int)(realLength / step);
        Color c2 = Color.valueOf("8CA9E8");

        for(int i = 0; i < segments; i++){
            float d1 = i * step, d2 = (i + 1) * step;
            float fade1 = 1f - (d1 / realLength), fade2 = 1f - (d2 / realLength);
            float startFade1 = Interp.smooth.apply(Math.min(d1 / 18f, 1f));
            float startFade2 = Interp.smooth.apply(Math.min(d2 / 18f, 1f));

            float amp1 = 6f * fade1 * startFade1 * Math.min(realLength / 40f, 1f);
            float amp2 = 6f * fade2 * startFade2 * Math.min(realLength / 40f, 1f);

            float wave1 = Mathf.sin(Time.time * 0.12f - d1 * freq1 + randOffset1) * 0.7f + Mathf.sin(Time.time * 0.22f + d1 * freq2 + randOffset2) * 0.3f;
            float wave2 = Mathf.sin(Time.time * 0.12f - d2 * freq1 + randOffset1) * 0.7f + Mathf.sin(Time.time * 0.22f + d2 * freq2 + randOffset2) * 0.3f;

            float w1 = 1.5f * fade1 * timeFade, w2 = 1.5f * fade2 * timeFade;
            float ox1 = pCos * w1 * 0.5f, oy1 = pSin * w1 * 0.5f;
            float ox2 = pCos * w2 * 0.5f, oy2 = pSin * w2 * 0.5f;

            float cx1 = e.x + cos * d1, cy1 = e.y + sin * d1;
            float cx2 = e.x + cos * d2, cy2 = e.y + sin * d2;

            float wv1 = wave1 * amp1, wv2 = wave2 * amp2;
            float tx1 = cx1 + pCos * wv1, ty1 = cy1 + pSin * wv1;
            float tx2 = cx2 + pCos * wv2, ty2 = cy2 + pSin * wv2;

            Draw.color(e.color);
            Fill.quad(tx1 - ox1, ty1 - oy1, tx1 + ox1, ty1 + oy1, tx2 + ox2, ty2 + oy2, tx2 - ox2, ty2 - oy2);

            wv1 = -wv1; wv2 = -wv2;
            tx1 = cx1 + pCos * wv1; ty1 = cy1 + pSin * wv1;
            tx2 = cx2 + pCos * wv2; ty2 = cy2 + pSin * wv2;

            Draw.color(c2);
            Fill.quad(tx1 - ox1, ty1 - oy1, tx1 + ox1, ty1 + oy1, tx2 + ox2, ty2 + oy2, tx2 - ox2, ty2 - oy2);
        }

        Draw.color();
        Draw.z(z);
    }),

    basilPulses = new Effect(60f, e -> {
        if(!(e.data instanceof Bullet b)) return;
        ContinuousFlameBulletType type = (ContinuousFlameBulletType) b.type;
        float timeFade = e.fin() < 0.5f ? e.fin() / 0.5f : 1f - (e.fin() - 0.5f) / 0.5f;

        float mult = b.fin(type.lengthInterp);
        float realLength = Damage.findLength(b, type.length * mult, type.laserAbsorb, type.pierceCap);

        float z = Draw.z();
        Draw.z(Layer.bullet + 0.001f);

        float cos = Mathf.cosDeg(e.rotation);
        float sin = Mathf.sinDeg(e.rotation);
        float pCos = -sin, pSin = cos;

        Color color1 = Color.valueOf("8ca9e8");
        Color color2 = Color.valueOf("d1efff");

        float baseSpeed = 0.012f; 
        int particleCount = 4;
        for(int i = 0; i < particleCount; i++){
            rand.setSeed(b.id * 10L + i);
            float phase = rand.random(1.0f);
            float speedScale = 0.7f + rand.random(0.6f);
            
            float progress = ((Time.time * baseSpeed * speedScale) + phase) % 1f;

            float d = progress * realLength;
            
            float progressFade = progress < 0.2f ? (progress / 0.2f) : (1f - progress) / 0.8f;
            float fade = timeFade * progressFade;

            float waveFreq = 1.0f + rand.random(1.5f);
            float wavePhase = rand.random(Mathf.PI * 2f);
            float amp = 1.5f + rand.random(1.5f);
            float baseOffset = 3.0f + rand.random(1.5f);
            
            float offsetVal = baseOffset + amp * Mathf.sin(progress * Mathf.PI * 2f * waveFreq + wavePhase);
            float offset = offsetVal * (rand.chance(0.5) ? 1f : -1f);
            float px = e.x + cos * d + pCos * offset;
            float py = e.y + sin * d + pSin * offset;

            float rectLen = (5f + rand.random(4f)) * fade;
            float rectWid = (1.5f + rand.random(1f)) * fade;

            Draw.color(color1);
            Draw.alpha(fade * 0.8f);
            Fill.rect(px, py, rectLen, rectWid, e.rotation);

            Draw.color(color2);
            Draw.alpha(fade);
            Fill.rect(px, py, rectLen * 0.5f, rectWid, e.rotation);
        }

        Draw.color();
        Draw.z(z);
    }),

    basilShoot = new Effect(24f, e -> {
        Draw.color(Color.valueOf("d1efff"), Color.valueOf("8ca9e8"), e.fin());
        Lines.stroke(e.fout() * 1.5f);
        Lines.circle(e.x, e.y, 1f + 14f * e.fin(Interp.pow2Out));

        rand.setSeed(e.id);
        for(int i = 0; i < 6; i++){
            float ang = e.rotation + rand.range(20f);
            float len = 8f + rand.random(12f);
            vec.trns(ang, 2f);
            Drawf.tri(e.x + vec.x, e.y + vec.y, 3f * e.fout(), len * e.fout(), ang);
        }

        Draw.color(Color.white, Color.valueOf("8ca9e8"), e.fin());
        randLenVectors(e.id, 4, 16f * e.fin(), e.rotation, 25f, (x, y) -> {
            Fill.square(e.x + x, e.y + y, 1.2f * e.fout(), 45f);
        });

        Drawf.light(e.x, e.y, 40f * e.fout(), Color.valueOf("8ca9e8"), 0.6f);
    }),

    basilSmoke = new Effect(40f, e -> {
        rand.setSeed(e.id);
        Draw.blend(Blending.additive);

        for(int i = 0; i < 3; i++){
            float ang = e.rotation + rand.range(4f);
            float speed = 40f + rand.random(35f);
            float vx = e.x + Angles.trnsx(ang, speed * e.fin(Interp.pow2Out));
            float vy = e.y + Angles.trnsy(ang, speed * e.fin(Interp.pow2Out));
            float size = (3f + rand.random(3f)) * e.fout(Interp.pow2Out);

            Draw.color(Color.valueOf("8ca9e8"));
            Draw.alpha(0.18f * e.fout());
            Fill.circle(vx, vy, size);
        }

        for(int i = 0; i < 4; i++){
            float ang = e.rotation + rand.range(3f);
            float speed = 50f + rand.random(30f);
            float dist = speed * e.fin(Interp.pow2Out);
            float sx = e.x + Angles.trnsx(ang, dist);
            float sy = e.y + Angles.trnsy(ang, dist);
            float len = (8f + rand.random(10f)) * e.fout();

            Lines.stroke(1.0f * e.fout(), Color.valueOf("d1efff"));
            Lines.lineAngle(sx, sy, ang, len);
        }

        Lines.stroke(1.5f * e.fout());
        Draw.color(Color.valueOf("8ca9e8"), Color.valueOf("d1efff"), e.fin());
        Lines.circle(e.x, e.y, 2f + 6f * e.finpow());

        Draw.blend();
        Draw.color();
    }),

    sageCannonShoot = new Effect(15f, e -> {
        Draw.color(Color.white, Color.valueOf("8ca9e8"), e.fin());

        Lines.stroke(e.fout() * 1.5f);
        Lines.circle(e.x, e.y, 1f + 12f * e.fin(Interp.pow2Out));

        rand.setSeed(e.id);
        for(int i = 0; i < 6; i++){
            float ang = e.rotation + rand.range(30f);
            float len = 8f + rand.random(14f);
            vec.trns(ang, 3f);
            Drawf.tri(e.x + vec.x, e.y + vec.y, 3f * e.fout(), len * e.fout(), ang);
        }

        Drawf.light(e.x, e.y, 35f * e.fout(), Color.valueOf("8ca9e8"), 0.6f);
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

    sageShockWave = new Effect(150f, 150f, e -> {
        float rad = e.rotation * 0.25f;
        rand.setSeed(e.id);

        Draw.color(Color.white, e.color, e.fin() + 0.6f);
        float progress = Interp.circleOut.apply(Mathf.clamp(e.fin() * 3f));
        float circleRad = progress * rad * 4f;
        Lines.stroke(3.5f * e.fout(Interp.pow3Out));
        Lines.circle(e.x, e.y, circleRad);
        for(int i = 0; i < 10; i++){
            Tmp.v1.set(1, 0).setToRandomDirection(rand).scl(circleRad);
            float triWidth = rand.random(circleRad / 12, circleRad / 9) * e.fout(Interp.pow3Out) * 2f;
            float triLength = rand.random(circleRad / 6, circleRad / 3.5f) * (1 + e.fin()) / 2;
            Drawf.tri(e.x + Tmp.v1.x, e.y + Tmp.v1.y, triWidth, triLength, Tmp.v1.angle() - 180);
        }
        Draw.blend(Blending.additive);
        Draw.z(Layer.effect + 0.1f);

        Fill.light(e.x, e.y, Lines.circleVertices(circleRad), circleRad, Color.clear, Tmp.c1.set(Draw.getColor()).a(e.fout(Interp.pow5Out) * 0.25f));
        Draw.blend();
        Draw.z(Layer.effect);

        Drawf.light(e.x, e.y, rad * progress * 4f, e.color, 0.4f * e.fout());
    }).layer(Layer.effect + 0.001f),

    sageStar = new Effect(55f, 150f, e -> {
        float radius = e.rotation;

        Draw.color(e.color);
        e.rotation = e.fin() * 200;
        for(int i = 0; i < 4; i++){
            Drawf.tri(e.x, e.y, e.fout(Interp.pow3Out) * (radius * 0.14f), e.fout(Interp.pow3Out) * (radius * 1.4f), e.rotation + (90 * i));
        }
        Draw.color(Color.white);
        for(int i = 0; i < 4; i++){
            Drawf.tri(e.x, e.y, e.fout(Interp.pow3Out) * (radius * 0.08f), e.fout(Interp.pow3Out) * (radius * 0.85f), e.rotation + (90 * i));
        }
    }).layer(Layer.effect + 0.002f),

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
    }),

    praetorianMissileLaunch = new Effect(40f, e -> {
        float ox = e.x + Angles.trnsx(e.rotation + 180f, 6f);
        float oy = e.y + Angles.trnsy(e.rotation + 180f, 6f);

        Angles.randLenVectors(e.id, 4, 10f * e.fin(Interp.pow3Out), e.rotation + 180f, 20f, (x, y) -> {
            float size = 1.5f + 1.5f * e.fout();
            Draw.color(Pal.lightOrange, Pal.lighterOrange, e.fin());
            Fill.circle(ox + x, oy + y, size * e.fout());
        });

        Angles.randLenVectors(e.id + 1, 6, 16f * e.fin(Interp.pow3Out), e.rotation + 180f, 25f, (x, y) -> {
            float size = 2f + 2f * e.fout();
            Draw.color(Color.gray, Color.darkGray, e.fin());
            Fill.circle(ox + x, oy + y, size * e.fout());
        });
    }),

    praetorianMissileExplosion = new Effect(50f, e -> {
        Draw.color(Color.white, Pal.lighterOrange, e.fin());
        Fill.circle(e.x, e.y, 8f * e.fout());

        Draw.color(Color.white, Pal.lightOrange, e.fin());
        Lines.stroke(1.2f * e.fout());
        Lines.circle(e.x, e.y, 4f + 22f * e.fin(Interp.pow3Out));

        Angles.randLenVectors(e.id, 8, 28f * e.fin(Interp.pow3Out), (x, y) -> {
            float len = 1f + 5f * e.fout();
            float angle = Mathf.angle(x, y);
            Draw.color(Color.white, Pal.lighterOrange, e.fin());
            Lines.stroke(1f * e.fout());
            Lines.lineAngle(e.x + x, e.y + y, angle, len);
        });

        Angles.randLenVectors(e.id + 1, 5, 14f * e.fin(Interp.pow3Out), (x, y) -> {
            float size = 1.5f + 2.5f * e.fout();
            Draw.color(Color.gray, Color.darkGray, e.fin());
            Fill.circle(e.x + x, e.y + y, size * e.fout());
        });

        Drawf.light(e.x, e.y, 45f * e.fout(), Pal.lightOrange, 0.7f);
    });
}