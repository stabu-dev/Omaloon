package omaloon.entities.units;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import ent.anno.Annotations.*;
import mindustry.audio.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import omaloon.type.*;
import omaloon.utils.*;

import static arc.Core.*;
import static mindustry.Vars.*;

@EntityPoint
public class MillipedeSegmentUnit extends UnitEntity{
    public MillipedeUnitType millipedeType;
    protected float segmentHealth;
    protected MillipedeDefaultUnit trueParentUnit;
    protected Unit parentUnit;
    protected boolean isBugged;
    protected int shootSequence, segmentType;

    public int getSegmentLength(){
        return millipedeType.segmentLength;
    }

    @Override
    public void type(UnitType type){
        super.type(type);
        if(type instanceof MillipedeUnitType m) millipedeType = m;
        else throw new ClassCastException("you set this unit's type a in sneaky way");
    }

    @Override
    public boolean collides(Hitboxc other){
        if(trueParentUnit == null) return true;
        MillipedeSegmentUnit[] segs = trueParentUnit.segmentUnits;
        for(int i = 0, len = getSegmentLength(); i < len; i++){
            if(segs[i] == other) return false;
        }
        return true;
    }

    @Override
    public void add(){
        if(added) return;
        isBugged = true;
        Groups.all.add(this);
        Groups.unit.add(this);
        Groups.sync.add(this);
        Groups.draw.add(this);
        added = true;
        updateLastPosition();
    }

    @Override
    public void setType(UnitType type){
        this.type = type;
        maxHealth = segmentHealth = type.health;
        drag = type.drag;
        armor = type.armor;
        hitSize = type.hitSize;
        hovering = type.hovering;

        if(controller == null) controller(type.createController(self()));
        if(mounts().length != type.weapons.size) setupWeapons(type);
        if(type instanceof MillipedeUnitType m) millipedeType = m;
        else throw new ClassCastException("you set this unit's type in sneaky way");
    }

    @Override
    public void remove(){
        if(!added) return;
        Groups.all.remove(this);
        Groups.unit.remove(this);
        Groups.sync.remove(this);
        Groups.draw.remove(this);
        added = false;
        controller.removed(this);
        if(net.client()) netClient.addRemovedEntity(id);
    }

    @Override
    public void damage(float amount){
        if(millipedeType.splittable) segmentHealth -= amount * millipedeType.segmentDamageScl;
        trueParentUnit.damage(amount);
        /*if(trueParentUnit.controller instanceof MillipedeAI){
            ((MillipedeAI)trueParentUnit.controller).setTarget(x, y, amount);
        }*/
    }

    public void segmentDamage(float amount){
        segmentHealth -= amount;
    }

    @Override
    public void controller(UnitController next){
        if(!(next instanceof Player)){
            controller = next;
            if(controller.unit() != this) controller.unit(this);
        }else if(trueParentUnit != null){
            trueParentUnit.controller(next);
            if(trueParentUnit.controller().unit() != trueParentUnit) trueParentUnit.controller().unit(trueParentUnit);
        }
    }

    @Override
    public boolean isPlayer(){
        if(trueParentUnit == null) return false;
        return trueParentUnit.controller() instanceof Player;
    }

    @Override
    public boolean isAI(){
        if(trueParentUnit == null) return true;
        return trueParentUnit.controller() instanceof AIController;
    }

    @Override
    public Player getPlayer(){
        if(trueParentUnit == null) return null;
        return isPlayer() ? (Player)trueParentUnit.controller() : null;
    }

    /*@Override
    public int classId(){
        return OlEntityMapping.classId(MillipedeSegmentUnit.class);
    }*/

    @Override
    public void heal(float amount){
        if(trueParentUnit != null) trueParentUnit.heal(amount);
        health += amount;
        segmentHealth = Mathf.clamp(segmentHealth + amount, 0f, maxHealth);
        clampHealth();
    }

    @Override
    public void kill(){
        if(dead || net.client()) return;
        if(trueParentUnit != null) Call.unitDeath(trueParentUnit.id);
        Call.unitDeath(id);
    }

    public void setSegmentType(int val){
        segmentType = val;
    }

    @Override
    public void setupWeapons(UnitType def){
        if(!(def instanceof MillipedeUnitType w)) super.setupWeapons(def);
        else{
            Seq<WeaponMount> tmpSeq = new Seq<>();
            Seq<Weapon> originSeq = w.segWeapSeq;
            for(int i = 0; i < originSeq.size; i++) tmpSeq.add(new WeaponMount(originSeq.get(i)));
            mounts = tmpSeq.toArray(WeaponMount.class);
        }
    }

