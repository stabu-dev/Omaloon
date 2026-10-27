package omaloon.world.blocks.storage;

import arc.*;
import arc.audio.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.Effect.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import omaloon.content.*;
import omaloon.math.*;

import static mindustry.Vars.*;

public class LandingCapsuleBlock extends GlassmoreCoreBlock{
    static final EffectContainer container = new EffectContainer();
    static final float settleZoomOut = 1.25f, burnStart = 0.72f, burnEndSpeed = 0.45f;

    /** Duration of the fall itself, in ticks*/
    public float fallDuration = 159f;
    public Sound burnSound = Sounds.loopThruster;

    protected float settleDuration, fallHeight, cameraHeight;

    public LandingCapsuleBlock(String name){
        super(name);
        landDuration = 230f;
        landZoomFrom = 0.18f;
        launchEffect = Fx.none;
    }

    @Override
    public void init(){
        settleDuration = landDuration - fallDuration;

        cameraHeight = size * tilesize * 6f;
        fallHeight = cameraHeight * (landZoomTo * settleZoomOut / landZoomFrom - 1f);
        super.init();
    }

    @Override
    public boolean displayShadow(Tile tile){
        return super.displayShadow(tile) && !(tile.build instanceof LandingCapsuleBuild b && b.falling);
    }

    public static class FallParticle{
        public float x, y, z, vx, vy, vz, life, lifetime, size, shade;
        public boolean lit, ember, spark;
    }

    public class LandingCapsuleBuild extends DelayedSpawnCoreBuild{
        public boolean falling, ignited;
        public float fallAngle, fallSpin, tilt;
        public float camX, camY, camZ, camPX, camPY, camPZ, camVX, camVY, camVZ;
        protected @Nullable FallParticle[] particles;
        protected int nextParticle;

        @Override
        public void draw(){
            if(!falling) super.draw();
        }

        @Override
        public void beginLaunch(boolean launching){
            super.beginLaunch(launching);
            if(launching || headless) return;

            int amount = (int)(fallDuration * (size * 1.8f + 0.4f));
            if(particles == null || particles.length != amount){
                particles = new FallParticle[amount];
                for(int i = 0; i < amount; i++) particles[i] = new FallParticle();
            }
            for(var p : particles) p.life = p.lifetime = 0f;

            falling = true;
            ignited = false;
            renderer.blocks.updateShadow(this);
            fallAngle = Mathf.random(360f);
            float angle = Mathf.random(5f, 10f);
            tilt = Mathf.sinDeg(angle) / Mathf.cosDeg(angle);
            fallSpin = Mathf.random(2, 4) * 90f * Mathf.randomSign();

            Time.run(launchDuration(), () -> renderer.shakeTime = renderer.shakeIntensity = 0f);

            float h = height(0f), vh = fallSpeed(0f);
            Vec2 pos = path(h, Tmp.v1);
            camPX = camX = pos.x;
            camPY = camY = pos.y;
            camPZ = camZ = h + cameraHeight;
            camVX = -Angles.trnsx(fallAngle, vh * tilt);
            camVY = -Angles.trnsy(fallAngle, vh * tilt);
            camVZ = -vh;
        }

        public float fallFin(){
            return Mathf.clamp(renderer.getLandTimeIn() * launchDuration() / fallDuration);
        }

        public float settleFin(){
            return Mathf.clamp((renderer.getLandTimeIn() * launchDuration() - fallDuration) / settleDuration);
        }

        public float burnHeight(){
            float k = (burnEndSpeed + (1f - burnEndSpeed) / 4f) * (1f - burnStart) * 1.7f / burnStart;
            return fallHeight * k / (1f + k);
        }

        public float height(float fin){
            float bh = burnHeight();
            if(fin < burnStart){
                float t = fin / burnStart;
                return bh + (fallHeight - bh) * (1f - t * (0.3f + 0.7f * t));
            }

            float u = (fin - burnStart) / (1f - burnStart), speed = (fallHeight - bh) * 1.7f / burnStart;
            return Math.max(bh - (1f - burnStart) * speed * (burnEndSpeed * u + (1f - burnEndSpeed) * (1f - Mathf.pow(1f - u, 4f)) / 4f), 0f);
        }

