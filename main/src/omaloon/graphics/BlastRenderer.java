package omaloon.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.pooling.*;
import arc.util.pooling.Pool.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import omaloon.content.*;
import omaloon.world.NuclearBlasts.*;

import java.nio.*;

import static mindustry.Vars.*;

/** Impact frames and shockwave passes sharing one buffer. Silhouettes ported from Flame Out by EyeOfDarkness. */
public class BlastRenderer{
    public static final String qualitySetting = "omaloon-blast-quality", strobeSetting = "omaloon-blast-strobes";
    private static final int maxAreas = 16, maxWaves = 8;
    private static final FloatSeq lights = new FloatSeq(maxAreas * 3), areas = new FloatSeq(maxAreas * 4), shapes = new FloatSeq(maxWaves * 4), states = new FloatSeq(maxWaves * 4);
    private static final float[] strengths = new float[maxAreas];
    private static final float settleTime = 120f;
    private static final Seq<Wave> waves = new Seq<>();
    private static final Rect waveArea = new Rect();
    private static FrameBuffer waveBuffer;
    /** Frames between block silhouette redraws, unless the camera moves or zooms. */
    public static int blockRefresh = 4;
    /** Additive blending for burnt shadows. Their summed alpha darkens the world in one pass, avoiding 8-bit color drift. */
    public static final Blending imprintBlending = new Blending(Gl.one, Gl.one);

    /** Strobe flash duration, in ticks. */
    private static final float strobeTime = 3f;
    /** Alpha the frames drop to when the fade begins. */
    private static final float fadeDrop = 0.55f;

    private static final Seq<Frame> frames = new Seq<>();
    private static final Rand rand = new Rand();
    // Area covered by the frames, in world and window coordinates.
    private static final Rect area = new Rect(), scissor = new Rect();
    private static final IntBuffer savedScissor = Buffers.newIntBuffer(4);

    private static FrameBuffer blocks;
    private static ImpactBatch batch;
    private static boolean initialized, failed;
    private static long blocksFrame = -1;
    // Camera state when the block silhouettes were drawn.
    private static float camX, camY, camW, camH;
    private static long imprintFrame = -1;
    private static StencilBuffer imprintMask;
    private static final IntSeq imprintCopies = new IntSeq(), imprintStamp = new IntSeq(), cachedImprintStamp = new IntSeq();
    private static boolean imprintCached, imprintCacheable, imprintKnown;

    public static void init(){
        initialized = true;
        Core.app.addListener(new ApplicationListener(){
            @Override
            public void resume(){
                imprintFrame = -1;
                imprintKnown = false;
            }
        });
        Events.on(DisposeEvent.class, e -> {
            clear();
            dispose();
        });

        Events.on(ResetEvent.class, e -> {
            clear();
            imprintFrame = -1;
            imprintKnown = false;
        });
        Events.run(Trigger.update, () -> {
            if(state.isPaused()) return;
            frames.removeAll(f -> {
                if(!f.glow && (f.time -= Time.delta) > 0f) return false;
                Pools.free(f);
                return true;
            });
            waves.removeAll(w -> (w.time += Time.delta) >= Math.max(w.lifetime, w.linger));
        });
        Events.run(Trigger.draw, () -> {
            draw();
            drawWaves();
        });
    }

    /** @return 0 for basic effects, 1 for reduced effects, or 2 for full effects. */
    //TODO: should it be based on vanilla setting(s)?
    public static int quality(){
        return headless || !Core.settings.getBool("effects", true) ? 0 : Math.min(Core.settings.getInt(qualitySetting, 2), failed ? 1 : 2);
    }

    public static void configure(){
        clear();
        if(quality() < 2){
            dispose();
            OlShaders.disposeBlasts();
        }
    }

    private static void clear(){
        Pools.freeAll(frames);
        frames.clear();
        waves.clear();
        imprintKnown = false;
    }

