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
import mindustry.world.*;
import omaloon.content.*;

import static mindustry.Vars.*;

/** Impact frames, shockwaves and burnt imprints of nuclear blasts. Silhouettes ported from Flame Out by EyeOfDarkness. */
public class BlastRenderer{
    public static final String strobeSetting = "omaloon-blast-strobes";
    /** Frames between block silhouette redraws, unless the camera moves or zooms. */
    public static int blockRefresh = 4;

    private static final int maxAreas = 16, maxWaves = 8, pageSize = 2048, maxBake = 512, maxPages = 4;
    /** Strobe flash duration, in ticks. */
    private static final float strobeTime = 3f;
    /** Alpha the frames drop to when the fade begins. */
    private static final float fadeDrop = 0.55f;
    private static final float settleTime = 120f;
    static final Blending additive = new Blending(Gl.one, Gl.one);

    private static final FloatSeq lights = new FloatSeq(), areas = new FloatSeq(), shapes = new FloatSeq(), states = new FloatSeq();
    private static final float[] strengths = new float[maxAreas];
    private static final Seq<Frame> frames = new Seq<>();
    private static final Seq<Frame> visibleFrames = new Seq<>();
    private static final Seq<Wave> waves = new Seq<>();
    private static final Seq<Page> pages = new Seq<>();
    private static final Seq<Imprint> baking = new Seq<>();
    private static final Rand rand = new Rand();
    private static final Rect area = new Rect(), waveArea = new Rect(), scissor = new Rect();
    private static final Mat bakeProj = new Mat(), lastProj = new Mat();
    private static final TextureRegion silhouetteRegion = new TextureRegion();

    private static FrameBuffer blocks, waveBuffer, silhouette;
    private static ImpactBatch batch;
    private static boolean initialized, failed, obscured;
    private static int lastQuality;
    private static long blocksFrame = -1, obscuredFrame = -1;
    //camera state when the block silhouettes were drawn
    private static float camX, camY, camW, camH;

    public static void init(){
        initialized = true;
        lastQuality = quality();
        Events.on(DisposeEvent.class, e -> {
            clear();
            dispose();
        });
        Events.on(ResetEvent.class, e -> clear());
        Events.run(Trigger.update, () -> {
            int quality = quality();
            if(quality != lastQuality){
                lastQuality = quality;
                configure();
            }
            if(state.isPaused()) return;
            frames.removeAll(f -> {
                if(!f.glow && (f.time -= Time.delta) > 0f) return false;
                Pools.free(f);
                return true;
            });
            waves.removeAll(w -> (w.time += Time.delta) >= Math.max(w.lifetime, w.linger));
        });
        Events.run(Trigger.draw, () -> {
            if(!baking.isEmpty()) Draw.draw(Layer.min, BlastRenderer::bake);
            draw();
            drawWaves();
        });
    }

    /** @return 0 for basic effects when vanilla effects are off, 1 for reduced effects when animated surfaces are off, or 2 for full effects. */
    public static int quality(){
        return headless || !renderer.enableEffects ? 0 : renderer.animateWater && !failed ? 2 : 1;
    }

    public static void configure(){
        clear();
        if(quality() < 2){
            dispose();
            OlShaders.disposeBlasts();
        }
    }

    private static void clear(){
        OlDraw.clearEffects();
        Pools.freeAll(frames);
        frames.clear();
        visibleFrames.clear();
        waves.clear();
        baking.clear();
        pages.each(p -> p.until = 0f);
    }

    private static void dispose(){
        if(blocks != null) blocks.dispose();
        if(waveBuffer != null) waveBuffer.dispose();
        if(silhouette != null) silhouette.dispose();
        if(batch != null) batch.dispose();
        pages.each(p -> p.buffer.dispose());
        pages.clear();
        blocks = waveBuffer = silhouette = null;
        batch = null;
        blocksFrame = -1;
    }