        public float fallSpeed(float fin){
            float a = Math.max(fin - 0.01f, 0f), b = Math.min(fin + 0.01f, 1f);
            return (height(a) - height(b)) / ((b - a) * fallDuration);
        }

        public float burn(float fin){
            float u = (fin - burnStart) / (1f - burnStart);
            return u <= 0f ? 0f : Mathf.clamp(u * 8f) * (1f - u * u * 0.5f);
        }

        public Vec2 path(float z, Vec2 out){
            return out.trns(fallAngle, z * tilt).add(x, y);
        }

        public float project(float wx, float wy, float z, Vec2 out){
            float ph = z / (camZ - z) / Physics.hMul(1f);
            Physics.parallax(out.set(wx, wy), Tmp.v2.set(camX, camY), ph);
            return 1f + Physics.hMul(ph);
        }

        public void touchdown(){
            falling = false;
            renderer.blocks.updateShadow(this);

            for(var p : particles){
                if(p.life >= p.lifetime) continue;
                float lf = p.life / p.lifetime;
                p.lifetime = Math.min(p.lifetime - p.life, settleDuration) / (1f - lf);
                p.life = lf * p.lifetime;
            }

            OlFx.capsuleImpact.at(x, y, size, Tmp.c1.set(tile.floor().mapColor).mul(1.4f));
            renderer.shake(size * 2.2f, 35f);
        }

        @Override
        public float zoomLaunch(){
            if(renderer.isLaunching()) return super.zoomLaunch();

            float rest = Interp.smooth.apply(Mathf.curve(settleFin(), 0.5f, 1f));
            camX = Mathf.lerp(camPX, x, rest);
            camY = Mathf.lerp(camPY, y, rest);
            camZ = Mathf.lerp(camPZ, cameraHeight * settleZoomOut, rest);

            Core.camera.position.set(camX, camY);
            return Scl.scl(landZoomTo) * settleZoomOut * cameraHeight / camZ;
        }

        public void updateCamera(){
            float tx = x, ty = y, tz = cameraHeight * settleZoomOut, tvx = 0f, tvy = 0f, tvz = 0f, floor = 0f;

            if(falling){
                float fin = fallFin(), h = height(fin), vh = fallSpeed(fin);
                Vec2 pos = path(h, Tmp.v1);
                tx = pos.x;
                ty = pos.y;
                tz = h + cameraHeight;
                tvx = -Angles.trnsx(fallAngle, vh * tilt);
                tvy = -Angles.trnsy(fallAngle, vh * tilt);
                tvz = -vh;
                floor = h;
            }

            float w = falling ? 29f / fallDuration : 7.3f / settleDuration, k = w * w, c = (falling ? 1.34f : 2.1f) * w;
            camVX += ((tx - camPX) * k + (tvx - camVX) * c) * Time.delta;
            camVY += ((ty - camPY) * k + (tvy - camVY) * c) * Time.delta;
            camVZ += ((tz - camPZ) * k + (tvz - camVZ) * c) * Time.delta;
            camPX += camVX * Time.delta;
            camPY += camVY * Time.delta;
            camPZ += camVZ * Time.delta;

            floor += cameraHeight * 0.45f;
            if(camPZ < floor){
                camPZ = floor;
                camVZ = Math.max(camVZ, 0f);
            }
        }

        public FallParticle spawn(float x, float y, float z, float vx, float vy, float vz, float lifetime, float size){
            FallParticle p = particles[nextParticle];
            nextParticle = (nextParticle + 1) % particles.length;

            p.lit = p.ember = p.spark = false;
            p.x = x;
            p.y = y;
            p.z = z;
            p.vx = vx;
            p.vy = vy;
            p.vz = vz;
            p.life = 0f;
            p.lifetime = lifetime;
            p.size = size;
            p.shade = Mathf.random();
            return p;
        }