    private static void dispose(){
        if(blocks != null) blocks.dispose();
        if(waveBuffer != null) waveBuffer.dispose();
        if(batch != null) batch.dispose();
        if(imprintMask != null) imprintMask.dispose();
        blocks = waveBuffer = null;
        batch = null;
        imprintMask = null;
        blocksFrame = imprintFrame = -1;
    }

    private static boolean ensure(){
        if(!initialized || quality() < 2) return true;
        if(batch != null) return false;
        if(!OlShaders.loadBlasts()){
            failed = true;
            return true;
        }
        try{
            blocks = new FrameBuffer(Core.graphics.getWidth(), Core.graphics.getHeight());
            waveBuffer = new FrameBuffer(Core.graphics.getWidth(), Core.graphics.getHeight());
            imprintMask = stencilBuffer(Core.graphics.getWidth(), Core.graphics.getHeight());
            batch = new ImpactBatch();
            return false;
        }catch(RuntimeException e){
            failed = true;
            dispose();
            OlShaders.disposeBlasts();
            Log.err("Nuclear blast buffers failed to initialize; using reduced effects", e);
            return true;
        }
    }

    /**
     * Adds impact frames.
     * @param intensity shadow length, in world units
     * @param core full-strength radius, in world units
     * @param range faint outer radius, in world units; at least 1.1 times the core
     * @param duration ticks before the strobes
     * @param strobes number of flashes, alternating inverted and normal
     * @param fade fade-out time after the strobes, in ticks
     * @param draw extra shapes, drawn in the light color with their own alpha
     */
    public static void add(float x, float y, float intensity, float core, float range, float duration, int strobes, float fade, Runnable draw){
        Frame f = frame(x, y, intensity, core, range, draw);
        if(f == null) return;
        f.strobes = Core.settings.getBool(strobeSetting, true) ? strobes : 0;
        f.fade = fade;
        f.duration = duration;
        f.time = f.total = duration + f.strobes * strobeTime + fade;
        //force a block silhouette redraw
        blocksFrame = -1;
    }

    /**
     * Shows impact frames for the current update only, without strobes, e.g. building up before a blast.
     * @param strength alpha multiplier, from 0 to 1
     */
    public static void glow(float x, float y, float intensity, float core, float range, float strength, Runnable draw){
        Frame f = frame(x, y, intensity, core, range, draw);
        if(f == null) return;
        f.duration = f.time = 1f;
        f.total = 2f;
        f.strength = strength;
        f.glow = true;
    }

    private static Frame frame(float x, float y, float intensity, float core, float range, Runnable draw){
        if(ensure()) return null;
        Frame f = Pools.obtain(Frame.class, Frame::new);
        f.x = x;
        f.y = y;
        f.intensity = intensity;
        f.core = core;
        f.range = range;
        f.draw = draw;
        frames.add(f);
        return f;
    }

    public static void imprint(int id, Imprint im, float alpha){
        if(ensure()) return;
        imprintPass();
        TextureRegion r = im.region;
        float scale = Draw.scl;
        imprintStamp.add(id, Float.floatToRawIntBits(scale), r.width, r.height);
        imprintStamp.add(Float.floatToRawIntBits(im.rotation), r.texture.getTextureObjectHandle(), r.texture.getMinFilter().ordinal(), r.texture.getMagFilter().ordinal());
        imprintStamp.add(Float.floatToRawIntBits(r.u), Float.floatToRawIntBits(r.v), Float.floatToRawIntBits(r.u2), Float.floatToRawIntBits(r.v2));
        imprintStamp.add(im.copies.length);
        for(int i = 0; i < im.copies.length; i += 4){
            imprintStamp.add(Float.floatToRawIntBits(im.copies[i]), Float.floatToRawIntBits(im.copies[i + 1]), Float.floatToRawIntBits(im.copies[i + 2]), (int)(Mathf.clamp(alpha * im.copies[i + 3]) * 255f) & 0xfe);
        }
        Draw.draw(Draw.z(), () -> {
            if(!imprintCached) drawImprint(im, alpha, scale);
        });
    }

