package omaloon.content;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import mindustry.entities.*;
import mindustry.graphics.*;
import mindustry.world.*;
import omaloon.entities.bullet.*;
import omaloon.graphics.*;
import omaloon.math.*;

import static arc.graphics.g2d.Draw.*;

public class OlFx {
    public static final Rand rand = new Rand();
    public static final Vec2 vec = new Vec2(), vec2 = new Vec2();
    public static Effect

    bigExplosionStone = new Effect(80f, e -> {
        Angles.randLenVectors(e.id, 22, e.fin() * 50f, (x, y) -> {
            float elevation = Interp.bounceIn.apply(e.fout() - 0.3f) * (Mathf.randomSeed((int) Angles.angle(x, y), 30f, 60f));

            Draw.z(Layer.power + 0.1f);
            Draw.color(Pal.shadow);
            Fill.circle(e.x + x, e.y + y, 12f);

            Draw.z(Layer.power + 0.2f);
            Draw.color(e.color);
            Fill.circle(e.x + x, e.y + y + elevation, 12f);
        });

    }),

    carborundumCraft = new Effect(60f, e -> {
        rand.setSeed(e.id);
        Draw.color(Color.valueOf("7545D5").mul(1.5f));
        Angles.randLenVectors(e.id, 10, 8 * e.finpow(), (x, y) -> {
            vec.trns(Mathf.angle(x, y), 8f).add(x + e.x, y + e.y);
            float rad = (3 + rand.range(2));
            Drawf.light(vec.x, vec.y, (rad + 8f) * e.fout(), Color.valueOf("7545D5"), 0.3f);
            Fill.circle(vec.x, vec.y, rad * e.fout());
        });

        if (e.time <= 5)Effect.shake(0.5f, 5f, e.x, e.y);
    }),

    explosionStone = new Effect(60f, e -> {
        Angles.randLenVectors(e.id, 12, e.fin() * 50f, (x, y) -> {
            float elevation = Interp.bounceIn.apply(e.fout() - 0.3f) * (Mathf.randomSeed((int) Angles.angle(x, y), 30f, 60f));

            Draw.z(Layer.power + 0.1f);
            Draw.color(Pal.shadow);
            Fill.circle(e.x + x, e.y + y, 12f);

            Draw.z(Layer.power + 0.2f);
            Draw.color(e.color);
            Fill.circle(e.x + x, e.y + y + elevation, 12f);
        });

    }),

    fellStone = new Effect(120f, e -> {
        if(!(e.data instanceof HailStoneBulletType.HailStoneData data)) return;

        vec2.trns(Mathf.randomSeed(e.id) * 360, data.fallTime/2 + Mathf.randomSeed(e.id + 1) * data.fallTime);
        float scl = Interp.bounceIn.apply(e.fout() - 0.3f);
        float rot = vec2.angle();
        float x = e.x + (vec2.x * e.finpow()), y = e.y + (vec2.y * e.finpow());

        Draw.z(Layer.power + 0.1f);
        Drawm.shadow(data.region, x, y, rot, Math.min(e.fout(), Pal.shadow.a));

        Draw.z(Layer.power + 0.2f);
        Draw.color(e.color);
        Draw.alpha(e.fout());
        Draw.rect(data.region, x, y + (scl * data.fallTime/2), rot);
    }),

    hammerHit = new Effect(80f, e -> {
        color(e.color, Color.gray, e.fin());
        alpha(0.6f);
        Draw.z(Layer.block);

        rand.setSeed(e.id);
        for(int i = 0; i < 3; i++) {
            float len = rand.random(6f), rot = rand.range(40f) + e.rotation;

            e.scaled(e.lifetime * rand.random(0.3f, 1f), e2 -> {
                vec.trns(rot, len * e2.finpow());

                Fill.square(e2.x + vec.x, e2.y + vec.y, 1.5f * e2.fslope() + 0.2f, 45);
            });
        }
    }),

    shelterRotate = new Effect(60f, e -> {
        if (!(e.data instanceof Block data)) return;

        Draw.color(Pal.accent, e.fout());
        Fill.rect(e.x, e.y, data.size * 8f, data.size * 8f);
        Lines.stroke(2f * e.fout(), Pal.accent);
        Lines.rect(
          e.x - data.size * 4f,
          e.y - data.size * 4f,
          data.size * 8f,
          data.size * 8f
        );
        Lines.circle(e.x, e.y, data.size * 16f * e.finpow());
        vec.trns(e.rotation, data.size * 16f * e.finpow()).add(e.x, e.y);

        Drawf.tri(vec.x, vec.y, 4f, 8f * e.foutpow(), e.rotation);
    }),

    staticStone = new Effect(250f, e -> {
        if(!(e.data instanceof HailStoneBulletType.HailStoneData data)) return;

        Draw.z(Layer.power + 0.1f);
        Draw.color(e.color);
        Draw.alpha(e.fout());
        Draw.rect(data.region, e.x, e.y, Mathf.randomSeed(e.id) * 360);
    }),

    windTail = new Effect(100f, e -> {

        Draw.color(Color.white);
        Draw.z(Layer.space - 0.1f);

        rand.setSeed(e.id);

        float rx = rand.random(-1, 1) + 0.01f, ry = rand.random(-1, 1) - 0.01f, dis = rand.random(120, 200);
        float force = rand.random(10, 40);
        float z = rand.random(0, 10);
        Vec3[] windTailPoints = new Vec3[12];

        for(int i = 0; i < 12; i++){
            float scl = (e.fin() - i * 0.05f);
            float x = (scl * dis) + Mathf.cos(scl * 10) * force * rx;
            float y = Mathf.sin(scl * 10) * force * ry;

            vec.trns(e.rotation,x, y);
            vec.add(e.x, e.y);
            vec.add(Math3D.xOffset(e.x, z), Math3D.yOffset(e.y, z));

            windTailPoints[i] = new Vec3(vec.x, vec.y, e.fslope());
        }

        for (int i = 0; i < windTailPoints.length - 1; i++) {
            Vec3 v1 = windTailPoints[i];
            Vec3 v2 = windTailPoints[i + 1];

            Draw.alpha(Mathf.clamp(v1.z, 0.04f, 0.1f));
            Lines.stroke(v1.z);
            Lines.line(v1.x, v1.y, v2.x, v2.y);
        }

    });
}
