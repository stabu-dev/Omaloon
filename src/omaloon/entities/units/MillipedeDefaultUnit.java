package omaloon.entities.units;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.io.*;
import ent.anno.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.gen.*;
import mindustry.type.*;
import omaloon.type.*;
import omaloon.utils.*;

import java.util.*;

@Annotations.EntityPoint
public class MillipedeDefaultUnit extends UnitEntity{
    public GlasmoreUnitType wormType;
    public MillipedeSegmentUnit[] segmentUnits;
    public float repairTime = 0f;
    protected float attachTime = 4f * 60f;
    protected float healthDistributionEfficiency = 1f;
    protected Vec2[] segments, segmentVelocities;
    protected boolean addSegments = true;
    protected boolean found;
    protected final Interval scanTimer = new Interval();
    protected final Vec2 lastVelocityC = new Vec2(), lastVelocityD = new Vec2();

    public int getSegmentLength(){
        return wormType.segmentLength;
    }

    @Override
    public void type(UnitType type){
        super.type(type);
        if(type instanceof GlasmoreUnitType w) wormType = w;
        else throw new ClassCastException("you set this unit's type in a sneaky way");
    }

    @Override
    public void setType(UnitType type){
        super.setType(type);
        if(type instanceof GlasmoreUnitType w) wormType = w;
        else throw new ClassCastException("you set this unit's type in a sneaky way");
    }

    protected void setEffects(){
        segmentUnits = new MillipedeSegmentUnit[wormType.segmentLength];
        segments = new Vec2[wormType.segmentLength];
        segmentVelocities = new Vec2[wormType.segmentLength];
        for(int i = 0; i < getSegmentLength(); i++){
            segments[i] = new Vec2(x, y);
            segmentVelocities[i] = new Vec2();
        }
    }

    @Override
    public void damage(float amount){
        super.damage(amount);
        healthDistributionEfficiency = Mathf.clamp(healthDistributionEfficiency - (amount / 15f));
    }

    @Override
    public void update(){
        lastVelocityD.set(lastVelocityC);
        lastVelocityC.set(vel);
        super.update();
        healthDistributionEfficiency = Mathf.clamp(healthDistributionEfficiency + (Time.delta / 160f));
        updateSegmentVLocal(lastVelocityC);
        updateSegmentsLocal();
        if(wormType.chainable && segmentUnits.length < wormType.maxSegments && scanTimer.get(15f) && attachTime >= 4f * 60f){
            scanTailSegment();
        }
        attachTime += Time.delta;
        if(regenAvailable()){
            if(repairTime >= wormType.regenTime){
                float damage = (health / segmentUnits.length) / 2f;
                damage(damage);
                for(MillipedeSegmentUnit seg : segmentUnits){
                    float sDamage = (seg.segmentHealth / segmentUnits.length) / 2f;
                    seg.segmentDamage(sDamage);
                }
                addSegment();
                repairTime = 0f;
            }else{
                repairTime += Time.delta;
            }
        }
    }

    public boolean regenAvailable(){
        return wormType.splittable && (segmentUnits.length < wormType.segmentLength || segmentUnits.length < wormType.maxSegments) && wormType.regenTime > 0f;
    }

    protected void updateSegmentVLocal(Vec2 vec){
        int len = getSegmentLength();
        for(int i = 0; i < len; i++){
            Vec2 seg = segments[i];
            Vec2 segV = segmentVelocities[i];
            segV.limit(type.speed);
            float angleB = i != 0 ? Angles.angle(seg.x, seg.y, segments[i - 1].x, segments[i - 1].y) : Angles.angle(seg.x, seg.y, x, y);
            float velocity = i != 0 ? segmentVelocities[i - 1].len() : vec.len();

            Tmp.v1.set(vel);
            Tmp.v1.add(vec);
            Tmp.v1.add(lastVelocityD);
            Tmp.v1.scl(1f / 3f);

            float trueVel = Math.max(Math.max(velocity, segV.len()), Tmp.v1.len());
            Tmp.v1.trns(angleB, trueVel);
            segV.add(Tmp.v1);
            segV.setLength(trueVel);
            if(wormType.counterDrag) segV.scl(1f - drag);
            segmentUnits[i].vel.set(segV);
        }
        for(int i = 0; i < len; i++) segmentVelocities[i].scl(Time.delta);
    }

