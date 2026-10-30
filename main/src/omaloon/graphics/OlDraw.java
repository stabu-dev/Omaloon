package omaloon.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.graphics.*;
import omaloon.content.*;
import omaloon.graphics.BlastRenderer.*;
import omaloon.math.*;

public class OlDraw{
    /**
     * Draws an elliptical donut sector using native Fill.tri.
     * @param x Center X
     * @param y Center Y
     * @param radiusInnerX Inner radius on X axis
     * @param radiusInnerY Inner radius on Y axis
     * @param radiusOuterX Outer radius on X axis
     * @param radiusOuterY Outer radius on Y axis
     * @param percent Coverage percentage (0-1)
     * @param angleOffset Starting angle offset in degrees
     * @param rotation Overall rotation of the ellipse in degrees
     */
    public static void donutEllipse(float x, float y, float radiusInnerX, float radiusInnerY, float radiusOuterX, float radiusOuterY, float percent, float angleOffset, float rotation) {
        int segments = (int)(40 * percent);
        if (segments < 3) segments = 3;

        float step = 360f * percent / segments;

        for (int i = 0; i < segments; i++) {
            float angle1 = i * step + angleOffset;
            float angle2 = (i + 1) * step + angleOffset;

            float cos1 = Mathf.cosDeg(angle1);
            float sin1 = Mathf.sinDeg(angle1);
            float cos2 = Mathf.cosDeg(angle2);
            float sin2 = Mathf.sinDeg(angle2);

            // Points before rotation
            float x1i = cos1 * radiusInnerX;
            float y1i = sin1 * radiusInnerY;
            float x1o = cos1 * radiusOuterX;
            float y1o = sin1 * radiusOuterY;

            float x2i = cos2 * radiusInnerX;
            float y2i = sin2 * radiusInnerY;
            float x2o = cos2 * radiusOuterX;
            float y2o = sin2 * radiusOuterY;

            // Apply rotation and center
            float cosR = Mathf.cosDeg(rotation);
            float sinR = Mathf.sinDeg(rotation);

            // Rotate and translate function
            // x' = x*cos - y*sin + cx
            // y' = x*sin + y*cos + cy

            float px1i = x1i * cosR - y1i * sinR + x;
            float py1i = x1i * sinR + y1i * cosR + y;
            float px1o = x1o * cosR - y1o * sinR + x;
            float py1o = x1o * sinR + y1o * cosR + y;

            float px2i = x2i * cosR - y2i * sinR + x;
            float py2i = x2i * sinR + y2i * cosR + y;
            float px2o = x2o * cosR - y2o * sinR + x;
            float py2o = x2o * sinR + y2o * cosR + y;

            // Draw two triangles to form a trapezoid segment
            Fill.tri(px1i, py1i, px1o, py1o, px2o, py2o);
            Fill.tri(px1i, py1i, px2o, py2o, px2i, py2i);
        }
    }

    /** @return lower near the camera center and at greater heights. */
    public static float underCamera(Vec2 pos, float radius, float height){
        float dst = Mathf.dst(pos.x, pos.y, Core.camera.position.x, Core.camera.position.y);
        return 1f - 0.75f * height * (1f - Mathf.curve(dst, radius * 0.4f, radius * 2.5f));
    }

    private static final Rand puffRand = new Rand();
    private static final Rect viewBounds = new Rect();