    @Override
    public boolean serialize(){
        return false;
    }

    @Override
    public void update(){
        if(parentUnit == null || parentUnit.dead || !parentUnit.isAdded()){
            dead = true;
            remove();
        }
        if(trueParentUnit != null && isBugged){
            if(!Structs.contains(trueParentUnit.segmentUnits, s -> s == this)) remove();
            else isBugged = false;
        }
    }

    public void segmentUpdate(){
        if(trueParentUnit != null){
            if(millipedeType.splittable && millipedeType.healthDistribution <= 0f) maxHealth = trueParentUnit.maxHealth;
            if(!millipedeType.splittable){
                health = trueParentUnit.health;
            }else{
                if(segmentHealth > maxHealth) segmentHealth = maxHealth;
                health = segmentHealth;
            }
            hitTime = trueParentUnit.hitTime;
            ammo = trueParentUnit.ammo;
        }else{
            return;
        }
        if(millipedeType.splittable && segmentHealth <= 0f){
            split();
        }
        if(team != trueParentUnit.team) team = trueParentUnit.team;
        if(!net.client() && !dead && controller != null) controller.updateUnit();
        if(controller == null || !controller.isValidController()) resetController();
        updateWeapon();
        updateStatus();
    }

    //probably inefficient
    protected void split(){
        int index = 0;
        MillipedeDefaultUnit hd = trueParentUnit;
        hd.maxHealth /= 2f;
        hd.health = Math.min(hd.health, hd.maxHealth);
        for(int i = 0; i < hd.segmentUnits.length; i++){
            if(hd.segmentUnits[i] == this){
                index = i;
                break;
            }
        }
        if(index >= hd.segmentUnits.length - 1) trueParentUnit.removeTail();
        if(index <= 0 || index >= hd.segmentUnits.length - 1){
            return;
        }
        hd.segmentUnits[index - 1].segmentType = 1;
        MillipedeDefaultUnit newHead = (MillipedeDefaultUnit)type.create(team);
        hd.segmentUnits[index + 1].parentUnit = newHead;
        newHead.addSegments = false;
        newHead.attachTime = 0f;
        newHead.set(this);
        newHead.vel.set(vel);
        newHead.maxHealth /= 2f;
        newHead.health /= 2f;
        newHead.rotation = rotation;

        SegmentData oldSeg = new SegmentData(hd.segmentUnits.length), newSeg = new SegmentData(hd.segmentUnits.length);
        for(int i = 0; i < hd.segmentUnits.length; i++){
            hd.segmentUnits[i].maxHealth /= 2f;
            hd.segmentUnits[i].clampHealth();
            if(i < index){
                oldSeg.add(hd, i);
            }
            if(i > index){
                newSeg.add(hd, i);
            }
        }
        oldSeg.set(hd);
        newSeg.set(newHead);
        newHead.add();
        millipedeType.splitSound.at(x, y, Mathf.random(0.9f, 1.1f));
        remove();
    }

    protected void updateStatus(){
        if(trueParentUnit == null || trueParentUnit.dead) return;
        if(!statuses.isEmpty()) statuses.each(s -> trueParentUnit.apply(s.effect, s.time));
        statuses.clear();
    }