    private static void imprintPass(){
        if(batch == null || imprintFrame == Core.graphics.getFrameId()) return;
        imprintFrame = Core.graphics.getFrameId();
        imprintStamp.clear();
        imprintCacheable = imprintMask != null;
        float z = Draw.z();

        FrameBuffer buffer = renderer.effectBuffer;
        buffer.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
        if(imprintMask != null && (imprintMask.getWidth() != buffer.getWidth() || imprintMask.getHeight() != buffer.getHeight())){
            imprintMask.dispose();
            imprintMask = stencilBuffer(buffer.getWidth(), buffer.getHeight());
            imprintKnown = false;
        }
        Draw.drawRange(Layer.scorch + 1f, 0.1f, () -> {
            imprintCacheable &= !Groups.draw.contains(d -> {
                float layer;
                if(d instanceof EffectState e){
                    layer = e.effect == OlFx.nukeImprint ? Float.NaN : e.effect.layer;
                }else if(d instanceof Bullet b){
                    layer = b.type.layer;
                }else if(d instanceof WorldLabel label){
                    layer = label.z;
                }else{
                    return !(d instanceof Unit || d instanceof Decal || d instanceof Fire || d instanceof Puddle || d instanceof Player || d instanceof WorldLabel || d instanceof LaunchCore);
                }
                return layer >= Layer.scorch + 0.9f && layer <= Layer.scorch + 1.1f;
            });
            imprintCacheable &= !Core.gl.glIsEnabled(Gl.scissorTest) && !Core.gl.glIsEnabled(Gl.stencilTest);
            imprintStamp.add(buffer.getWidth(), buffer.getHeight(), buffer.getTexture().getMinFilter().ordinal(), buffer.getTexture().getMagFilter().ordinal());
            imprintStamp.add(Float.floatToRawIntBits(Core.camera.position.x), Float.floatToRawIntBits(Core.camera.position.y), Float.floatToRawIntBits(Core.camera.width), Float.floatToRawIntBits(Core.camera.height));
            for(int i = 0; i < 9; i++) imprintStamp.add(Float.floatToRawIntBits(Draw.proj().val[i]), Float.floatToRawIntBits(Draw.trans().val[i]));
            imprintCached = imprintCacheable && imprintKnown && imprintStamp.equals(cachedImprintStamp);
            if(!imprintCached) buffer.begin(Color.clear);
        }, () -> {
            FrameBuffer image = imprintMask;
            if(!imprintCached){
                if(imprintCacheable){
                    imprintMask.getTexture().setFilter(buffer.getTexture().getMinFilter(), buffer.getTexture().getMagFilter());
                    imprintMask.begin();
                    Gl.disable(Gl.blend);
                    buffer.blit(Shaders.screenspace);
                    Gl.enable(Gl.blend);
                    imprintMask.end();
                    cachedImprintStamp.clear();
                    cachedImprintStamp.addAll(imprintStamp);
                    imprintKnown = true;
                }else{
                    image = buffer;
                }
                buffer.end();
            }
            Draw.color();
            Draw.rect(Draw.wrap(image.getTexture()), Core.camera.position.x, Core.camera.position.y, Core.camera.width, -Core.camera.height);
        });
        Draw.z(z);
    }

