package omaloon.graphics.shaders;

import arc.*;
import arc.graphics.*;
import arc.graphics.Texture.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import omaloon.graphics.*;
import omaloon.world.blocks.defense.*;
import omaloon.world.blocks.defense.Shelter.*;

import java.nio.*;

import static omaloon.graphics.OlShaders.*;

public class ShelterShader extends Shader{
    private static final float layer = Layer.shields + 2f, rippleRange = 4f, maskRange = 128f;
    /** Desktop GL reports uniforms by component, and Arc only names the vector count. */
    private static final int maxFragmentUniformComponents = 0x8B49;
    private static final int maxFields;

    public static float domeScale = 0.45f, roundness = 6f, mergeRadius = 16f, lagTicks = 8f;
    public static float darknessAbsorb = 0.75f, weatherMinAlpha = 0.1f;

    private static final Seq<ShelterBuild> packed = new Seq<>();
    private static final Rect bounds = new Rect(), maskView = new Rect();
    private static final float[] pos, motion, parts, star;
    private static final float[] lo = new float[3], hi = new float[3], radius = new float[3], edges = new float[5], outline = new float[4];
    private static long maskFrame = -1;

    static{
        int vectors = Core.app.isMobile() ? Gl.getInt(Gl.maxFragmentUniformVectors) : Gl.getInt(maxFragmentUniformComponents) / 4;
        maxFields = Mathf.clamp((vectors - 8) / 7, 4, 16);
        pos = new float[maxFields * 4];
        motion = new float[maxFields * 4];
        parts = new float[maxFields * 12];
        star = new float[maxFields * 8];
    }

    private final Shader maskShader, rings, fade;
    private final FrameBuffer mask = new FrameBuffer(2, 2), ripples = new FrameBuffer(2, 2);
    private final Blending sum = new Blending(Gl.one, Gl.one);
    private final Color color = new Color(), ringData = new Color(), ringCenter = new Color();
    private final IntBuffer viewport = Buffers.newIntBuffer(16);
    private final Runnable draw = this::render;

    private TextureRegion ringRegion;
    private @Nullable FrameBuffer dark;
    private boolean rippling, darkTaken;
    private int scene, sceneW, sceneH;
    private float vx, vy, vw = 1f, vh = 1f, stormX, stormY;

    public ShelterShader(){
        super(file("shelter.vert").readString(), source(null));
        mask.getTexture().setFilter(TextureFilter.nearest);

        maskShader = new Shader(Shaders.getShaderFi("screenspace.vert").readString(), source("MASK")){
            @Override
            public void apply(){
                int n = Math.min(packed.size + 1, maxFields);
                setUniform4fv("u_fieldPos", pos, 0, n * 4);
                setUniform4fv("u_fieldMotion", motion, 0, n * 4);
                setUniform4fv("u_parts", parts, 0, n * 12);
                setUniform4fv("u_star", star, 0, n * 8);
                setUniformf("u_fieldTime", Time.time, mergeRadius);
                setUniformf("u_fieldBounds", bounds.x, bounds.y, bounds.x + bounds.width, bounds.y + bounds.height);
                setUniformf("u_maskView", maskView.x, maskView.y, maskView.width, maskView.height);
            }
        };
        rings = new Shader(file("shelter.vert").readString(), source("RING")){
            @Override
            public void apply(){
                bindMask(this);
            }
        };
        fade = new Shader(file("shelter.vert").readString(), source("FADE")){
            @Override
            public void apply(){
                bindMask(this);
                setUniformf("u_minAlpha", weatherMinAlpha);
            }
        };

        Events.run(Trigger.draw, this::pack);
    }

    private static String source(@Nullable String variant){
        return (variant == null ? "" : "#define " + variant + "\n") + "#define MAX_FIELDS " + maxFields
            + "\n#define RIPPLE_RANGE " + rippleRange + "\n#define MASK_RANGE " + maskRange + "\n" + file("shelter.frag").readString();
    }

    /** @return whether weather drawn until {@link #endFade(boolean)} is faded over fields. */
    public static boolean beginFade(){
        if(OlShaders.shelter == null || OlShaders.shelter.noMask()) return false;
        Draw.shader(OlShaders.shelter.fade);
        return true;
    }

    public static void endFade(boolean faded){
        if(faded) Draw.shader();
    }

