package omaloon.type.weapons;

import arc.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.units.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.type.*;
import omaloon.content.*;
import omaloon.gen.*;

import static mindustry.Vars.*;

/** Rams ground targets when moving at minShootVelocity or faster, dealing bullet damage and bouncing off. */
public class RamWeapon extends Weapon{
    /** damage the unit takes per hit */
    public float recoilDamage = 10f;
    /** fraction of speed kept when bouncing off */
    public float rebound = 0.6f;
    /** wheel squash on hit at full speed */
    public float squash = 0.35f;
    /** hop velocity on hit at full speed */
    public float hopPower = 0.035f;

    private static final Vec2 normal = new Vec2();
    /** source for hit events, never added */
    private static @Nullable Bullet hitProxy;
    private static final BulletType hitProxyType = new BulletType(){{
        damage = 0f;
        collides = false;
    }};
    private static final UnitDamageEvent damageEvent = new UnitDamageEvent();

    public RamWeapon(){
        super();
        display = true;
        noAttack = true;
        mirror = false;
        showStatSprite = false;
        useAttackRange = false;
        controllable = aiControllable = false;
        autoTarget = alwaysShooting = false;
        reload = 0f;
        shootCone = 70f;
        mountType = RamMount::new;
    }

    @Override
    public void update(Unit unit, WeaponMount mount){
        if(!(mount instanceof RamMount ram)) return;

        float speed = unit.vel.len(), frac = Math.min(speed / unit.speed(), 1f);
        if(minShootVelocity >= 0f && speed < minShootVelocity) return;

        float reach = unit.hitSize / 2f + speed * Time.delta + 1.5f, angle = unit.vel.angle();

        Unit other = Units.closestEnemy(unit.team, unit.x, unit.y, unit.hitSize + 16f, u ->
            u.isGrounded() && u.hittable() &&
            unit.within(u, reach + u.hitSize / 2f) &&
            Angles.within(angle, unit.angleTo(u), shootCone)
        );

        float dealt = bullet.damage * unit.damageMultiplier() * state.rules.unitDamage(unit.team);
        if(other != null){
            float share = other.mass() / (other.mass() + unit.mass());
            normal.set(unit).sub(other).nor();
            Bullet source = hitSource(unit, dealt);
            hit(unit, ram, other, frac, share);
            if(!net.client()){
                boolean wasDead = other.dead;
                other.damage(dealt);
                other.apply(bullet.status, bullet.statusDuration * frac);
                Events.fire(damageEvent.set(other, source));
                other.controller().hit(source);
                if(!wasDead && other.dead) Events.fire(new UnitBulletDestroyEvent(other, source));

                other.impulseNet(Tmp.v1.set(normal).scl(-bullet.knockback * frac * 2f * (1f - share) * other.mass()));
            }
            return;
        }

        unit.hitboxTile(Tmp.r1);
        float half = Tmp.r1.width / 2f, front = half + speed * Time.delta + 1.5f;
        Building build = null;
        for(int i = -1; i <= 1 && build == null; i++){
            Building b = world.buildWorld(unit.x + Angles.trnsx(angle, front) + Angles.trnsx(angle + 90f, half * i), unit.y + Angles.trnsy(angle, front) + Angles.trnsy(angle + 90f, half * i));
            if(b != null && b.team != unit.team && (unit.type.targetUnderBlocks || !b.block.underBullets)) build = b;
        }
        if(build != null){
            build.hitbox(Tmp.r1);
            normal.set(unit).sub(
                Mathf.clamp(unit.x, Tmp.r1.x, Tmp.r1.x + Tmp.r1.width),
                Mathf.clamp(unit.y, Tmp.r1.y, Tmp.r1.y + Tmp.r1.height)
            );
            if(normal.isZero()) normal.set(unit).sub(build);
            normal.nor();

            Bullet source = hitSource(unit, dealt);
            hit(unit, ram, build, frac, 1f);
            if(!net.client()) build.collision(source);
        }
    }

    @Override
    public void addStats(UnitType u, Table t){
        t.row();
        t.add(Core.bundle.format("bullet.damage", Strings.autoFixed(bullet.damage, 1)));

        if(bullet.knockback > 0){
            t.row();
            t.add(Core.bundle.format("bullet.knockback", Strings.autoFixed(bullet.knockback, 2)));
        }

        if(recoilDamage > 0){
            t.row();
            t.add(Core.bundle.format("stat.omaloon-recoil-damage", Strings.autoFixed(recoilDamage, 1)));
        }
    }

    public @Nullable Teamc lastHit(Unit unit){
        RamMount mount = mount(unit);
        return mount == null ? null : mount.lastHit;
    }

    public void clearLastHit(Unit unit){
        RamMount mount = mount(unit);
        if(mount != null) mount.lastHit = null;
    }

    protected @Nullable RamMount mount(Unit unit){
        for(WeaponMount mount : unit.mounts){
            if(mount.weapon == this && mount instanceof RamMount ram) return ram;
        }
        return null;
    }

    private Bullet hitSource(Unit unit, float damage){
        if(hitProxy == null){
            hitProxy = Bullet.create();
            hitProxy.type = hitProxyType;
        }

        hitProxy.set(unit.x, unit.y);
        hitProxy.owner = unit;
        hitProxy.shooter = unit;
        hitProxy.team = unit.team;
        hitProxy.vel.set(unit.vel);
        hitProxy.rotation(unit.vel.angle());
        hitProxy.damage = damage;

        hitProxy.buildingDamageMultiplier = bullet.buildingDamageMultiplier;
        return hitProxy;
    }

    protected void hit(Unit unit, RamMount mount, Teamc target, float frac, float share){
        float dot = unit.vel.dot(normal), bounce = Mathf.lerp(0.4f, 1f, share);
        if(dot < 0f) unit.vel.sub(normal.x * dot * 2f, normal.y * dot * 2f);
        unit.vel.scl(rebound * bounce);

        if(unit instanceof Bouncec b){
            b.hop(hopPower * frac * bounce);
            b.squash(squash * frac);
        }
        if(!net.client()) unit.damage(recoilDamage);

        Tmp.v1.set(unit).sub(normal.x * unit.hitSize / 2f, normal.y * unit.hitSize / 2f);
        bullet.hitEffect.at(Tmp.v1.x, Tmp.v1.y, normal.angle() + 180f);
        bullet.hitSound.at(Tmp.v1.x, Tmp.v1.y, Mathf.random(0.9f, 1.1f), 0.4f + frac * 0.4f);
        Effect.shake(bullet.hitShake * frac * bounce, bullet.hitShake * frac * 4f, Tmp.v1);

        mount.lastHit = target;
    }

    public static class RamMount extends WeaponMount{
        public @Nullable Teamc lastHit;

        public RamMount(Weapon weapon){
            super(weapon);
        }
    }
}