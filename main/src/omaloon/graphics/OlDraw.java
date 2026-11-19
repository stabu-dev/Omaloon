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

public class OlDraw{
    private static final TextureRegion slicedRegion = new TextureRegion();

    public static TextureRegion slice(TextureRegion source, int x, int y, int width, int height){
        slicedRegion.set(source);
        slicedRegion.setX(slicedRegion.getX() + x);
        slicedRegion.setWidth(width);
        slicedRegion.setY(slicedRegion.getY() + y);
        slicedRegion.setHeight(height);
        return slicedRegion;
    }

    public static void drawEdge(TextureRegion source, int dir, boolean front, boolean animated, float layer, float x, float y){
        float px = x + Geometry.d4x(dir) * Vars.tilesize * 0.5625f, py = y + Geometry.d4y(dir) * Vars.tilesize * 0.5625f;
        TextureRegion cut;

        if(animated){
            cut = slice(source, front ? 0 : source.width - 4, 0, 4, source.height);
            if(!front) cut.flip(true, false);
        }else if(dir % 2 == 0){
            cut = slice(source, dir == 0 ? source.width - 4 : 0, 0, 4, source.height);
        }else{
            cut = slice(source, 0, dir == 3 ? source.height - 4 : 0, source.width, 4);
        }

        Draw.z(layer);
        Draw.rect(cut, px, py, animated ? dir * 90f : 0f);
    }

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

    private static final Seq<EffectState> attackEffects = new Seq<>();
    private static long coverageFrame = -1;
    private static int effectOwner;
    private static final FloatSeq afterglow = new FloatSeq();
    private static Bloom glowBloom;
    private static FrameBuffer glowBuffer;
    private static long glowFrame = -1;
    private static boolean glowListener, glowFailed;

    public static void clearEffects(){
        attackEffects.clear();
        coverageFrame = -1;
        Pillars.clear();
        afterglow.clear();
        glowFrame = -1;
        glowFailed = false;
        if(glowBloom != null) glowBloom.dispose();
        if(glowBuffer != null) glowBuffer.dispose();
        glowBloom = null;
        glowBuffer = null;
    }

    /** Queues emitted light after the smoke, without capturing the smoke's opacity. */
    public static void afterglow(float x, float y, float radius, float color, float alpha){
        if(Vars.renderer.bloom == null || glowFailed || alpha <= 0f || radius <= 0f) return;
        long frame = Core.graphics.getFrameId();
        if(glowFrame != frame){
            glowFrame = frame;
            afterglow.clear();
            float z = Draw.z();
            Draw.draw(Layer.flyingUnit + 2.2f, OlDraw::drawAfterglow);
            Draw.z(z);
        }
        afterglow.add(x, y, radius);
        afterglow.add(color, alpha);
    }

    private static void drawAfterglow(){
        if(afterglow.isEmpty()) return;
        Draw.flush();
        if(glowBloom == null){
            try{
                glowBloom = new Bloom(false);
                glowBloom.setOriginalIntensity(0f);
                glowBloom.setThreshold(0.2f);
                glowBuffer = new FrameBuffer(Core.graphics.getWidth(), Core.graphics.getHeight());
            }catch(RuntimeException e){
                if(glowBloom != null) glowBloom.dispose();
                glowBloom = null;
                glowFailed = true;
                Log.err("Afterglow bloom unavailable; keeping the visible heat surface", e);
                return;
            }
            if(!glowListener){
                glowListener = true;
                Core.app.addListener(new ApplicationListener(){
                    @Override
                    public void resume(){
                        if(glowBloom != null) glowBloom.resume();
                    }
                });
            }
        }
        glowBloom.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
        glowBuffer.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
        glowBloom.setBloomIntensity((Core.settings.getInt("bloomintensity", 6) / 4f + 1f) * 0.18f);
        glowBloom.blurPasses = Core.settings.getInt("bloomblur", 1);
        Draw.reset();
        glowBloom.capture();
        Draw.blend(Blending.additive);
        for(int i = 0; i < afterglow.size; i += 5){
            Draw.color(afterglow.get(i + 3));
            Draw.alpha(afterglow.get(i + 4));
            Fill.circle(afterglow.get(i), afterglow.get(i + 1), afterglow.get(i + 2));
        }
        Draw.flush();
        Draw.blend();
        glowBloom.capturePause();
        glowBuffer.begin(Color.clear);
        try{
            glowBloom.render();
        }finally{
            glowBuffer.end();
        }
        Draw.blend(BlastRenderer.additive);
        Draw.color();
        //the halo changes light, never the scene's coverage, including when pixelation uses an offscreen target
        Gl.colorMask(true, true, true, false);
        try{
            Draw.rect(Draw.wrap(glowBuffer.getTexture()), Core.camera.position.x, Core.camera.position.y, Core.camera.width, -Core.camera.height);
            Draw.flush();
        }finally{
            Gl.colorMask(true, true, true, true);
            Draw.blend();
            Draw.reset();
        }
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

    private static float visibility(Seq<EffectState> effects, int key, float x, float y, float radius, float alpha, boolean emission){
        if(coverageFrame != Core.graphics.getFrameId()){
            coverageFrame = Core.graphics.getFrameId();
            attackEffects.clear();
            for(EffectState e : Groups.effect){
                if(e.effect == OlFx.collectorHit || e.effect == OlFx.collectorWaves || e.effect == OlFx.hitSage || e.effect == OlFx.sageFire || e.effect == OlFx.sageStar || e.effect == OlFx.sageShockWave){
                    attackEffects.add(e);
                }
            }
        }
        float overlap = 0f, scale = 1.2f;
        for(EffectState e : effects){
            if(e.id == effectOwner) continue;
            float reach = Math.max(e.rotation * scale, 1f);
            float age = e.time;
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