    public static void drawMushroom(Effect.EffectContainer e){
        if(!(e.data instanceof Cloud cloud)) return;
        Core.camera.bounds(viewBounds);
        float size = e.rotation, x = e.x, y = e.y, t = e.time;
        cloud.begin(e.id, size);
        float rise = Interp.pow2Out.apply(Mathf.clamp(t / 300f)), spread = Interp.pow2Out.apply(Mathf.clamp(t / 600f));

        float alpha = Mathf.clamp(t / 50f), dissolve = Mathf.curve(e.fin(), 0.4f, 1f);
        float heat = 1f - Mathf.clamp(t / 200f);
        float top = 7f * rise;

        int stem = 46;
        for(int i = 0; i < stem; i++){
            float f = (i / (float)stem + t * 0.0016f * cloud.random(0.8f, 1.2f)) % 1f, h = top * f;
            float wobble = cloud.random(360f), rot = cloud.random(360f) + t * cloud.range(0.15f), pulse = 1f + 0.12f * Mathf.sinDeg(t * 1.7f + wobble);
            float rad = size * Mathf.lerp(0.85f, 0.32f, Interp.pow3Out.apply(Mathf.curve(f, 0f, 0.3f))) * (0.6f + 0.4f * rise) * (1f + spread * 0.25f) * pulse;
            float side = size * (0.12f * Mathf.sinDeg(t * 0.45f + wobble) + 0.7f * Interp.pow2In.apply(dissolve));
            float gone = Mathf.clamp(dissolve * 1.5f - (1f - f) * 0.5f);
            rad *= 1f + dissolve * 0.8f;

            Vec2 p = Physics.parallax(Tmp.v1.set(x + Angles.trnsx(wobble, side), y + Angles.trnsy(wobble, side)), h);
            float scl = 1f + Physics.hMul(h);
            Tmp.c1.set(Pal.rubble).lerp(Color.darkGray, 0.35f + 0.4f * f).lerp(Pal.lightOrange, heat * f * 0.6f);
            Tmp.c1.a = alpha * 0.5f * (1f - Interp.pow2Out.apply(gone)) * underCamera(p, rad * scl, f * 0.5f) * Mathf.clamp(f * 10f) * Mathf.clamp((1f - f) * 4f);

            Draw.z(Layer.flyingUnit + 2f + h * 0.01f);
            Draw.color(Tmp.c1);
            smokePuff(p.x, p.y, rad * scl * 2f, rot, cloud.random(1f));
        }

        float collar = Mathf.curve(t, 60f, 140f) * (1f - Mathf.curve(t, 300f, 480f));
        if(collar > 0f){
            float ch = top * 0.5f, cs = 1f + Physics.hMul(ch), cr = size * (0.9f + 0.4f * spread) * cs;
            Draw.z(Layer.flyingUnit + 2f + ch * 0.01f);
            for(int i = 0; i < 18; i++){
                float ang = i * 20f + Mathf.randomSeedRange(e.id + i, 8f) + t * 0.05f;
                Vec2 c = Physics.parallax(Tmp.v2.set(x, y), ch).add(Tmp.v1.trns(ang, cr));
                Draw.color(Color.lightGray, Color.gray, 0.5f + 0.5f * Mathf.sinDeg(ang - 45f));
                Draw.alpha(alpha * 0.25f * collar * underCamera(c, cr, 0.5f));
                smokePuff(c.x, c.y, size * 0.55f * cs, Mathf.randomSeed(e.id + i + 40, 360f), Mathf.randomSeed(e.id + i + 80));
            }
        }

        if(cloud.wave != null){
            float surgeRad = size * 3.5f;
            for(int i = 0; i < cloud.dust.length / 8; i++){
                int o = i * 8;
                float[] shape = cloud.dust;
                float edge = shape[o], dst = surgeRad * edge, ang = shape[o + 1];
                float local = t - shape[o + 7];
                if(local <= 0f) continue;

                float push = Interp.pow3Out.apply(Mathf.curve(local, 0f, 90f)), draw = Interp.smooth.apply(Mathf.curve(local, 90f, 520f)), grow = Interp.pow2Out.apply(Mathf.curve(local, 0f, 350f));
                float settle = Interp.pow2In.apply(Mathf.curve(t, 380f + 280f * edge, 500f + 280f * edge)), lifted = 1f - Mathf.curve(local, 20f, 120f);
                float roll = shape[o + 2] + local * 0.8f, rollRad = size * 0.25f * grow;
                float out = dst + size * 0.6f * push - dst * 0.2f * draw + Mathf.cosDeg(roll) * rollRad;
                float h = Math.max(0.12f * push + (0.2f + 0.8f * (1f - edge)) * draw + Mathf.sinDeg(roll) * 0.15f * grow, 0f) * shape[o + 3] * (1f - settle);
                float scl = 1f + Physics.hMul(h);
                float rad = size * shape[o + 4] * Mathf.lerp(0.6f, 1.4f, grow) * (1f - 0.5f * settle) * scl;

                Vec2 c = Physics.parallax(Tmp.v2.set(x, y).add(Tmp.v1.trns(ang, out)), h);
                Draw.z(Layer.flyingUnit + 2f + h * 0.01f);
                Draw.color(Tmp.c1.set(Color.lightGray).lerp(Color.gray, 0.5f * grow * (1f - lifted)).lerp(Pal.rubble, 0.2f));
                Draw.alpha(alpha * Mathf.lerp(0.4f, 0.6f, lifted) * Mathf.lerp(0.5f, 1f, edge) * Mathf.curve(local, 0f, 8f) * (1f - settle) * underCamera(c, rad, h * 0.5f));
                smokePuff(c.x, c.y, rad, shape[o + 5] + local * 0.2f, shape[o + 6]);
            }
        }

        float capRad = size * (0.5f + 0.65f * spread + 0.6f * dissolve) * (0.4f + 0.6f * rise), thick = 1.6f * rise * (1f - 0.6f * dissolve);
        Vec2 core = Physics.parallax(Tmp.v2.set(x, y), top);
        float coreScl = 1f + Physics.hMul(top);
        if(heat > 0f){
            Draw.z(Layer.flyingUnit + 2f + (top - 0.01f) * 0.01f);
            Draw.color(Pal.lighterOrange, alpha * heat * 0.9f);
            smokePuff(core.x, core.y, capRad * 1.2f * coreScl, t * 0.3f, 0.5f);
            Drawf.light(core.x, core.y, capRad * 3f * coreScl, Pal.lightOrange, heat);
        }

        int ring = 44;
        for(int i = 0; i < ring; i++){
            float ang = cloud.random(360f) + t * 0.02f, rot = cloud.random(360f) + t * cloud.range(0.2f);
            float roll = cloud.random(360f) + t * 0.7f * cloud.random(0.7f, 1.3f);
            float h = top + Mathf.sinDeg(roll) * thick, out = 0.6f + 0.25f * Mathf.cosDeg(roll) + 0.35f * dissolve * cloud.random(0.5f, 1f);
            float rad = capRad * cloud.random(0.32f, 0.5f) * (1f + 0.1f * Mathf.sinDeg(t * 1.4f + roll)) * (1f + 0.5f * dissolve);

            Vec2 c = Physics.parallax(Tmp.v2.set(x, y), h);
            float scl = 1f + Physics.hMul(h);
            Tmp.v1.trns(ang, capRad * out * scl).add(c);

            float light = 0.5f + 0.5f * Mathf.sinDeg(roll);
            Tmp.c1.set(Color.darkGray).lerp(Color.lightGray, light).lerp(Pal.lightOrange, heat * (1f - light));
            Tmp.c1.a = alpha * 0.4f * (1f - Interp.pow2In.apply(dissolve)) * underCamera(Tmp.v1, capRad * scl, 1f);

            Draw.z(Layer.flyingUnit + 2f + h * 0.01f);
            Draw.color(Tmp.c1);
            smokePuff(Tmp.v1.x, Tmp.v1.y, rad * scl * 2f, rot, cloud.random(1f));
        }
        Draw.color();
        OlFx.rand.setState(cloud.seed0, cloud.seed1);
    }