    /** Picks the fields in view nearest to the camera and queues them. */
    private void pack(){
        packed.clear();
        Rect view = Core.camera.bounds(Tmp.r1);
        float cx = Core.camera.position.x, cy = Core.camera.position.y;
        for(ShelterBuild b : Shelter.fields){
            float r = b.fieldMaxRadius() + mergeRadius;
            if(r <= mergeRadius + 0.01f || !view.overlaps(b.x - r, b.y - r, r * 2f, r * 2f)) continue;
            if(packed.size < maxFields){
                packed.add(b);
                continue;
            }

            int far = 0;
            for(int i = 1; i < packed.size; i++){
                if(packed.get(i).dst2(cx, cy) > packed.get(far).dst2(cx, cy)) far = i;
            }
            if(b.dst2(cx, cy) < packed.get(far).dst2(cx, cy)) packed.set(far, b);
        }
        if(packed.isEmpty()) return;

        for(int i = 0; i < packed.size; i++){
            ShelterBuild b = packed.get(i);
            b.drawnFrame = Core.graphics.getFrameId();
            packField(b, i);

            float r = b.fieldMaxRadius() + mergeRadius + 4f;
            Rect area = Tmp.r1.set(b.x - r, b.y - r, r * 2f, r * 2f);
            if(i == 0){
                bounds.set(area);
            }else{
                bounds.merge(area);
            }
        }
        if(packed.size < maxFields) pos[packed.size * 4 + 2] = -1f;

        float z = Draw.z();
        Draw.draw(layer, draw);
        Draw.z(z);
    }

    private static void packField(ShelterBuild b, int i){
        float inner = b.fieldInnerRadius(), outer = b.fieldOuterRadius();
        float left = b.fieldLobeRadius(b.currentLRadius), right = b.fieldLobeRadius(b.currentRRadius);
        float half = b.currentArcLength / 2f, ov = Shelter.fieldOverlap / 2f, lobe = Shelter.lobeHalfSpan * 2f, mid = b.currentRotation + half;

        //the sectors of ShelterBuild.covers, relative to the arc's middle
        lo[0] = -half - ov;
        hi[0] = half + ov;
        lo[1] = half - ov;
        hi[1] = half + ov + lobe;
        lo[2] = -half - ov - lobe;
        hi[2] = -half + ov;
        radius[0] = outer > inner ? outer : 0f;
        radius[1] = left > inner ? left : 0f;
        radius[2] = right > inner ? right : 0f;

        for(int j = 0; j < 3; j++){
            float start = lo[j], end = hi[j];

            for(int n = 0; n < 3; n++){
                if(n == j || radius[n] <= 0f || radius[n] < radius[j]) continue;
                float center = (lo[n] + hi[n]) / 2f;
                if(within(hi[j], lo[n], hi[n])){
                    float into = Mathf.mod(center - hi[j], 360f);
                    if(into <= 180f) end = Math.max(end, hi[j] + into);
                }
                if(within(lo[j], lo[n], hi[n])){
                    float into = Mathf.mod(lo[j] - center, 360f);
                    if(into <= 180f) start = Math.min(start, lo[j] - into);
                }
            }

            int p = (i * 3 + j) * 4;
            float angle = mid + (start + end) / 2f;
            parts[p] = Mathf.cosDeg(angle);
            parts[p + 1] = Mathf.sinDeg(angle);
            parts[p + 2] = Math.min((end - start) / 2f, 179f) * Mathf.degRad;
            parts[p + 3] = radius[j];
        }

        float arcR = Math.max(radius[0], inner), leftR = Math.max(radius[1], inner), rightR = Math.max(radius[2], inner);
        float start = lo[2], first = arcR >= rightR ? lo[0] : hi[2], second = Math.max(arcR >= leftR ? hi[0] : lo[1], first), end = hi[1];
        if(end - start > 360f){
            if(leftR > rightR){
                start = end - 360f;
            }else{
                end = start + 360f;
            }
        }
        edges[1] = first - start;
        edges[2] = second - start;
        edges[3] = end - start;
        edges[4] = 360f;
        outline[0] = rightR;
        outline[1] = arcR;
        outline[2] = leftR;
        outline[3] = inner;
        //an empty piece takes its neighbor's radius, or a step-down to it would cut into the field
        for(int k = 0; k < 8; k++){
            if(edges[k % 4 + 1] - edges[k % 4] < 0.01f) outline[k % 4] = outline[(k + 3) % 4];
        }

        int o = i * 8, f = i * 4;
        star[o] = (mid + start) * Mathf.degRad;
        for(int k = 0; k < 4; k++){
            if(k > 0) star[o + k] = edges[k] * Mathf.degRad;
            star[o + 4 + k] = outline[k];
        }

        pos[f] = b.x;
        pos[f + 1] = b.y;
        pos[f + 2] = inner;
        pos[f + 3] = Math.max(roundness * b.visualScale, 0.5f);

        motion[f] = Mathf.clamp(b.spin * lagTicks, -25f, 25f) * Mathf.degRad;
        motion[f + 1] = b.wobble;
        motion[f + 2] = Math.max(b.fieldMaxRadius() * domeScale, 8f);
        motion[f + 3] = b.broken ? 0f : Mathf.clamp(b.shield / ((Shelter)b.block).shieldHealth);
    }