    private static void drawImprint(Imprint im, float alpha, float scale){
        float w = im.region.width * scale, h = im.region.height * scale;
        float[] c = im.copies;
        Rect viewBounds = Core.camera.bounds(Tmp.r1);
        float cos = Math.abs(Mathf.cosDeg(im.rotation)), sin = Math.abs(Mathf.sinDeg(im.rotation));
        float halfW = (w * cos + h * sin) / 2f, halfH = (w * sin + h * cos) / 2f;
        float pad = Core.camera.width / Core.graphics.getWidth(), work = 0f;
        imprintCopies.clear();
        for(int i = 0; i < c.length; i += 4){
            if(((int)(Mathf.clamp(alpha * c[i + 3]) * 255f) & 0xfe) == 0) continue;
            float scl = Math.abs(c[i + 2]), hw = halfW * scl + pad, hh = halfH * scl + pad;
            float left = Math.max(viewBounds.x, c[i] - hw), bottom = Math.max(viewBounds.y, c[i + 1] - hh);
            float right = Math.min(viewBounds.x + viewBounds.width, c[i] + hw), top = Math.min(viewBounds.y + viewBounds.height, c[i + 1] + hh);
            if(right <= left || top <= bottom) continue;
            imprintCopies.add(i);
            work += (right - left) * (top - bottom);
        }
        if(imprintCopies.isEmpty()){
            Draw.blend();
            return;
        }
        boolean stencil = work > viewBounds.area() * 32f && BlastRenderer.canStencilImprints();
        if(stencil) beginImprintStencil();

        Draw.color(Color.black);
        Draw.blend(BlastRenderer.imprintBlending);
        for(int n = 0; n < imprintCopies.size; n++){
            int i = imprintCopies.get(n);
            Draw.alpha(alpha * c[i + 3]);
            Draw.rect(im.region, c[i], c[i + 1], w * c[i + 2], h * c[i + 2], im.rotation);
        }
        Draw.flush();
        if(stencil) endImprintStencil();
        Draw.blend();
    }

    public static boolean canStencilImprints(){
        return imprintMask != null && imprintMask.stencil() != 0 && !Core.gl.glIsEnabled(Gl.stencilTest);
    }

    private static StencilBuffer stencilBuffer(int width, int height){
        try{
            return new StencilBuffer(width, height, true);
        }catch(ArcRuntimeException ignored){
            return new StencilBuffer(width, height, false);
        }
    }

    public static void beginImprintStencil(){
        imprintMask.begin();
        Draw.beginStencil();
        Gl.clearStencil(0);
        Gl.clear(Gl.stencilBufferBit);
        renderer.effectBuffer.blit(OlShaders.imprintCut);
        imprintMask.end();
        Gl.framebufferRenderbuffer(Gl.framebuffer, Gl.stencilAttachment, Gl.renderbuffer, imprintMask.stencil());
        Draw.beginStenciled();
    }

    public static void endImprintStencil(){
        Draw.endStencil();
        Gl.framebufferRenderbuffer(Gl.framebuffer, Gl.stencilAttachment, Gl.renderbuffer, 0);
    }

    private static class StencilBuffer extends FrameBuffer{
        StencilBuffer(int width, int height, boolean stencil){
            super(Pixmap.Format.rgba8888, width, height, false, stencil);
        }

        int stencil(){
            return getStencilBufferHandle() != 0 ? getStencilBufferHandle() : getDepthStencilPackedBuffer();
        }
    }

    /** @return whether an opaque impact core covers the current viewport. */
    public static boolean obscured(){
        if(quality() < 2) return false;
        for(int i = 0, n = Math.min(frames.size, maxAreas); i < n; i++){
            Frame f = frames.get(i);
            if(f.alpha() == 1f && covers(f.x, f.y, Math.min(f.core * 0.8f, f.range * 0.9f))) return true;
        }
        return false;
    }

    private static boolean covers(float x, float y, float radius){
        float dx = Math.abs(x - Core.camera.position.x) + Core.camera.width / 2f;
        float dy = Math.abs(y - Core.camera.position.y) + Core.camera.height / 2f;
        return radius > 0f && dx * dx + dy * dy < radius * radius;
    }