    /** @return whether the full effects can't be used, creating their buffers on first use. */
    private static boolean unavailable(){
        if(!initialized || quality() < 2) return true;
        if(batch != null) return false;
        if(!OlShaders.loadBlasts()){
            failed = true;
            return true;
        }
        try{
            blocks = new FrameBuffer(Core.graphics.getWidth(), Core.graphics.getHeight());
            waveBuffer = new FrameBuffer(Core.graphics.getWidth(), Core.graphics.getHeight());
            silhouette = new FrameBuffer(maxBake, maxBake);
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

    private static @Nullable Frame frame(float x, float y, float intensity, float core, float range, Runnable draw){
        if(unavailable()) return null;
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

    /** @return whether an opaque impact core covers the current viewport. */
    public static boolean obscured(){
        long frame = Core.graphics.getFrameId();
        if(obscuredFrame == frame) return obscured;
        obscuredFrame = frame;
        obscured = false;
        if(quality() < 2) return false;
        for(int i = 0, n = Math.min(frames.size, maxAreas); i < n; i++){
            Frame f = frames.get(i);
            if(f.alpha() == 1f && covers(f.x, f.y, Math.min(f.core * 0.8f, f.range * 0.9f))) return obscured = true;
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
        visibleFrames.clear();
        for(int i = 0; i < frames.size && visibleFrames.size < maxAreas; i++){
            Frame f = frames.get(i);
            if(f.alpha() <= 0f || !Core.camera.bounds(Tmp.r1).overlaps(Tmp.r2.setCentered(f.x, f.y, f.range * 2f))) continue;
            visibleFrames.add(f);
        }
        if(visibleFrames.isEmpty()) return;
        area.setCentered(visibleFrames.first().x, visibleFrames.first().y, 0f);
        for(Frame f : visibleFrames){
            area.merge(Tmp.r2.setCentered(f.x, f.y, (Math.max(f.range, f.core * 1.1f) + 8f) * 2f));
        }
        if(!Core.camera.bounds(Tmp.r1).overlaps(area)) return;

        FrameBuffer buffer = renderer.effectBuffer;
        buffer.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
        waveBuffer.resize(Core.graphics.getWidth(), Core.graphics.getHeight());
        if(blocks.resizeCheck(Core.graphics.getWidth(), Core.graphics.getHeight())) blocksFrame = -1;

        Draw.draw(Layer.end - 1f, () -> {
            float alpha = 0f, warm = 0f, weight = 0f;
            boolean invert = false;
            for(Frame f : visibleFrames){
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
            Draw.batch(batch);
            Draw.proj(proj);
            Draw.trans(trans);

            boolean solid = visibleFrames.contains(f -> covers(f.x, f.y, f.range * 0.8f));
            waveBuffer.begin(solid ? dark : Color.clear);
            Draw.color(dark);
            if(!solid){
                for(Frame f : visibleFrames) Fill.circle(f.x, f.y, f.range);
            }

            lights.clear();
            rand.setSeed((long)Time.time);
            lightShapes(invert);
            for(Frame f : visibleFrames){
                Draw.color(light, f.alpha() / opacity);
                float reach = f.reach();
                if(lights.size < maxAreas * 3){
                    Vec2 p = Core.camera.project(f.x, f.y);
                    lights.add(p.x, p.y, f.intensity * Mathf.lerp(0.35f, 1f, f.growth()));
                }

                for(int i = 0, rays = 14; i < rays; i++){
                    float angle = i * 360f / rays + rand.range(12f);
                    float range = Math.min(f.core * rand.random(1.3f, 3f), f.range) * reach;
                    float spread = rand.random(3.5f, 8f) * Mathf.lerp(0.6f, 1f, reach);
                    Fill.tri(f.x, f.y,
                    f.x + Angles.trnsx(angle - spread, range), f.y + Angles.trnsy(angle - spread, range),
                    f.x + Angles.trnsx(angle + spread, range), f.y + Angles.trnsy(angle + spread, range));
                }
            }

            Draw.flush();
            Gl.blendEquation(Gl.funcAdd);
            Draw.shader();

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
            state.rules.lighting = lighting;
            renderer.animateShields = shields;

            if(ScissorStack.pushWorld(scissor.set(area))){
                Shader shadow = OlShaders.harshShadow;
                shadow.bind();
                shadow.setUniformf("u_texsize", Core.graphics.getWidth(), Core.graphics.getHeight());
                shadow.setUniformf("u_invsize", 1f / Core.camera.width, 1f / Core.camera.height);
                shadow.setUniformi("u_lights_count", lights.size / 3);
                shadow.setUniform3fv("u_lights", lights.items, 0, lights.size);
                shadow.setUniformi("u_invert", invert ? 1 : 0);
                buffer.blit(shadow);
                ScissorStack.pop();
            }

            Draw.reset();
            lightShapes(invert);
            for(Frame f : visibleFrames){
                Draw.color(light, f.alpha() / opacity);
                if(f.draw != null) f.draw.run();
            }
            Draw.flush();
            Gl.blendEquation(Gl.funcAdd);
            Draw.shader();
            Draw.reset();
            waveBuffer.end();
            Draw.batch(previous);

            areas.clear();
            float scl = Core.graphics.getWidth() / Core.camera.width;
            for(int i = 0; i < visibleFrames.size; i++){
                Frame f = visibleFrames.get(i);
                Vec2 p = Core.camera.project(f.x, f.y);
                strengths[i] = f.alpha() / opacity;
                areas.add(p.x, p.y, f.core * scl, f.range * scl);
            }

            if(ScissorStack.pushWorld(scissor.set(area))){
                Shader mask = OlShaders.impactMask;
                mask.bind();
                mask.setUniformi("u_areas_count", areas.size / 4);
                mask.setUniform4fv("u_areas", areas.items, 0, areas.size);
                mask.setUniform4fv("u_strengths", strengths, 0, strengths.length);
                Draw.shader(mask);
                Draw.color(Tmp.c1.set(Color.white).lerp(Pal.lightOrange, Mathf.clamp(warm * 2f)).lerp(Color.scarlet, Mathf.clamp(warm * 2f - 1f)), alpha);
                Draw.rect(Draw.wrap(waveBuffer.getTexture()), Core.camera.position.x, Core.camera.position.y, Core.camera.width, -Core.camera.height);
                Draw.color();
                Draw.shader();
                ScissorStack.pop();
            }
        });
    }

    private static void lightShapes(boolean invert){
        if(Core.graphics.getGLVersion().type != GLVersion.GlType.OpenGL && Core.gl30 == null && !Core.graphics.supportsExtension("GL_EXT_blend_minmax")) return;
        Draw.flush();
        Gl.blendEquation(invert ? Gl.min : Gl.max);
        Draw.shader(OlShaders.impactLight);
        OlShaders.impactLight.bind();
        OlShaders.impactLight.setUniformi("u_invert", invert ? 1 : 0);
    }

    static void silhouettes(FrameBuffer target, Runnable drawer){
        target.begin(Color.clear);
        Draw.shader(OlShaders.alphaCut);
        batch.locked = true;
        drawer.run();
        batch.locked = false;
        Draw.shader();
        target.end();
    }

    /**
     * Adds a shockwave.
     * @param radius max radius, in world units
     * @param strength displacement at the front, in world units
     * @param tint whitening, from 0 to 1
     * @param dust density of the dust and haze left behind, from 0 to 1
     * @param linger total time the wave and what it leaves behind last, in ticks
     * @return the wave, or null without full effects
     */
    public static @Nullable Wave wave(float x, float y, float radius, float strength, float tint, float dust, float lifetime, float linger){
        if(unavailable()) return null;
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
        waveArea.setCentered(waves.first().x, waves.first().y, 0f);
        for(int i = 0, n = Math.min(waves.size, maxWaves); i < n; i++){
            Wave w = waves.get(i);
            shapes.add(w.x, w.y, w.radius, w.lifetime);
            states.add(w.time, w.strength, w.tint, w.dust * (1f - Mathf.curve(w.time, w.linger - settleTime, w.linger)));
            waveArea.merge(Tmp.r2.setCentered(w.x, w.y, (w.reach() + 8f) * 2f));
        }
        if(!Core.camera.bounds(Tmp.r1).overlaps(waveArea)) return;
        waveBuffer.resize(Core.graphics.getWidth(), Core.graphics.getHeight());

        Draw.draw(Layer.floor - 1f, () -> waveBuffer.begin(Color.clear));
        Draw.draw(Layer.flyingUnit + 1f, () -> {
            waveBuffer.end();
            Shader shader = OlShaders.shockwave;
            shader.bind();
            shader.setUniformf("u_view", Core.camera.position.x - Core.camera.width / 2f, Core.camera.position.y - Core.camera.height / 2f, Core.camera.width, Core.camera.height);
            shader.setUniformi("u_waves_count", shapes.size / 4);
            shader.setUniform4fv("u_waves", shapes.items, 0, shapes.size);
            shader.setUniform4fv("u_states", states.items, 0, states.size);
            shader.setUniformf("u_time", Time.time);
            waveBuffer.blit(shader);
        });
    }

    /**
     * Bakes a burnt shadow of the sprites from the last {@link Disintegration#capture}: their silhouette, smeared away from the blast.
     * @param length smear length, in world units
     * @return the imprint, or null without full effects or atlas space
     */
    public static @Nullable Imprint imprint(float ox, float oy, float tx, float ty, float length){
        Seq<TextureRegion> regions = Disintegration.regions;
        FloatSeq parts = Disintegration.parts;
        if(regions.isEmpty() || unavailable()) return null;

        float dst = Math.max(Mathf.dst(ox, oy, tx, ty), 8f), angle = Angles.angle(ox, oy, tx, ty);
        float cos = Mathf.cosDeg(angle), sin = Mathf.sinDeg(angle), smin = Float.MAX_VALUE, smax = 0f;
        int steps = Math.max((int)length, 2);
        FloatSeq copies = new FloatSeq();
        for(int i = steps; i >= 0; i--){
            float scl = 1f + length * i / steps / dst;
            Tile tile = world.tileWorld(ox + (tx - ox) * scl, oy + (ty - oy) * scl);
            if(tile == null || !tile.floor().hasSurface()) continue;
            copies.add(scl, 0.02f * (1f - Interp.pow2In.apply(i / (float)steps)));
            smin = Math.min(smin, scl);
            smax = Math.max(smax, scl);
        }
        if(copies.isEmpty()) return null;

        Imprint im = new Imprint();
        im.regions = regions.toArray(TextureRegion.class);
        im.parts = new float[regions.size * 5];
        float x1 = Float.MAX_VALUE, y1 = Float.MAX_VALUE, x2 = -Float.MAX_VALUE, y2 = -Float.MAX_VALUE;
        for(int i = 0; i < regions.size; i++){
            float dx = parts.get(i * 6) - ox, dy = parts.get(i * 6 + 1) - oy, rotation = parts.get(i * 6 + 2) - angle;
            float w = parts.get(i * 6 + 3), h = parts.get(i * 6 + 4), lx = dx * cos + dy * sin, ly = dy * cos - dx * sin;
            float c = Math.abs(Mathf.cosDeg(rotation)), s = Math.abs(Mathf.sinDeg(rotation));
            float hw = (Math.abs(w) * c + Math.abs(h) * s) / 2f, hh = (Math.abs(w) * s + Math.abs(h) * c) / 2f;
            x1 = Math.min(x1, lx - hw);
            y1 = Math.min(y1, ly - hh);
            x2 = Math.max(x2, lx + hw);
            y2 = Math.max(y2, ly + hh);
            float[] p = im.parts;
            p[i * 5] = lx;
            p[i * 5 + 1] = ly;
            p[i * 5 + 2] = rotation;
            p[i * 5 + 3] = w;
            p[i * 5 + 4] = h;
        }

        float span = Math.max(Math.max(x2 * smax, x2 * smin) - Math.min(x1 * smax, x1 * smin), Math.max(y2 * smax, y2 * smin) - Math.min(y1 * smax, y1 * smin));
        float res = im.res = Math.min(1f / Draw.scl, (maxBake - 4) / span), pad = 1f / res;
        im.sx = x1 - pad;
        im.sy = y1 - pad;
        im.sw = Mathf.ceil((x2 - x1) * res) + 2;
        im.sh = Mathf.ceil((y2 - y1) * res) + 2;
        float sx2 = im.sx + im.sw / res, sy2 = im.sy + im.sh / res;
        im.bx = Math.min(im.sx * smin, im.sx * smax) - pad;
        im.by = Math.min(im.sy * smin, im.sy * smax) - pad;
        int width = Mathf.ceil((Math.max(sx2 * smin, sx2 * smax) + pad - im.bx) * res), height = Mathf.ceil((Math.max(sy2 * smin, sy2 * smax) + pad - im.by) * res);
        if(!allocate(im, width, height)) return null;

        float mx = im.bx + width / res / 2f, my = im.by + height / res / 2f;
        im.x = ox + mx * cos - my * sin;
        im.y = oy + mx * sin + my * cos;
        im.width = width / res;
        im.height = height / res;
        im.rotation = angle;
        im.region = new TextureRegion(im.page.buffer.getTexture(), im.px, im.py, width, height);
        im.region.flip(false, true);
        im.copies = copies.toArray();
        baking.add(im);
        return im;
    }

    private static boolean allocate(Imprint im, int width, int height){
        for(Page page : pages){
            if(page.fit(im, width, height)) return true;
        }
        if(pages.size >= maxPages) return false;
        Page page = new Page();
        pages.add(page);
        return page.fit(im, width, height);
    }

    static void bake(){
        lastProj.set(Draw.proj());
        Draw.reset();
        for(Imprint im : baking){
            silhouette.begin(Color.clear);
            Gl.viewport(0, 0, im.sw, im.sh);
            Draw.proj(bakeProj.setOrtho(im.sx, im.sy, im.sw / im.res, im.sh / im.res));
            Draw.color(Color.black);
            for(int i = 0; i < im.regions.length; i++){
                float[] p = im.parts;
                Draw.rect(im.regions[i], p[i * 5], p[i * 5 + 1], p[i * 5 + 3], p[i * 5 + 4], p[i * 5 + 2]);
            }
            silhouette.end();

            Page page = im.page;
            page.buffer.begin();
            if(page.clear){
                Gl.clearColor(0f, 0f, 0f, 0f);
                Gl.clear(Gl.colorBufferBit);
                page.clear = false;
            }
            Draw.proj(bakeProj.setOrtho(0f, 0f, pageSize, pageSize));
            Draw.blend(additive);
            silhouetteRegion.set(silhouette.getTexture());
            silhouetteRegion.set(0, 0, im.sw, im.sh);
            silhouetteRegion.flip(false, true);
            float cx = im.sx + im.sw / im.res / 2f, cy = im.sy + im.sh / im.res / 2f;
            for(int i = 0; i < im.copies.length; i += 2){
                float scl = im.copies[i];
                Draw.color(0f, 0f, 0f, im.copies[i + 1]);
                Draw.rect(silhouetteRegion, im.px + (cx * scl - im.bx) * im.res, im.py + (cy * scl - im.by) * im.res, im.sw * scl, im.sh * scl);
            }
            Draw.flush();
            Draw.blend();
            page.buffer.end();
            im.regions = null;
            im.parts = im.copies = null;
        }
        baking.clear();
        Draw.reset();
        Draw.proj(lastProj);
    }

    /** A burnt shadow baked into an atlas page. */
    public static class Imprint{
        public TextureRegion region;
        public float x, y, width, height, rotation;

        Page page;
        int px, py, sw, sh;
        float res, sx, sy, bx, by;
        TextureRegion[] regions;
        float[] parts, copies;
    }

    /** Atlas of baked imprints, filled row by row and reused once all its imprints have faded. */
    static class Page{
        final FrameBuffer buffer = new FrameBuffer(pageSize, pageSize);
        int x, y, row;
        float until;
        boolean clear = true;

        boolean fit(Imprint im, int width, int height){
            if(Time.time > until){
                x = y = row = 0;
                clear = true;
            }
            if(x + width > pageSize){
                x = 0;
                y += row;
                row = 0;
            }
            if(width > pageSize || y + height > pageSize) return false;
            im.page = this;
            im.px = x;
            im.py = y;
            x += width;
            row = Math.max(row, height);
            until = Math.max(until, Time.time + OlFx.nukeImprint.lifetime + 60f);
            return true;
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

    public static class Wave{
        float x, y, radius, strength, tint, dust, time, lifetime, linger;

        /** @return ticks until a wave front reaches the distance, for a wave with the given radius and lifetime. */
        public static float arrival(float dst, float radius, float lifetime){
            return lifetime * (1f - (float)Math.cbrt(1f - Math.min(dst / radius, 1f)));
        }

        public float arrival(float dst){
            return arrival(dst, radius, lifetime);
        }

        float front(){
            return radius * Interp.pow3Out.apply(Mathf.clamp(time / lifetime));
        }

        float reach(){
            if(time >= lifetime) return radius;
            float front = front();
            return front + radius * 0.02f + front * 0.08f;
        }
    }
}