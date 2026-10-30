package omaloon.world;

import arc.*;
import arc.audio.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.entities.effect.*;
import mindustry.game.EventType.*;
import mindustry.game.Teams.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.*;
import mindustry.world.blocks.power.*;
import mindustry.world.blocks.power.NuclearReactor.*;
import mindustry.world.blocks.power.PowerGenerator.*;
import mindustry.world.blocks.storage.CoreBlock.*;
import mindustry.world.meta.*;
import omaloon.content.*;
import omaloon.gen.*;
import omaloon.graphics.*;
import omaloon.graphics.Disintegration.*;
import omaloon.graphics.BlastRenderer.*;

import static mindustry.Vars.*;

public class NuclearBlasts{
    /** Tile each blast-shadow ray ended at, or -1. */
    private static int[] owners = {};
    private static VaporizeBatch vaporizer;
    private static final FloatSeq copies = new FloatSeq();
    private static final Rand ashRand = new Rand();
    public static float radius = 200f, coreRadius = 60f, ashRadius = 100f;
    public static int rays = 720, strobes = 3;
    public static float silence = 12f, impactFade = 120f, shockTime = 210f;
    public static float impactIntensity = 24f, impactRadius = 380f, impactRange = 1000f;
    public static float fireballRadius = 60f, mushroomSize = 62f, shockRange = 540f;
    public static float shockwaveStrength = 9f, shockTint = 0.8f, shockDust = 1f;
    public static float ashFadeDistance = 280f;
    public static float soundVolume = 3f, soundRange = 2400f;

    public static final String setting = "omaloon-reactor-blasts";
    private static final float glowHeat = 0.5f;

    private static final ObjectMap<PowerGenerator, Reactor> reactors = new ObjectMap<>();
    private static final ObjectSet<Effect> reactorEffects = new ObjectSet<>();
    private static final Seq<Blast> active = new Seq<>();
    private static final Seq<Burn> burns = new Seq<>();
    private static final ObjectSet<Effect> deathEffects = new ObjectSet<>();
    private static final ObjectMap<Unit, Blast> exposed = new ObjectMap<>();
    private static final Seq<Drawc> remains = new Seq<>();

    public static void init(){
        vaporizer = new VaporizeBatch();
        for(Block block : content.blocks()){
            if(!(block instanceof NuclearReactor || block instanceof ImpactReactor)) continue;

            PowerGenerator g = (PowerGenerator)block;
            Reactor reactor = new Reactor(g);
            reactorEffects.add(reactor.effect);
            reactors.put(g, reactor);
        }
        apply(Core.settings.getBool(setting, false));

        Events.on(ResetEvent.class, e -> clear());
        Events.on(BlockDestroyEvent.class, e -> {
            Building b = e.tile.build;
            if(b == null || !b.dead || BlastRenderer.quality() == 0) return;

            boolean radial = b instanceof GeneratorBuild g && prepare(g);
            Blast blast = blastAt(b.x, b.y, b.hitSize() / 2f);
            if(blast != null){
                blast.vaporize(b, radial, true);
                Burn burn = new Burn(b.x, b.y, b.hitSize() / 2f, b.block.destroyEffect);
                if(b instanceof CoreBuild){
                    burn.radius = 40f + b.hitSize();
                    burn.addEffect(Fx.coreExplosion);
                    burn.addEffect(Fx.podLandDust);
                    burn.addEffect(Fx.commandSend);
                }
                burns.add(burn);
            }
        });
        Events.run(Trigger.afterGameUpdate, () -> {
            active.removeAll(a -> Time.time > a.until);
            for(var it = exposed.entries(); it.hasNext();){
                var e = it.next();
                if(!e.key.isAdded() || !e.key.dead && Time.time > e.value.until) it.remove();
            }

            burns.removeAll(b -> Time.time - b.time > b.lifetime);
            if(burns.isEmpty()){
                deathEffects.clear();
                return;
            }

            remains.clear();
            Groups.draw.each(d -> {
                if(!(d instanceof EffectState || d instanceof Decal)) return;
                if(d instanceof EffectState s && (!deathEffects.contains(s.effect) || reactorEffects.contains(s.effect))) return;
                for(Burn b : burns){
                    float age = Time.time - b.time + Time.delta;
                    boolean deathEffect = d instanceof EffectState s && s.time <= age && b.effects.contains(s.effect);
                    boolean decal = d instanceof Decal c && c.time <= age;
                    if((deathEffect || decal) && d.within(b.x, b.y, b.radius)){
                        remains.add(d);
                        break;
                    }
                }
            });
            remains.each(Drawc::remove);
            remains.clear();
        });
        Events.on(UnitDestroyEvent.class, e -> {
            if(BlastRenderer.quality() == 0) return;

            Unit u = e.unit;
            Blast blast = exposed.remove(u);
            if(blast == null) blast = blastAt(u.x, u.y, u.hitSize / 2f);
            if(blast != null){
                blast.vaporize(u);
                burns.add(new Burn(u.x, u.y, u.bounds() / 2f, u.type.deathExplosionEffect));
            }
        });

        Events.run(Trigger.update, () -> {
            if(!Core.settings.getBool(setting, false) || !state.isGame() || state.isPaused() || !state.rules.reactorExplosions) return;

            for(TeamData data : state.teams.present){
                for(Building b : indexer.getFlagged(data.team, BlockFlag.reactor)){
                    if(b instanceof NuclearReactorBuild r && r.heat > glowHeat) glow(r);
                }
            }
        });
    }