    static void draw(){
        if(frames.isEmpty() || quality() < 2) return;
        area.setCentered(frames.first().x, frames.first().y, 0f);
        for(int i = 0, n = Math.min(frames.size, maxAreas); i < n; i++){
            Frame f = frames.get(i);
            area.merge(Tmp.r2.setCentered(f.x, f.y, (Math.max(f.range, f.core * 1.1f) + 8f) * 2f));
        }
        if(!Core.camera.bounds(Tmp.r1).overlaps(area)) return;

        FrameBuffer buffer = renderer.effectBuffer, composite = waveBuffer;
        buffer.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
        composite.resize(Core.graphics.getWidth(), Core.graphics.getHeight());

        if(blocks.resizeCheck(Core.graphics.getWidth(), Core.graphics.getHeight())) blocksFrame = -1;

        Draw.draw(Layer.end - 1f, () -> {
            float alpha = 0f, warm = 0f, weight = 0f;
            boolean invert = false;
            for(Frame f : frames){
                float strength = f.alpha();
                alpha = Math.max(alpha, strength);
                if(strength > 0f) warm = Mathf.lerp(warm, 1f - f.fadeOut(), strength / (weight += strength));
                invert |= f.inverted();
            }
            if(alpha <= 0f) return;
            float opacity = alpha;
            Color dark = invert ? Color.white : Color.black, light = invert ? Color.black : Color.white;

            Mat proj = Draw.proj(), trans = Draw.trans();
            Batch previous = Core.batch;
            boolean lighting = state.rules.lighting, shields = renderer.animateShields;
            boolean clipped = Core.gl.glIsEnabled(Gl.scissorTest);
            if(clipped) Gl.getIntegerv(Gl.scissorBox, savedScissor);
            Draw.batch(batch);
            try{
                Draw.proj(proj);
                Draw.trans(trans);

                boolean solidBackdrop = frames.contains(f -> covers(f.x, f.y, f.range * 0.8f));
                composite.begin(solidBackdrop ? dark : Color.clear);
                try{

                    Draw.reset();
                    Draw.color(dark);
                    if(!solidBackdrop){
                        for(Frame f : frames) Fill.circle(f.x, f.y, f.range);
                    }

                    Draw.color(light);
                    lights.clear();

                    rand.setSeed((long)Time.time);
                    for(Frame f : frames){
                        Draw.color(light, f.alpha() / opacity);
                        float reach = f.reach();
                        if(lights.size < maxAreas * 3){
                            Vec2 p = Core.camera.project(f.x, f.y);
                            lights.add(p.x, p.y, f.intensity * Mathf.lerp(0.35f, 1f, f.growth()));
                        }

                        int rays = 14;
                        for(int i = 0; i < rays; i++){
                            float angle = i * 360f / rays + rand.range(12f);
                            float range = Math.min(f.core * rand.random(1.3f, 3f), f.range) * reach;
                            float spread = rand.random(3.5f, 8f) * Mathf.lerp(0.6f, 1f, reach);

                            Fill.tri(
                            f.x, f.y,
                            f.x + Angles.trnsx(angle - spread, range), f.y + Angles.trnsy(angle - spread, range),
                            f.x + Angles.trnsx(angle + spread, range), f.y + Angles.trnsy(angle + spread, range)
                            );
                        }
                    }
                    Draw.color(light);

                    state.rules.lighting = false;
                    renderer.animateShields = false;

                    long frame = Core.graphics.getFrameId();
                    float moved = Math.max(Math.abs(camX - Core.camera.position.x), Math.abs(camY - Core.camera.position.y));
                    if(frame - blocksFrame >= blockRefresh || moved > Core.camera.width * 0.1f || camW != Core.camera.width || camH != Core.camera.height){
                        blocksFrame = frame;
                        camX = Core.camera.position.x;
                        camY = Core.camera.position.y;
                        camW = Core.camera.width;
                        camH = Core.camera.height;
                        silhouettes(blocks, renderer.blocks::drawBlocks);
                    }

                    silhouettes(buffer, () -> {
                        Draw.rect(Draw.wrap(blocks.getTexture()), camX, camY, camW, -camH);
                        Groups.unit.draw(Unitc::draw);
                        Disintegration.draw();
                    });
                    ScissorStack.calculateScissors(Core.camera, Tmp.m1.idt(), area, scissor);
                    Gl.enable(Gl.scissorTest);
                    Gl.scissor((int)scissor.x, (int)scissor.y, (int)scissor.width + 1, (int)scissor.height + 1);
                    Shader shadow = OlShaders.harshShadow;
                    shadow.bind();
                    shadow.setUniformf("u_texsize", Core.graphics.getWidth(), Core.graphics.getHeight());
                    shadow.setUniformf("u_invsize", 1f / Core.camera.width, 1f / Core.camera.height);
                    shadow.setUniformi("u_lights_count", lights.size / 3);
                    shadow.setUniform3fv("u_lights", lights.items, 0, lights.size);
                    shadow.setUniformi("u_invert", dark == Color.white ? 1 : 0);
                    buffer.blit(shadow);
                    Gl.disable(Gl.scissorTest);

                    state.rules.lighting = lighting;
                    renderer.animateShields = shields;

                    Draw.reset();
                    Draw.color(light);
                    for(Frame f : frames){
                        Draw.color(light, f.alpha() / opacity);
                        if(f.draw != null) f.draw.run();
                    }
                    Draw.reset();

                }finally{
                    composite.end();
                }
            }finally{
                batch.locked = false;
                state.rules.lighting = lighting;
                renderer.animateShields = shields;
                try{
                    Draw.flush();
                }finally{
                    Core.batch = previous;
                    if(clipped){
                        Gl.enable(Gl.scissorTest);
                        Gl.scissor(savedScissor.get(0), savedScissor.get(1), savedScissor.get(2), savedScissor.get(3));
                    }else{
                        Gl.disable(Gl.scissorTest);
                    }
                }
            }

            areas.clear();
            float scl = Core.graphics.getWidth() / Core.camera.width;
            for(int i = 0, n = Math.min(frames.size, maxAreas); i < n; i++){
                Frame f = frames.get(i);
                Vec2 p = Core.camera.project(f.x, f.y);
                strengths[i] = f.alpha() / opacity;
                areas.add(p.x, p.y, f.core * scl, f.range * scl);
            }

            if(ScissorStack.pushWorld(scissor.set(area))){
                try{
                    Shader mask = OlShaders.impactMask;
                    mask.bind();
                    mask.setUniformi("u_areas_count", areas.size / 4);
                    mask.setUniform4fv("u_areas", areas.items, 0, areas.size);
                    mask.setUniform4fv("u_strengths", strengths, 0, strengths.length);
                    Draw.shader(mask);
                    Draw.color(Tmp.c1.set(Color.white).lerp(Pal.lightOrange, Mathf.clamp(warm * 2f)).lerp(Color.scarlet, Mathf.clamp(warm * 2f - 1f)), alpha);
                    Draw.rect(Draw.wrap(composite.getTexture()), Core.camera.position.x, Core.camera.position.y, Core.camera.width, -Core.camera.height);
                    Draw.color();
                    Draw.shader();
                }finally{
                    Draw.color();
                    Draw.shader();
                    ScissorStack.pop();
                }
            }
        });
    }

