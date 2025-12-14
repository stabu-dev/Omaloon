package omaloon.content;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.graphics.*;
import mindustry.world.*;
import omaloon.entities.bullet.FallingRockBulletType.*;
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

    fellStone = new Effect(120f, e -> {
        if(!(e.data instanceof RockData data)) return;

        rand.setSeed(e.id);
        vec.trns(rand.random(360f), data.bullet.lifetime / 2f + rand.random(data.bullet.lifetime));
        float scl = Interp.bounceIn.apply(e.fout() - 0.3f);
        float rot = vec.angle();
        float x = e.x + (vec.x * e.finpow()), y = e.y + (vec.y * e.finpow());

        Draw.z(Layer.power + 0.1f);
        Draw.mixcol(Pal.shadow, 1f);
        Draw.alpha(Math.min(e.fout(), Pal.shadow.a));
        Draw.rect(data.region, x, y, rot);
//        Drawm.shadow(data.region, x, y, rot, Math.min(e.fout(), Pal.shadow.a));
        Draw.mixcol();

        Draw.z(Layer.power + 0.2f);
        Draw.color(e.color);
        Draw.alpha(e.fout());
        Draw.rect(data.region, x, y + (scl * data.bullet.lifetime / 2f), rot);
    }),

    glacied = new Effect(80f, e -> {
        color(OlStatusEffects.glacied.color);
        alpha(Mathf.clamp(e.fin() * 2f));

        Fill.circle(e.x, e.y, e.fout());
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

    staticStone = new Effect(250f, e -> {
        if(!(e.data instanceof RockData data)) return;

        Draw.z(Layer.power + 0.1f);
        Draw.color(e.color);
        Draw.alpha(e.fout());
        Draw.rect(data.region, e.x, e.y, Mathf.randomSeed(e.id) * 360);
    });
}