    public static void apply(boolean enabled){
        reactors.each((g, r) -> {
            g.explodeEffect = enabled ? r : r.effect;
            g.explodeSound = enabled ? Sounds.none : r.sound;
        });
    }

    public static boolean prepare(GeneratorBuild g){
        if(headless || !Core.settings.getBool(setting, false) || !reactors.containsKey((PowerGenerator)g.block) || !state.rules.reactorExplosions || !g.shouldExplode()) return false;

        add(g.x, g.y, scale((PowerGenerator)g.block));
        return true;
    }

    public static Blast add(float x, float y, float scale){
        Blast blast = new Blast(x, y, scale);
        if(headless || BlastRenderer.quality() == 0) return blast;
        active.add(blast);
        Units.nearby(null, x, y, blast.radius, u -> {
            if(!u.dead) exposed.put(u, blast);
        });
        return blast;
    }

    static void clear(){
        active.clear();
        burns.clear();
        deathEffects.clear();
        exposed.clear();
        remains.clear();
    }

    static @Nullable Blast blastAt(float x, float y, float size){
        for(Blast a : active){
            if(Time.time <= a.until && Mathf.within(x, y, a.x, a.y, a.radius + size)) return a;
        }
        return null;
    }

    static float scale(PowerGenerator g){
        return g.explosionRadius * tilesize / radius;
    }

    static void glow(NuclearReactorBuild r){
        float scale = scale((PowerGenerator)r.block), strength = Mathf.curve(r.heat, glowHeat, 1f);
        float core = impactRadius * scale * strength;
        BlastRenderer.glow(r.x, r.y, impactIntensity * scale * strength, core, impactRange * scale, strength, () -> Fill.circle(r.x, r.y, core * 0.5f));
    }

    static void fillShadow(float x, float y, float[] shadow, float radius, float reach){
        float solid = Draw.getColor().toFloatBits(), clear = Tmp.c1.set(Draw.getColor()).a(0f).toFloatBits();

        for(int i = 0; i < shadow.length; i++){
            float len1 = shadow[i], len2 = shadow[(i + 1) % shadow.length];
            if(len1 >= radius - 0.01f) len1 = reach;
            if(len2 >= radius - 0.01f) len2 = reach;
            float in1 = len1 >= reach ? reach * 0.65f : len1, in2 = len2 >= reach ? reach * 0.65f : len2;
            float cos1 = Mathf.cosDeg(i * 360f / shadow.length), sin1 = Mathf.sinDeg(i * 360f / shadow.length);
            float cos2 = Mathf.cosDeg((i + 1f) * 360f / shadow.length), sin2 = Mathf.sinDeg((i + 1f) * 360f / shadow.length);

            Fill.tri(x, y, x + cos1 * in1, y + sin1 * in1, x + cos2 * in2, y + sin2 * in2);
            if(in1 < len1 || in2 < len2){
                Fill.quad(
                x + cos1 * in1, y + sin1 * in1, solid,
                x + cos1 * len1, y + sin1 * len1, in1 < len1 ? clear : solid,
                x + cos2 * len2, y + sin2 * len2, in2 < len2 ? clear : solid,
                x + cos2 * in2, y + sin2 * in2, solid
                );
            }
        }
    }