    static void silhouettes(FrameBuffer target, Runnable drawer){
        target.begin(Color.clear);
        Draw.shader(OlShaders.alphaCut);
        batch.locked = true;

        try{
            drawer.run();
        }finally{
            batch.locked = false;
            Draw.shader();
            target.end();
        }
    }

    static class Frame implements Poolable{
        float x, y, intensity, core, range, time, total, duration, fade, strength = 1f;
        int strobes;
        /** If true, this frame is removed on the next update unless added again. */
        boolean glow;
        Runnable draw;

        @Override
        public void reset(){
            strength = 1f;
            fade = 0f;
            strobes = 0;
            glow = false;
            draw = null;
        }

        boolean inverted(){
            float strobing = strobes * strobeTime, left = time - fade;
            return left > 0f && left <= strobing && (int)((strobing - left) / strobeTime) % 2 == 0;
        }

        float fadeOut(){
            return time > fade ? 1f : Interp.pow2Out.apply(time / fade);
        }

        float alpha(){
            return (time > fade ? 1f : fadeDrop * fadeOut()) * strength;
        }

        float growth(){
            return Interp.pow3Out.apply(Mathf.clamp((total - time) / duration));
        }

        float reach(){
            return Mathf.lerp(0.25f, 1f, growth()) * fadeOut();
        }
    }