    private static boolean within(float angle, float start, float end){
        float d = Mathf.mod(angle - start, 360f);
        return d <= end - start + 0.5f || d >= 359.5f;
    }

    /** @return whether no field is in view; otherwise draws the mask, once a frame, for whichever reader comes first. */
    private boolean noMask(){
        long frame = Core.graphics.getFrameId();
        if(packed.isEmpty() || packed.first().drawnFrame != frame) return true;
        if(maskFrame == frame) return false;

        maskFrame = frame;
        Core.camera.bounds(maskView);
        mask.resize(Math.max(Core.graphics.getWidth(), 2), Math.max(Core.graphics.getHeight(), 2));
        mask.begin();
        Blending.disabled.apply();
        Draw.blit(maskShader);
        mask.end();
        return false;
    }

    private void bindMask(Shader shader){
        mask.getTexture().bind(5);
        Gl.activeTexture(Gl.texture0);
        shader.setUniformi("u_fieldMask", 5);
        shader.setUniformf("u_maskView", maskView.x, maskView.y, maskView.width, maskView.height);
        shader.setUniformf("u_maskTexel", 1f / mask.getWidth(), 1f / mask.getHeight());
        shader.setUniformf("u_fieldTime", Time.time, mergeRadius);
        shader.setUniformf("u_pixel", Core.camera.width / vw);
    }

    private void render(){
        if(noMask()) return;

        Rect view = Core.camera.bounds(Tmp.r3);
        float x = Math.max(bounds.x, view.x), y = Math.max(bounds.y, view.y);
        float w = Math.min(bounds.x + bounds.width, view.x + view.width) - x, h = Math.min(bounds.y + bounds.height, view.y + view.height) - y;

        color.set(((Shelter)packed.first().block).arcColor);
        Draw.flush();
        copyScene(view, x, y, w, h);
        drawRings();

        if(!darkTaken){
            darkTaken = true;
            try{
                //private in BlockRenderer :(
                dark = Reflect.get(Vars.renderer.blocks, "dark");
            }catch(Throwable e){
                Log.err("Shelter fields cannot read the map's darkness", e);
            }
        }

        stormX = stormY = 0f;
        for(WeatherState weather : Groups.weather){
            float s = weather.intensity * weather.opacity / Math.max(weather.windVector.len(), 0.001f);
            stormX += weather.windVector.x * s;
            stormY += weather.windVector.y * s;
        }

        Draw.shader(this);
        Draw.color();
        Fill.rect(x + w / 2f, y + h / 2f, w, h);
        Draw.shader();
        Draw.reset();
    }

