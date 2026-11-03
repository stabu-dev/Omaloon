package omaloon.world.blocks.sandbox;

import arc.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.entities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.meta.*;
import omaloon.graphics.BlastRenderer.*;
import omaloon.world.*;

import static mindustry.Vars.*;
import static omaloon.world.NuclearBlasts.*;

public class NuclearBomb extends Block{
    private static final IntSet collided = new IntSet();
    /** Damage to fully exposed targets, added to 10% of their max health. */
    public float damage = 10000f;
    /** Shockwave damage at the center. */
    public float shockDamage = 100f;
    /** Maps remaining range, from 1 at the center to 0 at maximum range, to a damage multiplier. */
    public Interp shockFalloff = Interp.pow2In;
    /** Damage to shadow-casting walls and static blocks, added to 20% of their max health. Don't ask how and why. */
    public float shadowDamage = 21000f;
    public float fireChance = 0.5f;
    public int fireballs = 30;
    public NuclearBomb(String name){
        super(name);
        solid = true;
        destructible = true;
        configurable = true;
        update = true;
        sync = true;
        category = Category.effect;
        configureSound = Sounds.none;
        buildVisibility = BuildVisibility.sandboxOnly;

        config(Boolean.class, (NuclearBombBuild build, Boolean value) -> {
            if(value) build.detonate();
        });
    }

    @Override
    public void load(){
        super.load();
        region = Core.atlas.find(name, Blocks.thoriumWallLarge.region);
    }

    @Override
    public void loadIcon(){
        super.loadIcon();
        if(!fullIcon.found()) fullIcon = Blocks.thoriumWallLarge.fullIcon;
        if(!uiIcon.found()) uiIcon = Blocks.thoriumWallLarge.uiIcon;
    }

    /** @return how much of the target is outside the blast shadow, from 0 to 1. */
    static float exposure(float x, float y, float[] shadow, Sized target){
        float amount = 0f;
        float hsize = target.hitSize() / 2f;

        float dst = Mathf.dst(x, y, target.getX(), target.getY());
        float ang = Angles.angle(x, y, target.getX(), target.getY());
        float angSize = Mathf.angle(dst, hsize);

        int idx1 = (int)(((ang - angSize) / 360f) * shadow.length + 0.5f);
        int idx2 = (int)(((ang + angSize) / 360f) * shadow.length + 0.5f);

        for(int i = idx1; i <= idx2; i++){
            float range = shadow[Mathf.mod(i, shadow.length)];
            if(dst - hsize < range){
                amount += Mathf.clamp((range - (dst - hsize)) / hsize);
            }
        }

        return amount / (idx2 - idx1 + 1);
    }

    public class NuclearBombBuild extends Building{
        public boolean detonated;
        public float remaining;
        public Blast blast;

        @Override
        public boolean configTapped(){
            configure(true);
            return false;
        }

        public void detonate(){
            if(detonated) return;
            detonated = true;
            remaining = silence;
        }

        @Override
        public void updateTile(){
            if(!detonated) return;

            if(blast == null){
                blast = NuclearBlasts.add(x, y, 1f);
                blast.source = this;
                blast.scorch();
                blast.start(null, Math.max(remaining, 0f));
            }

            remaining -= Time.delta;
            if(!net.client() && remaining <= 0f) explode();
        }

        void explode(){
            float bx = x, by = y, last = 0f;
            float[] shadow = blast.shadow;
            collided.clear();
            for(int owner : blast.trace()){
                if(owner == -1) continue;

                blast.scorched.add(owner);
                Building b = world.tile(owner).build;
                if(b != null && collided.add(b.id)){
                    float amount = (shadowDamage + b.maxHealth / 5f) * Mathf.clamp(1f - b.dst(bx, by) / radius) + pressure(b.dst(bx, by));
                    last = Math.max(last, hit(b, () -> {
                        if(b.isValid()) b.damage(amount);
                    }));
                }
            }

            Units.nearby(null, bx, by, shockRange, u -> {
                int id = u.id;
                float scl = exposure(bx, by, shadow, u) * Mathf.clamp(1f - u.dst(bx, by) / radius);
                float amount = (u.maxHealth / 10f + damage) * scl;
                hit(u, () -> {
                    if(!u.isValid() || u.id != id) return;
                    u.apply(StatusEffects.blasted, 60f);
                    u.vel.add(Tmp.v2.trns(Angles.angle(bx, by, u.x, u.y), (16f + 5f / u.mass()) * scl + 4f * Mathf.clamp(1f - u.dst(bx, by) / shockRange)));
                    if(!u.dead && scl > 0f) u.damagePierce(amount);
                    if(!u.dead) u.damage(pressure(u.dst(bx, by)));
                    if(!u.dead && scl > 0f) u.apply(StatusEffects.burning, 600f);
                    if(u.dead && u.isAdded() && u.within(bx, by, ashRadius + u.hitSize / 2f)) Call.unitDestroy(u.id);
                });
            });
            indexer.eachBlock(null, bx, by, shockRange, b -> b != this && !collided.contains(b.id), b -> {
                float scl = exposure(bx, by, shadow, b) * Mathf.clamp(1f - b.dst(bx, by) / radius);
                float amount = (b.maxHealth / 10f + damage) * scl + pressure(b.dst(bx, by));
                if(amount > 0f){
                    hit(b, () -> {
                        if(b.isValid()) b.damage(amount);
                    });
                }
            });

            ignite(bx, by, shadow);
            Time.run(last + 1f, blast::trace);
            if(isValid()) kill();
        }

        float pressure(float dst){
            return shockDamage * shockFalloff.apply(Mathf.clamp(1f - dst / shockRange));
        }

        /** @return ticks until the shockwave front reaches the target and applies the hit. */
        float hit(Position target, Runnable hit){
            float delay = Wave.arrival(target.dst(x, y), shockRange, shockTime);
            Time.run(delay, hit);
            return delay;
        }

        void ignite(float bx, float by, float[] shadow){
            if(net.client()) return;

            for(int i = Mathf.random(1, fireballs); i > 0; i--){
                Bullets.fireball.createNet(Team.derelict, bx, by, Mathf.random(360f), -1f, Mathf.random(0.5f, 1f), 1f);
            }

            Geometry.circle(World.toTile(bx), World.toTile(by), (int)(radius / tilesize), (tx, ty) -> {
                Tile tile = world.tile(tx, ty);
                if(tile == null) return;

                float dst = Mathf.dst(bx, by, tile.worldx(), tile.worldy());
                int ray = Mathf.mod((int)(Angles.angle(bx, by, tile.worldx(), tile.worldy()) / 360f * shadow.length + 0.5f), shadow.length);
                if(dst > shadow[ray]) return;

                boolean fire = Mathf.chance(fireChance * (1f - Mathf.sqrt(dst / radius)));
                Time.run(Wave.arrival(dst, shockRange, shockTime), () -> {
                    if(fire) Fires.create(tile);
                    if(tile.block().unitMoveBreakable) ConstructBlock.deconstructFinish(tile, tile.block(), null);
                });
            });
        }

        @Override
        public byte version(){
            return 1;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.bool(detonated);
            write.f(remaining);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            detonated = revision >= 1 && read.bool();
            remaining = revision >= 1 ? read.f() : 0f;
        }
    }
}