    /** Draws a smoke puff as a cluster of circles. The seed, from 0 to 1, picks the shape. */
    public static void smokePuff(float x, float y, float size, float rotation, float seed){
        float bound = Math.abs(size) * 0.65f;
        if(BlastRenderer.obscured() || Draw.getColor().a <= 0f || !viewBounds.overlaps(x - bound, y - bound, bound * 2f, bound * 2f)) return;
        puffRand.setSeed((long)(seed * 1000000f));
        for(int i = 0; i < 5; i++){
            float ang = puffRand.random(360f) + rotation, dst = size * 0.3f * Mathf.sqrt(puffRand.nextFloat());
            float rad = size * puffRand.random(0.16f, 0.3f) * (1f + 0.15f * Mathf.sinDeg(Time.time * 1.5f + puffRand.random(360f)));
            Fill.circle(x + Angles.trnsx(ang, dst), y + Angles.trnsy(ang, dst), rad);
        }
    }

    /** Replays the shape's seeded random sequence without regenerating it each frame. */
    public static class Cloud extends Rand{
        final Wave wave;
        final LongSeq values = new LongSeq();
        final float[] dust = new float[180 * 8];
        int index;

        public Cloud(Wave wave){
            this.wave = wave;
        }

        void begin(int id, float size){
            index = 0;
            if(!values.isEmpty()) return;
            setSeed(id);
            for(int i = 0; i < dust.length / 8; i++){
                int o = i * 8;
                dust[o] = Mathf.sqrt(Mathf.randomSeed(id + i + 120));
                dust[o + 1] = Mathf.randomSeed(id + i + 160, 360f);
                dust[o + 2] = Mathf.randomSeed(id + i + 400, 360f);
                dust[o + 3] = Mathf.randomSeed(id + i + 200, 0.6f, 1f);
                dust[o + 4] = Mathf.randomSeed(id + i + 280, 0.6f, 0.9f);
                dust[o + 5] = Mathf.randomSeed(id + i + 440, 360f);
                dust[o + 6] = Mathf.randomSeed(id + i + 360);
                if(wave != null) dust[o + 7] = wave.arrival(size * 3.5f * dust[o]);
            }
        }

        @Override
        public long nextLong(){
            if(index == values.size) values.add(super.nextLong());
            return values.get(index++);
        }
    }
}