    static class ImpactBatch extends SpriteBatch{
        boolean locked;

        @Override
        protected void setShader(Shader shader, boolean apply){
            if(!locked) super.setShader(shader, apply);
        }
    }

    /**
     * Adds a shockwave.
     * @param radius max radius, in world units
     * @param strength displacement at the front, in world units
     * @param tint whitening, from 0 to 1
     * @param dust density of the dust and haze left behind, from 0 to 1
     * @param linger total time the wave and what it leaves behind last, in ticks
     * @return the wave, or null on a server
     */
    public static @Nullable Wave wave(float x, float y, float radius, float strength, float tint, float dust, float lifetime, float linger){
        if(ensure()) return null;

        Wave w = new Wave();
        w.x = x;
        w.y = y;
        w.radius = radius;
        w.strength = strength;
        w.tint = tint;
        w.dust = dust;
        w.linger = linger;
        w.lifetime = lifetime;
        waves.add(w);
        return w;
    }

    static void drawWaves(){
        if(waves.isEmpty() || quality() < 2 || obscured()) return;

        shapes.clear();
        states.clear();
        boolean shared = waves.size > 1;
        waveArea.setCentered(waves.first().x, waves.first().y, 0f);
        for(int i = 0, n = Math.min(waves.size, maxWaves); i < n; i++){
            Wave w = waves.get(i);
            if(w.radius != waves.first().radius) shared = false;
            shapes.add(w.x, w.y, w.radius, w.lifetime);
            states.add(w.time, w.strength, w.tint, w.dust * (1f - Mathf.curve(w.time, w.linger - settleTime, w.linger)));
            waveArea.merge(Tmp.r2.setCentered(w.x, w.y, (w.reach() + 8f) * 2f));
        }
        if(!Core.camera.bounds(Tmp.r1).overlaps(waveArea)) return;
        Shader shader = shared ? OlShaders.sharedShockwave : OlShaders.shockwave;
        waveBuffer.resize(Core.graphics.getWidth(), Core.graphics.getHeight());

        Draw.draw(Layer.floor - 1f, () -> waveBuffer.begin(Color.clear));
        Draw.draw(Layer.flyingUnit + 1f, () -> {
            waveBuffer.end();

            waveBuffer.blit(Shaders.screenspace);

            if(ScissorStack.pushWorld(waveArea)){
                shader.bind();
                shader.setUniformf("u_view", Core.camera.position.x - Core.camera.width / 2f, Core.camera.position.y - Core.camera.height / 2f, Core.camera.width, Core.camera.height);
                shader.setUniformi("u_waves_count", shapes.size / 4);
                shader.setUniform4fv("u_waves", shapes.items, 0, shapes.size);
                shader.setUniform4fv("u_states", states.items, 0, states.size);
                shader.setUniformf("u_time", Time.time);
                waveBuffer.blit(shader);
                ScissorStack.pop();
            }
        });
    }

    public static class Wave{
        float x, y, radius, strength, tint, dust, time, lifetime, linger;

        float front(){
            return radius * Interp.pow3Out.apply(Mathf.clamp(time / lifetime));
        }

        public float arrival(float dst){
            return lifetime * (1f - (float)Math.cbrt(1f - Math.min(dst / radius, 1f)));
        }

        float reach(){
            if(time >= lifetime) return radius;
            float front = front();
            return front + radius * 0.02f + front * 0.08f;
        }
    }
}