    public static int[] castShadow(float bx, float by, float radius, float coreRadius, float[] out){
        if(owners.length != out.length) owners = new int[out.length];
        for(int i = 0; i < out.length; i++){
            out[i] = radius;
            owners[i] = -1;
        }

        float step = 360f / out.length;
        int x1 = Math.max(World.toTile(bx - radius), 0), y1 = Math.max(World.toTile(by - radius), 0);
        int x2 = Math.min(World.toTile(bx + radius), world.width() - 1), y2 = Math.min(World.toTile(by + radius), world.height() - 1);

        for(int tx = x1; tx <= x2; tx++){
            for(int ty = y1; ty <= y2; ty++){
                Tile tile = world.tile(tx, ty);
                if(tile.block().isAir() || !(tile.block().isStatic() || tile.block() instanceof Wall) || Mathf.within(bx, by, tile.worldx(), tile.worldy(), coreRadius)) continue;

                float minX = tile.worldx() - tilesize / 2f, minY = tile.worldy() - tilesize / 2f, maxX = minX + tilesize, maxY = minY + tilesize;
                if(Mathf.dst(bx, by, tile.worldx(), tile.worldy()) - tilesize > radius) continue;

                float mid = Angles.angle(bx, by, tile.worldx(), tile.worldy()), lo = 0f, hi = 0f;
                for(int c = 0; c < 4; c++){
                    float d = Mathf.mod(Angles.angle(bx, by, c % 2 == 0 ? minX : maxX, c < 2 ? minY : maxY) - mid + 180f, 360f) - 180f;
                    lo = Math.min(lo, d);
                    hi = Math.max(hi, d);
                }

                for(int i = Mathf.ceil((mid + lo) / step); i <= Mathf.floor((mid + hi) / step); i++){
                    int ray = Mathf.mod(i, out.length);
                    float cos = Mathf.cosDeg(ray * step), sin = Mathf.sinDeg(ray * step);

                    boolean vertical = Math.abs(cos) < 0.00001f, horizontal = Math.abs(sin) < 0.00001f;
                    if(vertical && (bx < minX || bx > maxX) || horizontal && (by < minY || by > maxY)) continue;

                    float rx1 = vertical ? 0f : (minX - bx) / cos, rx2 = vertical ? radius : (maxX - bx) / cos;
                    float ry1 = horizontal ? 0f : (minY - by) / sin, ry2 = horizontal ? radius : (maxY - by) / sin;
                    float enter = Math.max(0f, Math.max(Math.min(rx1, rx2), Math.min(ry1, ry2)));
                    float exit = Math.min(radius, Math.min(Math.max(rx1, rx2), Math.max(ry1, ry2)));

                    if(enter <= exit && enter < out[ray]){
                        out[ray] = enter;
                        owners[ray] = tile.pos();
                    }
                }
            }
        }

        return owners;
    }

    public static class Vapor{
        public final float size;

        public Vapor(float size){
            this.size = size;
        }
    }

    public static class Imprint{
        public TextureRegion region;
        public float rotation;
        public float[] copies;
    }

    static class Reactor extends Effect{
        final PowerGenerator generator;
        final Effect effect;
        final Sound sound;

        Reactor(PowerGenerator generator){
            super(1f, e -> {});
            this.generator = generator;
            effect = generator.explodeEffect;
            sound = generator.explodeSound;
        }

        @Override
        public void create(float x, float y, float rotation, Color color, Object data){
            if(headless) return;

            Blast blast = active.find(a -> Time.time <= a.until && Mathf.equal(a.x, x) && Mathf.equal(a.y, y));
            if(blast == null) blast = NuclearBlasts.add(x, y, scale(generator));
            for(int owner : blast.trace()){
                if(owner != -1) blast.scorched.add(owner);
            }
            blast.start(effect, silence);
        }
    }

    public static class Blast{
        public final float x, y, scale;
        final float radius;
        float until;
        public final float[] shadow = new float[rays];
        public final IntSet scorched = new IntSet();
        public @Nullable Wave wave;

        public Blast(float x, float y, float scale){
            this.x = x;
            this.y = y;
            this.scale = scale;
            radius = ashRadius * scale;
            until = Time.time + 10f;
        }

        /** @return the walls blocking this explosion; reused by the next trace. */
        public int[] trace(){
            return castShadow(x, y, NuclearBlasts.radius * scale, coreRadius * scale, shadow);
        }

        public void start(@Nullable Effect fireball, float delay){
            if(headless) return;
            until = Time.time + delay + 30f;
            float r = NuclearBlasts.radius * scale;
            Core.app.post(() -> BlastRenderer.add(x, y, impactIntensity * scale, impactRadius * scale, impactRange * scale, delay, strobes, impactFade, () -> fillShadow(x, y, shadow, r, impactRadius * scale)));
            Time.run(delay, () -> {
                if(!state.isGame()) return;

                Effect.shake(9f * scale, 150f, x, y);
                if(!headless){
                    float fall = Mathf.clamp(1f - Mathf.dst(x, y, Core.camera.position.x, Core.camera.position.y) / soundRange);
                    int voice = OlSounds.nuclearExplosion.play(Core.audio.sfxVolume * soundVolume * fall, 1f, OlSounds.nuclearExplosion.calcPan(x, y), false, false);
                    Core.audio.protect(voice, true);
                }
                (fireball == null ? BlastRenderer.quality() == 0 ? Fx.reactorExplosion : OlFx.nukeFireball : fireball).at(x, y, fireballRadius * scale);
                Wave created = BlastRenderer.wave(x, y, shockRange * scale, shockwaveStrength * scale, shockTint, shockDust, shockTime, OlFx.nukeMushroom.lifetime);
                if(BlastRenderer.quality() > 0) OlFx.nukeMushroom.at(x, y, mushroomSize * scale, created);
                Fx.spawnShockwave.at(x, y, r * 2f);
                scorched.each(pos -> {
                    Tile tile = world.tile(pos);
                    if(tile != null && !tile.block().isAir()) OlFx.nukeWallDust.at(tile.worldx(), tile.worldy(), Angles.angle(x, y, tile.worldx(), tile.worldy()), tile.block().mapColor);
                });
                scorched.clear();
                wave = created;
            });
        }