    protected void updateSegmentsLocal(){
        float segmentOffset = wormType.segmentOffset / 2f;
        float angleC = OlUtils.clampedAngle(Angles.angle(segments[0].x, segments[0].y, x, y), rotation, wormType.angleLimit) + 180f;
        Tmp.v1.trns(angleC, segmentOffset + wormType.headOffset);
        Tmp.v1.add(x, y);
        segments[0].set(Tmp.v1);
        int len = getSegmentLength();

        segments[0].add(segmentVelocities[0]);

        rotation -= OlUtils.angleDistSigned(rotation, segmentUnits[0].rotation, wormType.angleLimit) / 1.25f;
        Tmp.v1.trns(rotation + 180f, segmentOffset + wormType.headOffset).add(this);
        segmentUnits[0].rotation = OlUtils.clampedAngle(segments[0].angleTo(Tmp.v1), rotation, wormType.angleLimit);
        Tmp.v2.trns(segmentUnits[0].rotation, segmentOffset).add(segments[0]).sub(Tmp.v1);
        segments[0].sub(Tmp.v2);

        segmentVelocities[0].scl(Mathf.clamp(1f - (drag * Time.delta)));
        segmentUnits[0].set(segments[0].x, segments[0].y);
        segmentUnits[0].segmentUpdate();
        if(wormType.healthDistribution > 0) distributeHealth(0);

        for(int i = 1; i < len; i++){
            Vec2 seg = segments[i], segLast = segments[i - 1];
            MillipedeSegmentUnit segU = segmentUnits[i], segULast = segmentUnits[i - 1];

            seg.add(segmentVelocities[i]);

            segULast.rotation -= OlUtils.angleDistSigned(segULast.rotation, segU.rotation, wormType.angleLimit) / 1.25f;
            Tmp.v1.trns(segULast.rotation + 180f, segmentOffset).add(segLast);
            segU.rotation = OlUtils.clampedAngle(segU.angleTo(Tmp.v1), segULast.rotation, wormType.angleLimit);
            Tmp.v2.trns(segU.rotation, segmentOffset).add(seg).sub(Tmp.v1);
            seg.sub(Tmp.v2);

            segmentVelocities[i].scl(Mathf.clamp(1f - (drag * Time.delta)));
            segU.set(seg);
            segU.segmentUpdate();
            if(wormType.healthDistribution > 0) distributeHealth(i);
        }
        for(int i = 0; i < segmentUnits.length; i++){
            Vec2 seg = segments[i];
            Vec2 segV = segmentVelocities[i];
            MillipedeSegmentUnit segU = segmentUnits[i];
            seg.add(segV);
            float angleD = i == 0 ? Angles.angle(seg.x, seg.y, x, y) : Angles.angle(seg.x, seg.y, segments[i - 1].x, segments[i - 1].y);
            segV.scl(Mathf.clamp(1f - drag * Time.delta));
            segU.set(seg.x, seg.y);
            segU.rotation = angleD;
            segU.segmentUpdate();
            if(wormType.healthDistribution > 0) distributeHealth(i);
        }
    }

    protected void distributeHealth(int index){
        int idx = 0;
        float mHealth = 0f;
        float mMaxHealth = 0f;
        for(int i = -1; i < 2; i++){
            Unit seg = getSegment(i + index);
            if(seg == null) break;
            mHealth += seg.health;
            mMaxHealth += seg.maxHealth;
            idx++;
        }
        mMaxHealth /= idx;
        mHealth /= idx;
        for(int i = -1; i < 2; i++){
            Unit seg = getSegment(i + index);
            if(seg == null) break;
            if(seg instanceof MillipedeSegmentUnit ws){
                if(!Mathf.equal(ws.segmentHealth, mHealth, 0.001f)) ws.segmentHealth = Mathf.lerpDelta(ws.segmentHealth, mHealth, wormType.healthDistribution * healthDistributionEfficiency);
            }else{
                if(!Mathf.equal(seg.health, mHealth, 0.001f)) seg.health = Mathf.lerpDelta(seg.health, mHealth, wormType.healthDistribution * healthDistributionEfficiency);
            }
            if(!Mathf.equal(seg.maxHealth, mMaxHealth, 0.001f)) seg.maxHealth = Mathf.lerpDelta(seg.maxHealth, mMaxHealth, wormType.healthDistribution * healthDistributionEfficiency);
        }
    }

    protected Unit getSegment(int index){
        if(index < 0) return this;
        if(index >= segmentUnits.length) return null;
        return segmentUnits[index];
    }

    /*@Override
    public int classId(){
        return UnityEntityMapping.classId(WormDefaultUnit.class);
    }*/

    @Override
    public float clipSize(){
        return segmentUnits.length * wormType.segmentOffset * 2f;
    }

    public void drawShadow(){
        float originZ = Draw.z();
        for(int i = 0, len = segmentUnits.length; i < len; i++){
            Draw.z(originZ - (i + 1) / 10000f);
            segmentUnits[i].drawShadow();
        }
        Draw.z(originZ);
    }

    public MillipedeSegmentUnit newSegment(){
        return new MillipedeSegmentUnit();
    }

