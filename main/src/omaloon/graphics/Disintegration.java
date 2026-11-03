package omaloon.graphics;

import arc.*;
import arc.func.*;
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
import mindustry.graphics.*;
import omaloon.content.*;
import omaloon.graphics.BlastRenderer.*;
import omaloon.world.NuclearBlasts.*;

import static mindustry.Vars.*;

/** Sprites broken into jittered quads that fly apart and burn to ash. Ported from Flame Out by EyeOfDarkness. */
public class Disintegration{
    /** Sprites drawn during the last {@link #capture}, with x, y, rotation, width, height and z in {@link #parts}. */
    public static final Seq<TextureRegion> regions = new Seq<>();
    public static final FloatSeq parts = new FloatSeq();

    private static final int maxDimension = 64;
    private static final Seq<Piece> pieces = new Seq<>();
    private static final int[] corners = {0, 0, 1, 0, 1, 1, 0, 1};
    private static final float[] verts = new float[4 * 6], xs = new float[maxDimension * maxDimension], ys = new float[maxDimension * maxDimension];
    private static final Rect view = new Rect();
    private static final Color ashColor = Color.gray, heatColor = Pal.lighterOrange, cooledColor = Color.valueOf("8f1c0c");
    private static CaptureBatch captureBatch;

    public static void init(){
        Events.on(ResetEvent.class, e -> {
            Pools.freeAll(pieces);
            pieces.clear();
        });
        Events.run(Trigger.update, () -> {
            if(state.isPaused()) return;
            int alive = 0;
            for(int i = 0; i < pieces.size; i++){
                Piece p = pieces.get(i);
                if(!p.update()) pieces.set(alive++, p);
            }
            pieces.truncate(alive);
        });
        Events.run(Trigger.draw, () -> {
            if(!BlastRenderer.obscured()) draw();
        });
    }

    public static void draw(){
        Core.camera.bounds(view);
        for(Piece p : pieces){
            float bound = (Math.abs(p.width) + Math.abs(p.height)) * 0.5f;
            if(view.overlaps(p.x - bound, p.y - bound, bound * 2f, bound * 2f)) p.draw();
        }
    }

    /** Records the sprites the drawer draws into {@link #regions} and {@link #parts} instead of rendering them. */
    public static void capture(Runnable drawer){
        if(captureBatch == null) captureBatch = new CaptureBatch();
        regions.clear();
        parts.clear();

        Batch last = Core.batch;
        boolean legacy = Lines.useLegacyLine;
        Lines.useLegacyLine = true;
        try{
            Draw.batch(captureBatch, drawer);
        }finally{
            Lines.useLegacyLine = legacy;
            Core.batch = last;
        }
    }

    /** Breaks the sprites from the last {@link #capture} into pieces. */
    public static void shatter(Cons<Piece> cons){
        float[] p = parts.items;
        for(int i = 0; i < regions.size; i++){
            generate(regions.get(i), p[i * 6], p[i * 6 + 1], p[i * 6 + 2], p[i * 6 + 3], p[i * 6 + 4], p[i * 6 + 5], cons);
        }
    }

    static void generate(TextureRegion region, float x, float y, float rotation, float width, float height, float z, Cons<Piece> cons){
        int limit = BlastRenderer.quality() == 0 ? 0 : BlastRenderer.quality() == 1 ? 1024 : 8192;
        if(pieces.size >= limit) return;
        int columns = Mathf.clamp((int)(5f / 320f * Math.abs(width / Draw.scl)), 4, maxDimension), rows = Mathf.clamp((int)(5f / 320f * Math.abs(height / Draw.scl)), 4, maxDimension);
        float cos = Mathf.cosDeg(rotation), sin = Mathf.sinDeg(rotation);

        for(int ix = 0; ix < columns; ix++){
            for(int iy = 0; iy < rows; iy++){
                float fx = ix / (columns - 1f), fy = iy / (rows - 1f);
                if(ix > 0 && ix < columns - 1) fx += OlFx.rand.random(0.1f, 0.4f) * Mathf.sign(OlFx.rand.nextBoolean()) / (columns - 1f);
                if(iy > 0 && iy < rows - 1) fy += OlFx.rand.random(0.1f, 0.4f) * Mathf.sign(OlFx.rand.nextBoolean()) / (rows - 1f);
                xs[ix + iy * columns] = fx;
                ys[ix + iy * columns] = fy;
            }
        }

        for(int ix = 0; ix < columns - 1; ix++){
            for(int iy = 0; iy < rows - 1; iy++){
                if(pieces.size >= limit) return;
                Piece p = Pools.obtain(Piece.class, Piece::new);
                float ox = 0f, oy = 0f;
                for(int i = 0; i < 8; i += 2){
                    int pos = ix + corners[i] + (iy + corners[i + 1]) * columns;
                    ox += p.corners[i] = xs[pos];
                    oy += p.corners[i + 1] = ys[pos];
                }
                float rx = (ox / 4f - 0.5f) * width, ry = (oy / 4f - 0.5f) * height;

                p.region = region;
                p.x = p.startX = rx * cos - ry * sin + x;
                p.y = p.startY = rx * sin + ry * cos + y;
                p.z = z;
                p.rotation = rotation;
                p.width = width;
                p.height = height;
                p.offsetX = ox / 4f;
                p.offsetY = oy / 4f;
                cons.get(p);
                pieces.add(p);
            }
        }
    }