        public void vaporize(Unit u){
            vaporize(u::draw, x, y, NuclearBlasts.radius * scale, 1.5f, 90f, 0.25f, Layer.flyingUnit);
            if(u.isGrounded()) imprint(u.type.fullIcon, u.x, u.y, u.rotation - 90f, u.hitSize);
            OlFx.nukeVapor.at(u.x, u.y, Angles.angle(x, y, u.x, u.y), u.hitSize / 2f);
        }

        public void vaporize(Building b, boolean radial, boolean imprint){
            vaporize(b::draw, radial ? b.x : x, radial ? b.y : y, ashRadius * scale, 0.75f, 50f, 0.225f, Layer.turret + 1f);
            if(imprint) imprint(b.block.fullIcon, b.x, b.y, b.block.rotate ? b.rotdeg() : 0f, b.hitSize());
            OlFx.nukeVapor.at(b.x, b.y, Angles.angle(x, y, b.x, b.y), radial || Mathf.within(x, y, b.x, b.y, 0.01f) ? new Vapor(b.hitSize() / 2f) : b.hitSize() / 2f);
        }

        private void imprint(TextureRegion region, float tx, float ty, float rotation, float size){
            if(headless || BlastRenderer.quality() < 2) return;

            Imprint im = new Imprint();
            im.region = region;
            im.rotation = rotation;
            float length = impactIntensity * scale + size * 1.5f, dst = Math.max(Mathf.dst(x, y, tx, ty), 8f);
            int steps = Math.max((int)length, 2);

            copies.clear();
            for(int i = steps; i >= 0; i--){
                float scl = 1f + length * i / steps / dst;
                float px = x + (tx - x) * scl, py = y + (ty - y) * scl;

                Tile tile = world.tileWorld(px, py);
                if(tile == null || !tile.floor().hasSurface()) continue;

                copies.add(px, py, scl, 0.02f * (1f - Interp.pow2In.apply(i / (float)steps)));
            }
            im.copies = copies.toArray();
            OlFx.nukeImprint.at(tx, ty, 0f, im);
        }

        private void vaporize(Runnable drawer, float bx, float by, float range, float near, float falloff, float far, float layer){
            if(headless || BlastRenderer.quality() == 0) return;

            Rand previous = Mathf.rand;
            Mathf.rand = ashRand;
            try{
                vaporizer.capture(drawer, d -> {
                    float dx = d.x - bx, dy = d.y - by;
                    float len = Mathf.len(dx, dy);
                    float force = near / (1f + len / falloff) + (len / range) * far;

                    Vec2 v = Tmp.v1.set(dx, dy).nor().setLength(force * Mathf.random(0.9f, 1f));
                    d.lifetime = shockTime * Mathf.random(0.8f, 1.1f);
                    d.drag = 0.02f;
                    d.vx = v.x;
                    d.vy = v.y;
                    d.vr = Mathf.range((force / 3f) * 5f);
                    d.z = layer;
                    d.fadeDistance = ashFadeDistance;
                    d.wave = wave;
                    d.windCarry = Mathf.random(0.55f, 0.9f);
                });
            }finally{
                Mathf.rand = previous;
            }
        }
    }

    static class Burn{
        float x, y, radius, time = Time.time;
        float lifetime = 50f;
        final ObjectSet<Effect> effects = new ObjectSet<>();

        Burn(float x, float y, float radius, Effect effect){
            this.x = x;
            this.y = y;
            this.radius = radius;
            addEffect(effect);
            addEffect(Fx.blockExplosionSmoke);
            addEffect(Fx.shockwave);
            addEffect(Fx.bigShockwave);
        }

        void addEffect(Effect effect){
            if(effect == null || !effects.add(effect)) return;
            deathEffects.add(effect);
            lifetime = Math.max(lifetime, effect.startDelay + 50f);
            if(effect instanceof MultiEffect multi){
                for(Effect child : multi.effects) addEffect(child);
            }else if(effect instanceof WrapEffect wrap){
                addEffect(wrap.effect);
            }else if(effect instanceof SoundEffect sound){
                addEffect(sound.effect);
            }
        }
    }
}