    @Override
    public void destroy(){
        if(!added) return;
        super.destroy();
        for(MillipedeSegmentUnit seg : segmentUnits){
            float explosiveness = 2f + seg.item().explosiveness * stack().amount * 1.53f;
            float flammability = seg.item().flammability * seg.stack().amount / 1.9f;
            float power = seg.item().charge * seg.stack().amount * 150f;

            if(!spawnedByCore){
                Damage.dynamicExplosion(seg.x, seg.y, flammability, explosiveness, power, bounds() / 2f, Vars.state.rules.damageExplosions, item().flammability > 1, team);
            }

            float shake = hitSize / 3f;

            Effect.scorch(seg.x, seg.y, (int)(hitSize / 5));
            Fx.explosion.at(seg);
            Effect.shake(shake, shake, seg);
            type.deathSound.at(seg);

            if(type.flying && !spawnedByCore){
                Damage.damage(team, seg.x, seg.y, Mathf.pow(seg.hitSize, 0.94f) * 1.25f, Mathf.pow(seg.hitSize, 0.75f) * type.crashDamageMultiplier * 5f, true, false, true);
            }

            if(!Vars.headless){
                for(int i = 0; i < type.wreckRegions.length; i++){
                    if(type.wreckRegions[i].found()){
                        float range = type.hitSize / 4f;
                        Tmp.v1.rnd(range);
                        Effect.decal(type.wreckRegions[i], seg.x + Tmp.v1.x, seg.y + Tmp.v1.y, seg.rotation - 90);
                    }
                }
            }
        }
    }

    @Override
    public void remove(){
        if(!added) return;
        super.remove();
        for(MillipedeSegmentUnit segmentUnit : segmentUnits){
            segmentUnit.remove();
        }
    }

    protected void superRemove(){
        super.remove();
    }

    @Override
    public int count(){
        return Math.max(super.count() / Math.max(wormType.segmentLength, wormType.maxSegments), 1);
    }

    protected void scanTailSegment(){
        Tmp.v1.trns(rotation, wormType.segmentOffset).add(this);
        float size = wormType.hitSize / 2f;
        found = false;
        Units.nearby(team, Tmp.v1.x - size, Tmp.v1.y - size, size * 2f, size * 2f, e -> {
            if(found) return;
            if(e instanceof MillipedeSegmentUnit ms && ms.segmentType == 1 && ms.millipedeType == wormType && ms.trueParentUnit != this && within(ms, (wormType.segmentOffset) + 5f) && Angles.within(angleTo(e), e.rotation, wormType.angleLimit + 2f)){
                if(ms.trueParentUnit == null || ms.trueParentUnit.segmentUnits.length > wormType.maxSegments) return;
                wormType.chainSound.at(this, Mathf.random(0.9f, 1.1f));
                MillipedeSegmentUnit head = newSegment();
                head.setType(wormType);
                head.set(this);
                head.rotation = rotation;
                head.vel.set(vel);
                head.team = team;
                head.maxHealth = maxHealth;
                head.health = head.segmentHealth = health;
                head.segmentType = 0;
                segmentUnits[0].parentUnit = head;
                head.add();
                superRemove();

                MillipedeSegmentUnit.SegmentData data = new MillipedeSegmentUnit.SegmentData(segmentUnits.length + 1);
                data.add(head, head.vel);

                for(int i = 0; i < segmentUnits.length; i++){
                    data.add(this, i);
                }
                for(int i = 0; i < data.size; i++){
                    ms.trueParentUnit.addSegment(data.units[i], data.pos[i], data.vel[i]);
                }
                found = true;
            }
        });
    }

    protected void removeTail(){
        int index = segments.length - 1;
        if(index <= 0) return;

        segmentUnits[index].remove();
        segmentUnits[index] = null;
        segmentUnits[index - 1].segmentType = 1;

        segmentUnits = Arrays.copyOf(segmentUnits, segmentUnits.length - 1);
        segments = Arrays.copyOf(segments, segments.length - 1);
        segmentVelocities = Arrays.copyOf(segmentVelocities, segmentVelocities.length - 1);
    }

    public void addSegment(MillipedeSegmentUnit unit, Vec2 pos, Vec2 vel){
        int index = segments.length;
        Unit parent = segmentUnits[index - 1];
        segmentUnits[index - 1].segmentType = 0;
        segmentUnits = Arrays.copyOf(segmentUnits, segmentUnits.length + 1);
        segments = Arrays.copyOf(segments, segments.length + 1);
        segmentVelocities = Arrays.copyOf(segmentVelocities, segmentVelocities.length + 1);

        unit.elevation = elevation;
        unit.segmentType = 1;
        unit.parentUnit = parent;
        unit.trueParentUnit = this;

        segmentUnits[segmentUnits.length - 1] = unit;
        segments[segments.length - 1] = pos;
        segmentVelocities[segmentVelocities.length - 1] = vel;
    }