    public static class Piece implements Poolable{
        final float[] corners = new float[8];
        TextureRegion region;
        float rotation, width, height, offsetX, offsetY, startX, startY;

        public float x, y, z, vx, vy, vr, drag, time, lifetime;
        public float fadeDistance;
        /** Blast whose shockwave carries this piece once the front passes. */
        public @Nullable Blast blast;
        public float windCarry;

        /** @return dissolve progress by travel distance, from 0 to 1. */
        float dissolve(){
            return Mathf.curve(Mathf.dst(startX, startY, x, y), fadeDistance * 0.25f, fadeDistance);
        }

        /** @return whether this piece expired and was freed. */
        boolean update(){
            Wave wave = blast == null ? null : blast.wave;
            if(wave != null && wave.time < wave.lifetime){
                float dx = x - wave.x, dy = y - wave.y, dst = Mathf.len(dx, dy);
                if(dst > 0.01f && dst <= wave.front()){
                    float inv = 1f - wave.time / wave.lifetime, speed = 3f * wave.radius * inv * inv / wave.lifetime * windCarry;
                    vx = Mathf.lerpDelta(vx, dx / dst * speed, 0.25f);
                    vy = Mathf.lerpDelta(vy, dy / dst * speed, 0.25f);
                }
            }

            x += vx * Time.delta;
            y += vy * Time.delta;
            rotation += vr * Time.delta;
            float dt = 1f - drag * Time.delta;
            vx *= dt;
            vy *= dt;

            if((time += Time.delta) < lifetime && dissolve() < 1f) return false;
            Pools.free(this);
            return true;
        }

        void draw(){
            float cos = Mathf.cosDeg(rotation), sin = Mathf.sinDeg(rotation);
            float scl = Interp.pow5Out.apply(1f - Mathf.clamp(time / lifetime)), cool = Mathf.curve(time / lifetime, 0f, 0.4f);
            Tmp.c1.set(Color.black).lerp(ashColor, Mathf.curve(time / lifetime, 0.3f, 0.8f));
            Tmp.c1.a *= 1f - Interp.pow2In.apply(dissolve());
            float color = Tmp.c1.toFloatBits();
            float mix = Tmp.c2.set(heatColor).lerp(cooledColor, cool).a(1f - Interp.pow2In.apply(cool)).toFloatBits();

            for(int i = 0; i < 8; i += 2){
                float cx = corners[i], cy = corners[i + 1], ox = (cx - offsetX) * width * scl, oy = (cy - offsetY) * height * scl;
                int v = i * 3;
                verts[v] = ox * cos - oy * sin + x;
                verts[v + 1] = ox * sin + oy * cos + y;
                verts[v + 2] = color;
                verts[v + 3] = Mathf.lerp(region.u, region.u2, cx);
                verts[v + 4] = Mathf.lerp(region.v2, region.v, cy);
                verts[v + 5] = mix;
            }

            Draw.z(z);
            Draw.vert(region.texture, verts, 0, verts.length);
        }

        @Override
        public void reset(){
            region = null;
            blast = null;
            time = 0f;
        }
    }

    static class CaptureBatch extends Batch{
        final TextureRegion circle = Core.atlas.find("circle");

        @Override
        protected void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float rotation){
            if((Color.floatToIntColor(colorPacked) >>> 24) / 255f <= 0.9f || region == circle || blending != Blending.normal || region == Core.atlas.white() || !region.found()) return;

            float cos = Mathf.cosDeg(rotation), sin = Mathf.sinDeg(rotation);
            float dx = width / 2f - originX, dy = height / 2f - originY;
            regions.add(new TextureRegion(region));
            parts.add(cos * dx - sin * dy + x + originX, sin * dx + cos * dy + y + originY, rotation);
            parts.add(width, height, z);
        }

        @Override
        protected void draw(Texture texture, float[] spriteVertices, int offset, int count){
        }

        @Override
        protected void setPackedMixColor(float packedColor){
        }

        @Override
        protected void flush(){
        }

        @Override
        protected void setShader(Shader shader, boolean apply){
        }
    }
}