        @Override
        public void updateLaunch(){
            if(renderer.isLaunching()){
                super.updateLaunch();
                return;
            }

            float fin = fallFin();
            if(falling && fin >= 1f) touchdown();

            updateCamera();
            updateParticles(falling ? fallSpeed(fin) : 0f);
            if(!falling) return;

            float fire = Mathf.clamp(fin * 5f);
            float h = height(fin), vh = fallSpeed(fin), burn = burn(fin);

            if(burn > 0f && !ignited){
                ignited = true;
                renderer.shake(size * 0.8f, 15f);
            }
            if(!headless && burn > 0f){
                control.sound.loop(burnSound, Core.camera.position, 2f * burn);
            }

            float hsize = hitSize() / 2f;
            Vec2 pos = path(h, Tmp.v3);
            float cx = pos.x, cy = pos.y, cvx = -Angles.trnsx(fallAngle, vh * tilt), cvy = -Angles.trnsy(fallAngle, vh * tilt);

            float rotation = spin(fin);

            for(int i = 0; i < size * 5 / 2; i++){
                if(Mathf.chanceDelta(0.07f * fire * (1f - burn * 0.5f))){
                    Tmp.v1.set(Mathf.range(hsize), Mathf.range(hsize)).rotate(rotation);
                    Tmp.v2.rnd(Mathf.random(0.04f, 0.11f) * hsize);
                    spawn(cx + Tmp.v1.x, cy + Tmp.v1.y, h + 4f, cvx * 0.8f + Tmp.v2.x, cvy * 0.8f + Tmp.v2.y, -vh * 0.8f, Mathf.random(100f, 130f), Mathf.random(0.35f, 0.5f) * hsize).lit = i < size / 2;
                }
            }

            landParticleTimer += burn * (0.3f + 0.7f * Mathf.clamp(1f - h / burnHeight())) * Time.delta;
            if(landParticleTimer >= 1f){
                OlFx.capsuleThrustDust.at(x, y, size * (1f + h / cameraHeight * 0.5f), Tmp.c1.set(tile.floor().mapColor).mul(1.4f));
                landParticleTimer = 0f;
            }

            if(Mathf.chanceDelta(0.1f * fire * (1f - burn * 0.5f))){
                float side = Mathf.random(4) * 90f + rotation;
                Tmp.v1.set(hsize, Mathf.range(hsize)).rotate(side);
                Tmp.v2.trns(side + Mathf.range(25f), Mathf.random(0.15f, 0.3f) * hsize);
                spawn(cx + Tmp.v1.x, cy + Tmp.v1.y, h + 2f, cvx * 0.85f + Tmp.v2.x, cvy * 0.85f + Tmp.v2.y, -vh * 0.85f, Mathf.random(14f, 22f), 0f).spark = true;
            }

            if(Mathf.chanceDelta(0.05f * fire * (1f - burn * 0.5f))){
                Tmp.v1.set(Mathf.range(hsize), Mathf.range(hsize)).rotate(rotation);
                Tmp.v2.rnd(Mathf.random(0.04f, 0.1f) * hsize);
                spawn(cx + Tmp.v1.x, cy + Tmp.v1.y, h + 2f, cvx * 0.85f + Tmp.v2.x, cvy * 0.85f + Tmp.v2.y, -vh * 0.85f, 50f, Mathf.random(0.19f, 0.27f) * hsize).ember = true;
            }
        }

        public float spin(float fin){
            return fallSpin * (1f - fin) + Mathf.sin(fin * Mathf.PI * 3f) * 12f * fin * (1f - fin);
        }

        public void updateParticles(float maxSpeed){
            if(particles == null) return;

            float drag = Math.max(1f - 0.03f * Time.delta, 0f);
            for(var p : particles){
                if(p.life >= p.lifetime) continue;

                if(-p.vz > maxSpeed){
                    float scl = Math.max(maxSpeed, 0f) / -p.vz;
                    p.vx *= scl;
                    p.vy *= scl;
                    p.vz *= scl;
                }

                p.x += p.vx * Time.delta;
                p.y += p.vy * Time.delta;
                p.z = Math.max(p.z + p.vz * Time.delta, 2f);
                p.vx *= drag;
                p.vy *= drag;
                p.vz *= drag;
                p.life += Time.delta;
            }
        }