    public void addSegment(){
        int index = segments.length;
        Unit parent = segmentUnits[index - 1];
        Tmp.v1.trns(segmentUnits[index - 1].rotation + 180f, wormType.segmentOffset).add(segmentUnits[index - 1]);
        segmentUnits[index - 1].segmentType = 0;
        segmentUnits = Arrays.copyOf(segmentUnits, segmentUnits.length + 1);
        segments = Arrays.copyOf(segments, segments.length + 1);
        segmentVelocities = Arrays.copyOf(segmentVelocities, segmentVelocities.length + 1);

        MillipedeSegmentUnit segment = newSegment();
        segment.elevation = elevation;
        segment.segmentType = 1;
        segment.setType(type);
        segment.parentUnit = parent;
        segment.trueParentUnit = this;
        segment.set(Tmp.v1);
        segment.team = team;
        segment.health = health;
        segment.maxHealth = maxHealth;
        segment.segmentHealth = health;
        segment.dead = false;
        segment.add();
        segmentUnits[segmentUnits.length - 1] = segment;
        segments[segments.length - 1] = new Vec2(Tmp.v1);
        segmentVelocities[segmentVelocities.length - 1] = new Vec2(segmentVelocities[segmentVelocities.length - 2]);
    }

    @Override
    public void add(){
        if(added) return;
        super.add();
        if(!addSegments){
            postAdd();
            return;
        }
        setEffects();
        Unit parent = this;
        for(int i = 0, len = getSegmentLength(); i < len; i++){
            int typeS = i == len - 1 ? 1 : 0;
            segments[i].set(x, y);
            MillipedeSegmentUnit temp = newSegment();

            temp.elevation = elevation;
            temp.setSegmentType(typeS);
            temp.type(type);
            temp.resetController();
            temp.team = team;
            temp.setTrueParent(this);
            temp.setParent(parent);
            temp.add();
            temp.afterSync();
            temp.heal();
            parent = temp;
            segmentUnits[i] = temp;
        }
    }

    void postAdd(){
        for(MillipedeSegmentUnit ms : segmentUnits){
            ms.add();
        }
    }

    @Override
    public void read(Reads read){
        super.read(read);
        addSegments = false;
        int length = read.s();
        boolean splittable = read.bool();
        repairTime = read.f();

        segmentUnits = new MillipedeSegmentUnit[length];
        segments = new Vec2[length];
        segmentVelocities = new Vec2[length];

        Unit parent = this;
        for(int i = 0; i < length; i++){
            segments[i] = new Vec2();
            segmentVelocities[i] = new Vec2();
            MillipedeSegmentUnit temp = newSegment();
            temp.elevation = elevation;
            temp.type(type);
            temp.team = team;
            temp.drag = type.drag;
            temp.armor = type.armor;
            temp.hitSize = type.hitSize;
            temp.hovering = type.hovering;
            temp.setupWeapons(type);
            temp.resetController();
            temp.abilities = type.abilities.toArray(Ability.class);
            temp.setTrueParent(this);
            temp.setParent(parent);

            temp.x = segments[i].x = read.f();
            temp.y = segments[i].y = read.f();
            temp.rotation = read.f();
            temp.segmentType = read.b();
            if(splittable){
                temp.segmentHealth = temp.health = read.f();
                temp.maxHealth = read.f();
            }

            parent = temp;
            segmentUnits[i] = temp;
        }
    }

    @Override
    public void write(Writes write){
        super.write(write);

        write.s(segmentUnits.length);
        write.bool(wormType.splittable);
        write.f(repairTime);

        for(int i = 0; i < segmentUnits.length; i++){
            write.f(segments[i].x);
            write.f(segments[i].y);
            write.f(segmentUnits[i].rotation);
            write.b(segmentUnits[i].segmentType);
            if(wormType.splittable){
                write.f(segmentUnits[i].segmentHealth);
                write.f(segmentUnits[i].maxHealth);
            }
        }
    }

    /* seems uselss because multiple setStats() does nothing at end.
    @Override
    public void read(Reads read){
    	super.read(read);
    	for(int i = 0, len = getSegmentLength(); i < len; i++){
    		segments[i].x = read.f();
    		segments[i].y = read.f();
    	}
    }

    @Override
    public void write(Writes write){
    	super.write(write);
    	for(int i = 0, len = getSegmentLength(); i < len; i++){
    		write.f(segments[i].x);
    		write.f(segments[i].y);
    	}
    }*/

    public void handleCollision(Hitboxc originUnit, Hitboxc other, float x, float y){

    }
}
