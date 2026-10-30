package omaloon.graphics;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.mock.*;
import arc.struct.*;
import arc.util.*;
import arc.util.pooling.*;
import arc.util.pooling.Pool.*;
import mindustry.game.EventType.*;
import mindustry.graphics.*;
import omaloon.graphics.BlastRenderer.*;

import static mindustry.Vars.*;

/** Sprites broken into jittered quads that fly apart and burn to ash. Ported from Flame Out by EyeOfDarkness. */
public class Disintegration{
    private static final Seq<Piece> pieces = new Seq<>();
    private static final int maxDimension = 64;
    private static final int[] corners = {0, 0, 1, 0, 1, 1, 0, 1};
    private static final float[] verts = new float[4 * 6];
    private static final Color ashColor = Color.gray, heatColor = Pal.lighterOrange, cooledColor = Color.valueOf("8f1c0c");

    float[] xs, ys;
    int width, height, uses;
    float drawWidth, drawHeight;
    TextureRegion region = new TextureRegion();

    public static void init(){
        Events.on(ResetEvent.class, e -> {
            pieces.each(Piece::free);
            pieces.clear();
        });
        Events.run(Trigger.update, () -> {
            if(state.isPaused()) return;
            int alive = 0;
            for(int i = 0, n = pieces.size; i < n; i++){
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
        for(Piece p : pieces){
            p.draw();
        }
    }

    static void generate(TextureRegion region, float x, float y, float rotation, float width, float height, float z, Cons<Piece> cons){
        int limit = BlastRenderer.quality() == 0 ? 0 : BlastRenderer.quality() == 1 ? 1024 : 8192;
        if(pieces.size >= limit) return;
        float cos = Mathf.cosDeg(rotation), sin = Mathf.sinDeg(rotation);

        Disintegration dis = Pools.obtain(Disintegration.class, Disintegration::new);
        dis.set((int)(5f / 320f * Math.abs(width / Draw.scl)), (int)(5f / 320f * Math.abs(height / Draw.scl)));
        dis.region.set(region);
        dis.drawWidth = width;
        dis.drawHeight = height;

        for(int ix = 0; ix < dis.width - 1; ix++){
            for(int iy = 0; iy < dis.height - 1; iy++){
                if(pieces.size >= limit) return;
                float ox = 0f, oy = 0f;
                for(int i = 0; i < 8; i += 2){
                    int pos = dis.toPos(ix + corners[i], iy + corners[i + 1]);
                    ox += dis.xs[pos];
                    oy += dis.ys[pos];
                }
                ox /= 4f;
                oy /= 4f;

                float rx = (ox - 0.5f) * width, ry = (oy - 0.5f) * height;

                Piece p = Pools.obtain(Piece.class, Piece::new);
                p.source = dis;
                p.x = p.startX = rx * cos - ry * sin + x;
                p.y = p.startY = rx * sin + ry * cos + y;
                p.z = z;
                p.rotation = rotation;
                p.idx = dis.toPos(ix, iy);
                p.offsetX = ox;
                p.offsetY = oy;

                cons.get(p);

                pieces.add(p);
                dis.uses++;
            }
        }
    }

    void set(int width, int height){
        this.width = width = Mathf.clamp(width, 4, maxDimension);
        this.height = height = Mathf.clamp(height, 4, maxDimension);
        int size = width * height;

        if(xs == null || xs.length != size) xs = new float[size];
        if(ys == null || ys.length != size) ys = new float[size];

        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                float fx = x / (width - 1f), fy = y / (height - 1f);

                if(x > 0 && x < width - 1) fx += Mathf.range(0.1f, 0.4f) / (width - 1f);
                if(y > 0 && y < height - 1) fy += Mathf.range(0.1f, 0.4f) / (height - 1f);

                xs[toPos(x, y)] = fx;
                ys[toPos(x, y)] = fy;
            }
        }
    }

    int toPos(int x, int y){
        return x + y * width;
    }

    public static class Piece implements Poolable{
        Disintegration source;
        int idx;
        float rotation, offsetX, offsetY, startX, startY;

        public float x, y, z, vx, vy, vr, drag, time, lifetime;
        public float fadeDistance;
        /** Shockwave that carries this piece once the front passes. */
        public @Nullable Wave wave;
        public float windCarry;

        /** @return dissolve progress by travel distance, from 0 to 1. */
        float dissolve(){
            return Mathf.curve(Mathf.dst(startX, startY, x, y), fadeDistance * 0.25f, fadeDistance);
        }

        /** @return whether this piece expired and was freed. */
        boolean update(){
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

            free();
            return true;
        }

        void free(){
            if(--source.uses <= 0) Pools.free(source);
            Pools.free(this);
        }

        void draw(){
            float cos = Mathf.cosDeg(rotation), sin = Mathf.sinDeg(rotation);
            Disintegration s = source;
            TextureRegion region = s.region;

            float scl = Interp.pow5Out.apply(1f - Mathf.clamp(time / lifetime)), cool = Mathf.curve(time / lifetime, 0f, 0.4f);
            Tmp.c1.set(Color.black).lerp(ashColor, Mathf.curve(time / lifetime, 0.3f, 0.8f));
            Tmp.c1.a *= 1f - Interp.pow2In.apply(dissolve());
            float color = Tmp.c1.toFloatBits();
            float mix = Tmp.c2.set(heatColor).lerp(cooledColor, cool).a(1f - Interp.pow2In.apply(cool)).toFloatBits();

            for(int i = 0; i < 8; i += 2){
                int ps = s.toPos(idx % s.width + corners[i], idx / s.width + corners[i + 1]), v = i * 3;

                float ox = (s.xs[ps] - offsetX) * s.drawWidth * scl;
                float oy = (s.ys[ps] - offsetY) * s.drawHeight * scl;

                verts[v] = ox * cos - oy * sin + x;
                verts[v + 1] = ox * sin + oy * cos + y;
                verts[v + 2] = color;
                verts[v + 3] = Mathf.lerp(region.u, region.u2, s.xs[ps]);
                verts[v + 4] = Mathf.lerp(region.v2, region.v, s.ys[ps]);
                verts[v + 5] = mix;
            }

            Draw.z(z);
            Draw.vert(region.texture, verts, 0, verts.length);
        }

        @Override
        public void reset(){
            source = null;
            wave = null;
            time = 0f;
        }
    }

    public static class VaporizeBatch extends Batch{
        /** Replaces {@link Core#gl} while capturing, so GL calls from the drawer do nothing. */
        private static final GL20 mockGl = new MockGL20();

        TextureRegion circle = Core.atlas.find("circle");
        Cons<Piece> cons;

        public void capture(Runnable drawer, Cons<Piece> cons){
            this.cons = cons;

            Batch last = Core.batch;
            GL20 lastGl = Core.gl;
            boolean legacy = Lines.useLegacyLine;
            Core.batch = this;
            Core.gl = mockGl;
            Lines.useLegacyLine = true;

            try{
                drawer.run();
            }finally{
                Lines.useLegacyLine = legacy;
                Core.batch = last;
                Core.gl = lastGl;
                this.cons = null;
            }
        }

        @Override
        protected void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height, float rotation){
            if((Color.floatToIntColor(colorPacked) >>> 24) / 255f <= 0.9f || region == circle || blending != Blending.normal || region == Core.atlas.white() || !region.found()) return;

            float cos = Mathf.cosDeg(rotation), sin = Mathf.sinDeg(rotation);
            float dx = width / 2f - originX, dy = height / 2f - originY;
            generate(region, cos * dx - sin * dy + x + originX, sin * dx + cos * dy + y + originY, rotation, width, height, z, cons);
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