    protected void updateWeapon(){
        boolean can = canShoot();
        for(WeaponMount mount : mounts){
            Weapon weapon = mount.weapon;
            mount.reload = Math.max(mount.reload - Time.delta * reloadMultiplier, 0);
            float weaponRotation = this.rotation - 90 + (weapon.rotate ? mount.rotation : 0);
            float mountX = this.x + Angles.trnsx(this.rotation - 90, weapon.x, weapon.y);
            float mountY = this.y + Angles.trnsy(this.rotation - 90, weapon.x, weapon.y);
            float shootX = mountX + Angles.trnsx(weaponRotation, weapon.shootX, weapon.shootY);
            float shootY = mountY + Angles.trnsy(weaponRotation, weapon.shootX, weapon.shootY);
            float shootAngle = weapon.rotate ? weaponRotation + 90 : Angles.angle(shootX, shootY, mount.aimX, mount.aimY) + (this.rotation - angleTo(mount.aimX, mount.aimY));
            if(weapon.continuous && mount.bullet != null){
                if(!mount.bullet.isAdded() || mount.bullet.time >= mount.bullet.lifetime || mount.bullet.type != weapon.bullet){
                    mount.bullet = null;
                }else{
                    mount.bullet.rotation(weaponRotation + 90);
                    mount.bullet.set(shootX, shootY);
                    mount.reload = weapon.reload;
                    vel.add(Tmp.v1.trns(rotation + 180.0F, mount.bullet.type.recoil));
                    if(weapon.shootSound != Sounds.none && !headless){
                        if(mount.sound == null) mount.sound = new SoundLoop(weapon.shootSound, 1.0F);
                        mount.sound.update(x, y, true);
                    }
                }
            }else{
                mount.heat = Math.max(mount.heat - Time.delta * reloadMultiplier / mount.weapon.cooldownTime, 0);
                if(mount.sound != null){
                    mount.sound.update(x, y, false);
                }
            }
            if(weapon.otherSide != -1 && weapon.alternate && mount.side == weapon.flipSprite && mount.reload + Time.delta > weapon.reload / 2.0F && mount.reload <= weapon.reload / 2.0F){
                mounts[weapon.otherSide].side = !mounts[weapon.otherSide].side;
                mount.side = !mount.side;
            }
            if(weapon.rotate && (mount.rotate || mount.shoot) && can){
                float axisX = this.x + Angles.trnsx(this.rotation - 90, weapon.x, weapon.y);
                float axisY = this.y + Angles.trnsy(this.rotation - 90, weapon.x, weapon.y);
                mount.targetRotation = Angles.angle(axisX, axisY, mount.aimX, mount.aimY) - this.rotation;
                mount.rotation = Angles.moveToward(mount.rotation, mount.targetRotation, weapon.rotateSpeed * Time.delta);
            }else if(!weapon.rotate){
                mount.rotation = 0;
                mount.targetRotation = angleTo(mount.aimX, mount.aimY);
            }
            if(mount.shoot && can && (ammo > 0 || !state.rules.unitAmmo || team().rules().infiniteAmmo) && (!weapon.alternate || mount.side == weapon.flipSprite) && (vel.len() >= mount.weapon.minShootVelocity || (net.active() && !isLocal())) && mount.reload <= 1.0E-4F && Angles.within(weapon.rotate ? mount.rotation : this.rotation, mount.targetRotation, mount.weapon.shootCone)){
                shoot(mount, shootX, shootY, mount.aimX, mount.aimY, mountX, mountY, shootAngle, Mathf.sign(weapon.x));
                mount.reload = weapon.reload;
                ammo--;
                if(ammo < 0) ammo = 0;
            }
        }
    }

    protected void shoot(WeaponMount mount, float x, float y, float aimX, float aimY, float mountX,
                         float mountY, float rotation, int side){
        Weapon weapon = mount.weapon;
        float baseX = this.x;
        float baseY = this.y;
        boolean delay = weapon.shoot.firstShotDelay + weapon.shoot.shotDelay > 0f;
        (delay ? weapon.chargeSound : weapon.continuous ? Sounds.none : weapon.shootSound).at(x, y, Mathf.random(weapon.soundPitchMin, weapon.soundPitchMax));
        BulletType ammo = weapon.bullet;
        float lifeScl = ammo.keepVelocity ? Mathf.clamp(Mathf.dst(x, y, aimX, aimY) / ammo.range) : 1f;
        final float[] sequenceNum = {0};
        if(delay){
            OlUtils.shotgun(weapon.shoot.shots, weapon.reload, rotation, (f)->{
                Time.run(sequenceNum[0] * weapon.shoot.shotDelay + weapon.shoot.firstShotDelay, ()->{
                    if(!isAdded()) return;
                    mount.bullet = bullet(weapon, x + this.x - baseX, y + this.y - baseY, f + Mathf.range(weapon.inaccuracy), lifeScl);
                });
                sequenceNum[0]++;
            });
        } else {
            OlUtils.shotgun(weapon.shoot.shots, weapon.reload, rotation, f -> mount.bullet = bullet(weapon, x, y, f + Mathf.range(weapon.inaccuracy), lifeScl));
        }
        boolean parentize = ammo.keepVelocity;
        if(delay){
            Time.run(weapon.shoot.firstShotDelay, () -> {
                if(!isAdded()) return;
                vel.add(Tmp.v1.trns(rotation + 180f, ammo.recoil));
                Effect.shake(weapon.shake, weapon.shake, x, y);
                mount.heat = 1f;
                if(!weapon.continuous){
                    weapon.shootSound.at(x, y, Mathf.random(weapon.soundPitchMin, weapon.soundPitchMax));
                }
            });
        }else{
            vel.add(Tmp.v1.trns(rotation + 180f, ammo.recoil));
            Effect.shake(weapon.shake, weapon.shake, x, y);
            mount.heat = 1f;
        }
        weapon.ejectEffect.at(mountX, mountY, rotation * side);
        ammo.shootEffect.at(x, y, rotation, parentize ? this : null);
        ammo.smokeEffect.at(x, y, rotation, parentize ? this : null);
        apply(weapon.shootStatus, weapon.shootStatusDuration);
    }