        public void drawParticles(boolean above, float h){
            if(particles == null) return;

            for(var p : particles){
                if(p.life >= p.lifetime || (p.z >= h) != above) continue;

                float depth = camZ - p.z;
                if(depth < 8f) continue;

                Vec2 pos = Tmp.v3;
                float scl = project(p.x, p.y, p.z, pos), lf = p.life / p.lifetime;
                float near = Mathf.clamp((depth - 8f) / 20f);

                if(p.ember){
                    Draw.color(Pal.lightFlame, Pal.darkFlame, lf);
                    Draw.alpha(near);
                    Fill.circle(pos.x, pos.y, (0.3f + Mathf.slope(lf) * p.size) * scl);
                }else if(!p.spark){
                    float heat = p.lit ? 1f - Mathf.curve(lf, 0.03f, 0.15f) : 0f;
                    Tmp.c2.set(Pal.lightFlame).lerp(Pal.darkFlame, Mathf.curve(lf, 0f, 0.12f));
                    Draw.color(Tmp.c1.set(Color.gray).lerp(Color.darkGray, p.shade).lerp(Tmp.c2, heat));
                    Draw.alpha(near);
                    Fill.circle(pos.x, pos.y, Interp.pow2Out.apply(Mathf.curve(lf, 0f, 0.1f)) * (1f - lf) * p.size * scl);
                }else{
                    float sx = pos.x, sy = pos.y, tz = p.z - (p.vz - camVZ) * 2f;
                    if(tz >= camZ - 8f) continue;

                    Vec2 tail = Tmp.v1;
                    project(p.x - (p.vx - camVX) * 2f, p.y - (p.vy - camVY) * 2f, tz, tail);
                    Draw.color(Color.white, Pal.lightOrange, lf);
                    Draw.alpha(near);
                    Lines.stroke((1f - lf) * 1.2f * scl);
                    Lines.line(sx, sy, tail.x, tail.y);
                }
            }

            Draw.reset();
        }

        @Override
        public void drawLanding(float x, float y){
            if(renderer.isLaunching()){
                super.drawLanding(x, y);
                return;
            }

            if(!falling){
                drawParticles(true, 0f);
                return;
            }

            float fin = fallFin(), fout = 1f - fin;
            float h = height(fin);
            Vec2 pos = path(h, Tmp.v3);
            float sizeScl = project(pos.x, pos.y, h, pos);
            x = pos.x;
            y = pos.y;

            drawParticles(false, h);

            float rotation = spin(fin);

            float s = region.width * region.scl() * sizeScl * 3.6f * Interp.pow2Out.apply(fout);
            Draw.color(Pal.lightTrail);
            Draw.rect("circle-shadow", x, y, s, s);

            float ignite = (fin - burnStart) * fallDuration;
            if(ignite >= 0f && ignite < 20f){
                Tmp.m2.set(Draw.trans());
                Draw.trans(Tmp.m1.set(Tmp.m2).translate(x, y).scale(sizeScl * size * 0.75f, sizeScl * size * 0.75f).translate(-x, -y));

                container.set(id, Color.white, ignite, 20f, 0f, x, y, null);
                Fx.shockwave.render(container);
                for(int i = 0; i < 4; i++){
                    container.set(id + i, Color.white, ignite, 20f, rotation + i * 90f, x, y, null);
                    Fx.hitBulletBig.render(container);
                }

                Draw.trans(Tmp.m2);
                Draw.reset();
            }

            float burn = burn(fin);
            if(burn > 0f){
                float hs = hitSize() / 2f * sizeScl, flicker = burn * (0.95f + Mathf.absin(2f, 0.1f));

                Draw.color(team.color);
                Draw.rect("circle-shadow", x, y, hs * 8f * flicker, hs * 8f * flicker);
                Fill.circle(x, y, hs * 1.75f * flicker);
                Draw.color(Color.white);
                Fill.circle(x, y, hs * 1.3f * flicker);
            }

            Draw.color();
            Draw.scl(sizeScl);

            Drawf.spinSprite(region, x, y, rotation);

            Draw.color();
            if(teamRegions[team.id] == teamRegion) Draw.color(team.color);
            Drawf.spinSprite(teamRegions[team.id], x, y, rotation);

            Draw.color();
            Draw.scl();

            float speed = Mathf.clamp(fallSpeed(fin) / fallSpeed(burnStart)) * Mathf.clamp(fin * 5f) * (1f - Mathf.clamp(burn * 3f));
            container.set(id, Color.white, speed * speed, 1f, rotation, x, y, this);
            OlFx.capsuleVapor.render(container);

            drawParticles(true, h);
        }
    }
}
