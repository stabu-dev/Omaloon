package omaloon.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.entities.*;
import mindustry.gen.*;
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

    private static final Rand puffRand = new Rand();
    private static final Rect viewBounds = new Rect();
    private static final Seq<EffectState> smokeEffects = new Seq<>(), attackEffects = new Seq<>();
    private static long coverageFrame = -1;
    private static int effectOwner;

    public static void clearEffects(){
        smokeEffects.clear();
        attackEffects.clear();
        coverageFrame = -1;
        Pillars.clear();
    }

    public static void drawMushroom(Effect.EffectContainer e){
        if(!(e.data instanceof Cloud cloud) || BlastRenderer.obscured()) return;
        effectOwner = e.id;
        Core.camera.bounds(viewBounds);
        float size = e.rotation, x = e.x, y = e.y, t = e.time;
        cloud.begin(e.id, size, t);
        float top = 7f * Interp.pow2Out.apply(Mathf.curve(t, 20f, 220f)), rise = top / 7f, spread = Interp.pow2Out.apply(Mathf.curve(t, 100f, 600f));

        float alpha = Mathf.clamp(t / 50f), dissolve = Mathf.curve(e.fin(), 0.4f, 1f);
        float heat = 1f - Mathf.curve(t, 100f, 320f);

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
            Tmp.c1.a = alpha * Mathf.curve(t, 50f, 150f) * 0.35f * (1f - Interp.pow2Out.apply(gone)) * Mathf.clamp(f * 10f) * Mathf.clamp((1f - f) * 4f);

            Draw.z(Layer.flyingUnit + 2f + h * 0.01f);
            Draw.color(Tmp.c1);
            smokePuff(p.x, p.y, rad * scl * 2f, rot, cloud.random(1f));
        }

        float collar = Mathf.curve(t, 120f, 200f) * (1f - Mathf.curve(t, 300f, 480f));
        if(collar > 0f){
            float ch = top * 0.5f, cs = 1f + Physics.hMul(ch), cr = size * (0.9f + 0.4f * spread) * cs;
            Draw.z(Layer.flyingUnit + 2f + ch * 0.01f);
            for(int i = 0; i < 18; i++){
                float ang = i * 20f + Mathf.randomSeedRange(e.id + i, 8f) + t * 0.05f;
                Vec2 c = Physics.parallax(Tmp.v2.set(x, y), ch).add(Tmp.v1.trns(ang, cr));
                Draw.color(Color.lightGray, Color.gray, 0.5f + 0.5f * Mathf.sinDeg(ang - 45f));
                Draw.alpha(alpha * 0.25f * collar);
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
                Draw.alpha(alpha * Mathf.lerp(0.4f, 0.6f, lifted) * Mathf.lerp(0.5f, 1f, edge) * Mathf.curve(local, 0f, 8f) * (1f - settle));
                smokePuff(c.x, c.y, rad, shape[o + 5] + local * 0.2f, shape[o + 6]);
            }
        }

        float formation = Interp.smooth.apply(Mathf.curve(t, 60f, 220f));
        float capRad = size * (0.9f - 0.4f * formation + 0.65f * spread + 0.6f * dissolve) * (0.4f + 0.6f * Interp.pow3Out.apply(Mathf.clamp(t / 45f))), thick = 1.6f * rise * (1f - 0.6f * dissolve);

        int ring = 44;
        for(int i = 0; i < ring; i++){
            float ang = cloud.random(360f) + t * 0.02f, rot = cloud.random(360f) + t * cloud.range(0.2f);
            float roll = cloud.random(360f) + t * 0.7f * cloud.random(0.7f, 1.3f);
            float h = top + Mathf.sinDeg(roll) * thick, out = (0.6f + 0.25f * Mathf.cosDeg(roll)) * Mathf.lerp(0.25f, 1f, formation) + 0.35f * dissolve * cloud.random(0.5f, 1f);
            float rad = capRad * cloud.random(0.32f, 0.5f) * (1f + 0.1f * Mathf.sinDeg(t * 1.4f + roll)) * (1f + 0.5f * dissolve);

            Vec2 c = Physics.parallax(Tmp.v2.set(x, y), h);
            float scl = 1f + Physics.hMul(h);
            Tmp.v1.trns(ang, capRad * out * scl).add(c);

            float light = 0.5f + 0.5f * Mathf.sinDeg(roll), hot = heat * (0.15f + 0.5f * (1f - light)), seed = cloud.random(1f);
            Tmp.c1.set(Color.darkGray).lerp(Color.lightGray, light).lerp(Pal.lightOrange, hot);
            Tmp.c1.a = alpha * 0.25f * (1f - Interp.pow2In.apply(dissolve));

            float emission = hot > 0f ? hot * 0.05f * visibility(smokeEffects, Float.floatToIntBits(seed) * 31, Tmp.v1.x, Tmp.v1.y, rad * scl, Tmp.c1.a, true) : 0f;
            if(emission > 0f){
                Draw.z(Layer.flyingUnit + 2.1f);
                Draw.blend(Blending.additive);
                Draw.color(Pal.lightOrange, emission);
                Draw.rect(Core.atlas.find("circle-shadow"), Tmp.v1.x, Tmp.v1.y, rad * scl * 4f, rad * scl * 4f);
                Draw.blend();
            }
            Draw.z(Layer.flyingUnit + 2f + h * 0.01f);
            Draw.color(Tmp.c1);
            smokePuff(Tmp.v1.x, Tmp.v1.y, rad * scl * 2f, rot, seed);
        }
        if(heat > 0f){
            Vec2 glow = Physics.parallax(Tmp.v1.set(x, y), top);
            Drawf.light(glow.x, glow.y, capRad * 3f, Pal.lightOrange, heat);
        }
        Draw.color();
        OlFx.rand.setState(cloud.seed0, cloud.seed1);
    }

    public static void pillarEffect(Effect.EffectContainer e){
        effectOwner = e.id;
        Draw.z(Layer.effect);
    }

    public static boolean effectVisible(float x, float y, float radius, int key){
        float alpha = Draw.getColor().a;
        float visibility = visibility(attackEffects, key, x, y, radius, alpha, false);
        Draw.alpha(alpha * visibility);
        return visibility > 0f;
    }

    public static void effectCircle(float x, float y, float radius, int key){
        float alpha = Draw.getColor().a;
        if(effectVisible(x, y, radius, key)) Fill.circle(x, y, radius);
        Draw.alpha(alpha);
    }

    public static void pillarGlow(){
        Draw.z(Layer.effect + 0.015f);
        Draw.blend(Blending.additive);
    }

    /** Draws a smoke puff as a cluster of circles. The seed, from 0 to 1, picks the shape. */
    public static void smokePuff(float x, float y, float size, float rotation, float seed){
        float bound = Math.abs(size) * 0.65f;
        if(BlastRenderer.obscured() || Draw.getColor().a <= 0f || !viewBounds.overlaps(x - bound, y - bound, bound * 2f, bound * 2f)) return;
        puffRand.setSeed((long)(seed * 1000000f));
        for(int i = 0; i < 5; i++){
            float ang = puffRand.random(360f) + rotation, dst = size * 0.3f * Mathf.sqrt(puffRand.nextFloat());
            float rad = size * puffRand.random(0.16f, 0.3f) * (1f + 0.15f * Mathf.sinDeg(Time.time * 1.5f + puffRand.random(360f)));
            float px = x + Angles.trnsx(ang, dst), py = y + Angles.trnsy(ang, dst);
            float alpha = Draw.getColor().a;
            float visibility = visibility(smokeEffects, Float.floatToIntBits(seed) * 31 + i, px, py, rad, alpha, false);
            if(visibility <= 0f) continue;
            Draw.alpha(alpha * visibility);
            Fill.circle(px, py, rad);
            Draw.alpha(alpha);
        }
    }

    private static float visibility(Seq<EffectState> effects, int key, float x, float y, float radius, float alpha, boolean emission){
        if(coverageFrame != Core.graphics.getFrameId()){
            coverageFrame = Core.graphics.getFrameId();
            smokeEffects.clear();
            attackEffects.clear();
            for(EffectState e : Groups.effect){
                if(e.data instanceof Cloud){
                    smokeEffects.add(e);
                }else if(e.effect == OlFx.collectorHit || e.effect == OlFx.collectorWaves || e.effect == OlFx.hitSage || e.effect == OlFx.sageFire || e.effect == OlFx.sageStar || e.effect == OlFx.sageShockWave){
                    attackEffects.add(e);
                }
            }
        }
        float overlap = 0f, scale = effects == smokeEffects ? 5f : 1.2f;
        for(EffectState e : effects){
            if(e.id == effectOwner) continue;
            float reach = Math.max(e.rotation * scale, 1f);
            float age = e.data instanceof Cloud cloud ? cloud.firstDraw < 0f ? 0f : e.time - cloud.firstDraw : e.time;
            float presence = Mathf.clamp(age / 36f) * Mathf.clamp((e.lifetime - e.time) / 36f);
            float edge = Mathf.clamp((reach - Mathf.dst(x, y, e.x, e.y) - radius) / (reach * 0.4f));
            overlap += presence * Interp.smooth.apply(edge) * Mathf.clamp(e.fout() / Math.max(alpha, 0.01f));
        }
        float density = emission ? 1f / (1f + overlap) : 1f / Mathf.sqrt(1f + overlap), sample = Mathf.randomSeed((long)effectOwner * 0x9e3779b9L + key);
        return Math.min(1f, Interp.smooth.apply(Mathf.clamp((density - sample) / 0.12f + 0.5f)) / Interp.smooth.apply(Mathf.clamp((1f - sample) / 0.12f + 0.5f)));
    }

    public static void lightPillar(Effect.EffectContainer e){
        Pillars.draw(e);
    }

    /** Seeded random sequence of a mushroom cloud, restarted every frame, and its precomputed dust. */
    public static class Cloud extends Rand{
        float firstDraw = -1f;
        final Wave wave;
        final float[] dust = new float[80 * 8];

        public Cloud(Wave wave){
            this.wave = wave;
        }

        void begin(int id, float size, float time){
            setSeed(id);
            if(firstDraw >= 0f) return;
            firstDraw = time;
            for(int i = 0; i < dust.length / 8; i++){
                int o = i * 8;
                dust[o] = Mathf.sqrt(nextFloat());
                dust[o + 1] = random(360f);
                dust[o + 2] = random(360f);
                dust[o + 3] = random(0.6f, 1f);
                dust[o + 4] = random(0.6f, 0.9f);
                dust[o + 5] = random(360f);
                dust[o + 6] = nextFloat();
                if(wave != null) dust[o + 7] = wave.arrival(size * 3.5f * dust[o]);
            }
            setSeed(id);
        }
    }
    /** Native disc masks, rebuilt each frame and merged without polygon clipping or custom shaders. */
    private static class Pillars{
        private static final Seq<Pillar> pillars = new Seq<>();
        private static final Rect bounds = new Rect();
        private static final Blending premultiplied = new Blending(Gl.one, Gl.oneMinusSrcAlpha);
        private static FrameBuffer mask;
        private static TextureRegion circle, glow;
        private static long frame = -1;
        private static int count;

        public static void clear(){
            frame = -1;
            count = 0;
            if(mask != null) mask.dispose();
            if(circle != null) circle.texture.dispose();
            if(glow != null) glow.texture.dispose();
            mask = null;
            circle = glow = null;
        }

        public static void draw(Effect.EffectContainer e){
            if(e.rotation <= 0f) return;
            if(frame != Core.graphics.getFrameId()){
                frame = Core.graphics.getFrameId();
                count = 0;
                bounds.setCentered(e.x, e.y, 0f);
                Draw.draw(Layer.scorch + 0.0123f, () -> render(0));
                Draw.draw(Layer.effect + 0.0123f, () -> render(1));
                Draw.draw(Layer.effect + 0.015f, () -> render(2));
            }
            float alpha = e.fout(Interp.pow2Out), shift = e.rotation * 0.088f * Mathf.curve(e.fin(), 0f, 0.15f) / 48f * Vars.renderer.getDisplayScale();
            float tx = (e.x - Core.camera.position.x) * shift, ty = (e.y - Core.camera.position.y) * shift;
            for(int i = 0; i < count; i++){
                Pillar p = pillars.get(i);
                if(p.x == e.x && p.y == e.y && p.radius == e.rotation && p.tx == tx && p.ty == ty && p.color.equals(e.color)){
                    p.alpha = Math.max(p.alpha, alpha);
                    return;
                }
            }
            if(count == pillars.size) pillars.add(new Pillar());
            Pillar p = pillars.get(count++);
            p.x = e.x;
            p.y = e.y;
            p.radius = e.rotation;
            p.alpha = alpha;
            p.color = e.color;
            p.tx = tx;
            p.ty = ty;
            bounds.merge(Tmp.r1.set(p.x - p.radius * 1.5f + Math.min(0f, p.tx) - 2f, p.y - p.radius * 1.5f + Math.min(0f, p.ty) - 2f, p.radius * 3f + Math.abs(p.tx) + 4f, p.radius * 3f + Math.abs(p.ty) + 4f));
        }

        private static TextureRegion texture(boolean soft){
            //max blending ignores blend factors, so texture and tint carry their alpha in RGB
            Pixmap image = new Pixmap(201, 201);
            image.each((x, y) -> {
                float dst = Mathf.dst(x, y, 100f, 100f) / 100f;
                float alpha = soft ? (float)Math.exp(-4f * dst * dst) * (1f - Interp.smooth.apply(Mathf.curve(dst, 0.8f, 1f))) : 1f - Mathf.curve(dst, 0.98f, 1f);
                image.set(x, y, Color.rgba8888(alpha, alpha, alpha, alpha));
            });
            Texture texture = new Texture(image);
            texture.setFilter(Texture.TextureFilter.linear);
            image.dispose();
            return new TextureRegion(texture);
        }

        private static void render(int layer){
            if(!ScissorStack.pushWorld(Tmp.r2.set(bounds))) return;
            Draw.mixcol();
            if(mask == null){
                mask = new FrameBuffer();
                circle = texture(false);
                glow = texture(true);
            }
            mask.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
            boolean union = Core.graphics.getGLVersion().type == GLVersion.GlType.OpenGL || Core.gl30 != null || Core.graphics.supportsExtension("GL_EXT_blend_minmax");
            for(int disc = 0, n = layer == 1 ? 11 : 1; disc < n; disc++){
                mask.begin(Color.clear);
                if(union) Gl.blendEquation(Gl.max);
                Draw.blend(premultiplied);
                for(int i = 0; i < count; i++){
                    Pillar p = pillars.get(i);
                    float f = layer == 1 ? disc / 11f : 0f, alpha = p.alpha * (layer == 0 ? 0.12f : layer == 1 ? (1f - f) * 0.064f : 0.35f);
                    Draw.color(p.color.r * alpha, p.color.g * alpha, p.color.b * alpha, alpha);
                    float size = p.radius * (layer == 2 ? 3f : 2f);
                    Draw.rect(layer == 2 ? glow : circle, p.x + p.tx * f, p.y + p.ty * f, size, size);
                }
                Draw.flush();
                if(union) Gl.blendEquation(Gl.funcAdd);
                mask.end();
                Draw.color();
                Draw.blend(layer == 2 ? BlastRenderer.additive : premultiplied);
                Draw.rect(Draw.wrap(mask.getTexture()), Core.camera.position.x, Core.camera.position.y, Core.camera.width, -Core.camera.height);
                Draw.flush();
            }
            Draw.blend();
            ScissorStack.pop();
        }

        private static class Pillar{
            Color color;
            float x, y, radius, alpha, tx, ty;
        }
    }
}