    protected Bullet bullet(Weapon weapon, float x, float y, float angle, float lifescl){
        return weapon.bullet.create(this, this.team, x, y, angle, 1.0f - weapon.velocityRnd + Mathf.random(weapon.velocityRnd), lifescl);
    }

    public void drawBody(){
        float z = Draw.z();
        type.applyColor(this);
        TextureRegion region = segmentType == 0 ? millipedeType.segmentRegion : millipedeType.tailRegion;
        Draw.rect(region, this, rotation - 90);
        TextureRegion segCellReg = millipedeType.segmentCellRegion;
        if(segmentType == 0 && segCellReg != atlas.find("error")) drawCell(segCellReg);
        TextureRegion outline = millipedeType.segmentOutline == null || millipedeType.tailOutline == null ? null : segmentType == 0 ? millipedeType.segmentOutline : millipedeType.tailOutline;
        if(outline != null){
            Draw.color(Color.white);
            Draw.z(Draw.z());
            Draw.rect(outline, this, rotation - 90f);
            Draw.z(z);
        }
        Draw.reset();
    }

    public void drawCell(TextureRegion cellRegion){
        Draw.color(type.cellColor(this));
        Draw.rect(cellRegion, x, y, rotation - 90f);
    }

    public void drawShadow(){
        TextureRegion region = segmentType == 0 ? millipedeType.segmentRegion : millipedeType.tailRegion;
        Draw.color(Pal.shadow); //seems to not exist in v106
        float e = Math.max(elevation, type.shadowElevation);
        Draw.rect(region, x + (UnitType.shadowTX * e), y + UnitType.shadowTY * e, rotation - 90f);
        Draw.color();
    }

    @Override
    public void draw(){

    }

    @Override
    public void collision(Hitboxc other, float x, float y){
        super.collision(other, x, y);
        if(trueParentUnit != null) trueParentUnit.handleCollision(this, other, x, y);
    }

    protected void setTrueParent(MillipedeDefaultUnit parent){
        shootSequence = 0;
        trueParentUnit = parent;
    }

    public void setParent(Unit parent){
        parentUnit = parent;
    }

    protected static class SegmentData{
        MillipedeSegmentUnit[] units;
        Vec2[] pos;
        Vec2[] vel;
        int size = 0;

        SegmentData(int size){
            units = new MillipedeSegmentUnit[size];
            pos = new Vec2[size];
            vel = new Vec2[size];
        }

        void add(MillipedeSegmentUnit unit, Vec2 vel){
            units[size] = unit;
            pos[size] = new Vec2(unit.getX(), unit.getY());
            this.vel[size++] = vel;
        }

        void add(MillipedeDefaultUnit unit, int index){
            Log.info(toString() + ":" + unit.segmentUnits[index] + ":" + unit.segments[index] + ":" + unit.segmentVelocities[index] + ":" + index);
            units[size] = unit.segmentUnits[index];
            pos[size] = unit.segments[index];
            vel[size++] = unit.segmentVelocities[index];
        }

        void set(MillipedeDefaultUnit unit){
            for(MillipedeSegmentUnit seg : units){
                if(seg == null) break;
                seg.trueParentUnit = unit;
            }
            unit.segmentUnits = new MillipedeSegmentUnit[size];
            unit.segments = new Vec2[size];
            unit.segmentVelocities = new Vec2[size];
            System.arraycopy(units, 0, unit.segmentUnits, 0, size);
            System.arraycopy(pos, 0, unit.segments, 0, size);
            System.arraycopy(vel, 0, unit.segmentVelocities, 0, size);
        }
    }
}