    /** Copies what is drawn under the fields so far, unfiltered so the bent world stays as crisp as the rest. */
    private void copyScene(Rect view, float x, float y, float w, float h){
        Gl.getIntegerv(Gl.viewport, viewport);
        vx = viewport.get(0);
        vy = viewport.get(1);
        vw = Math.max(viewport.get(2), 2);
        vh = Math.max(viewport.get(3), 2);
        if(scene == 0) scene = Gl.genTexture();
        Gl.activeTexture(Gl.texture1);
        Gl.bindTexture(Gl.texture2d, scene);
        if(vw != sceneW || vh != sceneH){
            sceneW = (int)vw;
            sceneH = (int)vh;
            Gl.texParameteri(Gl.texture2d, Gl.textureMinFilter, Gl.nearest);
            Gl.texParameteri(Gl.texture2d, Gl.textureMagFilter, Gl.nearest);
            Gl.texParameteri(Gl.texture2d, Gl.textureWrapS, Gl.clampToEdge);
            Gl.texParameteri(Gl.texture2d, Gl.textureWrapT, Gl.clampToEdge);
            //RGB, as copying needs a format every framebuffer can give, including one without alpha
            Gl.texImage2D(Gl.texture2d, 0, Gl.rgb, sceneW, sceneH, 0, Gl.rgb, Gl.unsignedByte, null);
        }
        //with room for the farthest a ripple or the lens bends a lookup
        float scale = vw / view.width, margin = (rippleRange * 1.5f + 6f) * scale + 2f;
        int cx = Math.max((int)((x - view.x) * scale - margin), 0), cy = Math.max((int)((y - view.y) * scale - margin), 0);
        int cw = Math.min((int)((x + w - view.x) * scale + margin) + 1, sceneW) - cx, ch = Math.min((int)((y + h - view.y) * scale + margin) + 1, sceneH) - cy;
        if(cw > 0 && ch > 0) Gl.copyTexSubImage2D(Gl.texture2d, 0, cx, cy, (int)vx + cx, (int)vy + cy, cw, ch);
        Gl.activeTexture(Gl.texture0);
    }

    /** Draws one sprite per impact into the ripple buffer, which is cleared once after the last ring fades and then left alone. */
    private void drawRings(){
        boolean any = false;
        for(ShelterBuild b : packed){
            b.pruneHits();
            any |= b.hits.size > 0;
        }
        if(ripples.getWidth() != sceneW || ripples.getHeight() != sceneH){
            ripples.resize(sceneW, sceneH);
            rippling = true;
        }
        if(!any && !rippling) return;
        rippling = any;

        if(ringRegion == null) ringRegion = new TextureRegion(Core.atlas.white().texture);
        ripples.begin(Color.clear);
        Draw.shader(rings);
        Draw.blend(sum);
        for(ShelterBuild b : packed){
            float[] hits = b.hits.items;
            for(int i = 0; i < b.hits.size; i += ShelterBuild.hitStride){
                float age = (Time.time - hits[i + 2]) / hits[i + 5], reach = hits[i + 4], size = (reach + 4f) * 2f;
                float tx = b.x - hits[i], ty = b.y - hits[i + 1], to = Math.max(Mathf.len(tx, ty), 0.001f);
                //the ring's parameters ride in its color, and the way to the shelter's center in its mix color
                Draw.color(ringData.set(age, Math.min(hits[i + 3] / 3f, 1f), Math.min(reach / 128f, 1f), 1f));
                Draw.mixcol(ringCenter.set(0.5f + 0.5f * tx / to, 0.5f + 0.5f * ty / to, Math.min(to / 255f, 1f), 1f), 0f);
                Draw.rect(ringRegion, hits[i], hits[i + 1], size, size);
            }
        }
        Draw.shader();
        Draw.blend();
        Draw.mixcol();
        ripples.end();
    }

    @Override
    public void apply(){
        bindMask(this);

        Gl.activeTexture(Gl.texture1);
        Gl.bindTexture(Gl.texture2d, scene);
        ripples.getTexture().bind(2);
        if(dark != null) dark.getTexture().bind(3);
        Gl.activeTexture(Gl.texture0);
        setUniformi("u_screen", 1);
        setUniformi("u_ripples", 2);
        setUniformi("u_dark", 3);
        setUniformf("u_darkSize", Vars.world.width() * Vars.tilesize, Vars.world.height() * Vars.tilesize);
        setUniformf("u_darkAbsorb", dark != null && Vars.enableDarkness ? darknessAbsorb : 0f);
        setUniformf("u_viewport", vx, vy, vw, vh);
        setUniformf("u_color", color);
        float storm = Math.max(Mathf.len(stormX, stormY), 0.001f);
        setUniformf("u_storm", stormX / storm, stormY / storm, Math.min(storm, 1f) * Vars.renderer.weatherAlpha);
        setUniformf("u_intensity", 1f + (Mathf.clamp(Core.settings.getInt("omaloon-shelter-intensity", 5), 0, 100) - 5) / 95f);
    }

    @Override
    public void dispose(){
        super.dispose();
        maskShader.dispose();
        rings.dispose();
        fade.dispose();
        mask.dispose();
        ripples.dispose();
        if(scene != 0) Gl.deleteTexture(scene);
    }
}
