package omaloon.entities.comp;

import arc.func.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import ent.anno.Annotations.*;
import mindustry.entities.*;
import mindustry.entities.EntityCollisions.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;
import omaloon.gen.*;
import omaloon.type.*;
import omaloon.utils.*;

@SuppressWarnings({"unused", "UnnecessaryReturnStatement"})
@EntityComponent
abstract class MillipedeComp implements Unitc, Legsc {
    private static Unit last;
    transient Unit head, parent, child, tail;
    transient float layer = 0f, scanTime = 0f;
    transient byte weaponIdx = 0;
    transient boolean removing = false, saveAdd = false;

    protected float splitHealthDiv = 1f;
    protected float regenTime = 0f;
    protected float waitTime = 0f;

    @SyncLocal public int childId = -1, headId = -1;
    @Import UnitType type;
    @Import float healthMultiplier, health, x, y, elevation, rotation, baseRotation;
    @Import boolean dead, isShooting;
    @Import Leg[] legs;
    @Import WeaponMount[] mounts;
    @Import Team team;

    @Override
    public void add(){
        MillipedeUnitType uType = (MillipedeUnitType)type;
        Unit current = self();
        if(isHead()){
            if(saveAdd){
                var seg = (Unit & Millipedec)child;
                while(seg != null){
                    seg.add();
                    seg.setupWeapons(uType);
                    seg = (Unit & Millipedec)seg.child();
                }
                saveAdd = false;
                return;
            } else {
                ((Millipedec) addTail()).addTail();
            }
            float[] rot = {rotation() + uType.angleLimit};
            Tmp.v1.trns(rot[0] + 180f, uType.segmentOffset + uType.headOffset).add(self());
            distributeActionBack(u -> {
                if(u != self()){
                    u.x = Tmp.v1.x;
                    u.y = Tmp.v1.y;
                    u.rotation = rot[0];

                    rot[0] += uType.angleLimit;
                    Tmp.v2.trns(rot[0] + 180f, uType.segmentOffset);
                    Tmp.v1.add(Tmp.v2);

                    u.add();
                    u.setupWeapons(uType);
                }
            });
        }
    }

    Unit addTail(){
        if(isHead()) head = self();
        if(!isTail()) return null;
        Unit tail = type.constructor.get();
        tail.team = team;
        tail.setType(type);
        tail.ammo = type.ammoCapacity;
        tail.elevation = type.flying ? 1f : 0;
        tail.heal();

        MillipedeUnitType uType = (MillipedeUnitType)type;
        if(tail instanceof Millipedec){
            float z = layer + uType.segmentLayerOffset;
            Tmp.v1.trns(rotation() + 180f, uType.segmentOffset).add(self());
            tail.set(Tmp.v1);
            ((Millipedec)tail).layer(z);
            ((Millipedec)tail).head(head);
            ((Millipedec)tail).parent(self());
            child = tail;
            tail.setupWeapons(uType);
            tail.add();
            ((Millipedec) tail).distributeActionForward(u -> u.setupWeapons(uType));
        }
        return tail;
    }

    @Override
    public void afterSync(){
        if(headId != -1 && head == null){
            Unit h = Groups.unit.getByID(headId);
            if(h instanceof Millipedec wc){
                head = h;
                headId = -1;
            }
        }
        if(childId != -1 && child == null){
            Unit c = Groups.unit.getByID(childId);
            if(c instanceof Millipedec wc){
                child = c;
                wc.parent(self());
                childId = -1;
            }
        }
    }

    @Replace
    @Override
    public void aim(float x, float y) {
			if (isHead()) distributeActionBack(u -> {
		    Tmp.v1.set(x, y).sub(this.x, this.y);
		    if (Tmp.v1.len() < type.aimDst) Tmp.v1.setLength(type.aimDst);
        float
		    tx = Tmp.v1.x + this.x,
		    ty = Tmp.v1.y + this.y;

		    for (WeaponMount mount : u.mounts) if (mount.weapon.controllable) {
          mount.aimX = tx;
          mount.aimY = ty;
        }

		    u.aimX = tx;
		    u.aimY = ty;
	    });
    }

    @Replace
    @Override
    public int cap(){
        int max = Math.max(((MillipedeUnitType)type).maxSegments, ((MillipedeUnitType)type).segmentLength);
        return Math.max(Units.getCap(team), Units.getCap(team) * max);
    }

    boolean canJoin(Unit other) {
        if (!(other instanceof Millipedec snek)) return false;
        MillipedeUnitType uType = (MillipedeUnitType)type;

        return uType == other.type() && snek.countAll() + countAll() <= uType.maxSegments;
    }

    public void connect(Unit other){
        if(other instanceof Millipedec snek && isHead() && snek.isTail()){
            MillipedeUnitType uType = (MillipedeUnitType) type;
            float z = snek.layer() + uType.segmentLayerOffset;
            distributeActionBack(u -> {
                u.layer(z);
                u.head(snek.head());
            });
            snek.child(self());
            parent = other;
            head = snek.head();
            ((Millipedec) head).distributeActionBack(u -> u.setupWeapons(type));
            uType.chainSound.at(self());
            if(controller() instanceof Player){
                UnitController con = controller();
                snek.head().controller(con);
                con.unit(snek.head());
                controller(type.createController(self()));
            }
        }
    }

    @MethodPriority(-1)
    @Override
    @BreakAll
    public void controller(UnitController next){
        if(next instanceof Player && head != null && !isHead()){
            head.controller(next);
            return;
        }
    }

    @Override
    @Replace
    public void controlWeapons(boolean rotate, boolean shoot) {
        if (isHead()) distributeActionBack((unit) -> {
            for(WeaponMount mount : unit.mounts) {
                if (mount.weapon.controllable) {
                    mount.rotate = rotate;
                    mount.shoot = shoot;
                }
            }

            unit.isShooting = shoot;
        });
    }

    /**
     * counts the number of units towards the tail
     */
    int countBackward(){
        Millipedec current = self();
        int num = 0;
        while(current != null && current.child() != null){
            if(current.child() instanceof Millipedec){
                num++;
                current = (Millipedec)current.child();
            }else{
                current = null;
            }
        }
        return num;
    }
    /**
     * counts the number of units in this snake, including itself
     */
    int countAll() {
        return countBackward() + countForward() + 1;
    }
    /**
     * counts the number of units towards the head
     */
    int countForward(){
        Millipedec current = self();
        int num = 0;
        while(current != null && current.parent() != null){
            if(current.parent() instanceof Millipedec){
                num++;
                current = (Millipedec)current.parent();
            }else{
                current = null;
            }
        }
        return num;
    }

    @Replace
    @MethodPriority(-2)
    @Override
    @BreakAll
    public void damage(float amount){
        if(!isHead() && head != null && !((MillipedeUnitType)type).splittable){
            head.damage(amount);
            return;
        }
    }

    /**
     * runs a consumer with every unit towards the tail
     */
    <T extends Unit & Millipedec> void distributeActionBack(Cons<T> cons){
        T current = as();
        cons.get(current);
        while(current.child() != null){
            cons.get(current.child().as());
            current = current.child().as();
        }
    }

    /**
     * runs a consumer with every unit towards the head
     */
    <T extends Unit & Millipedec> void distributeActionForward(Cons<T> cons){
        T current = as();
        cons.get(current);
        while(current.parent() != null){
            cons.get(current.parent().as());
            current = current.parent().as();
        }
    }

    @MethodPriority(-1)
    @Override
    @BreakAll
    public void heal(float amount){
        if(!isHead() && head != null && !((MillipedeUnitType)type).splittable){
            head.heal(amount);
            return;
        }
    }

    @Override
    @Replace
    public TextureRegion icon(){
        MillipedeUnitType uType = (MillipedeUnitType)type;

        if(isHead() && isTail()) return type.fullIcon;
        if(isTail()) return uType.tailOutline;
        if(!isHead()) return uType.segmentOutline;
        return type.fullIcon;
    }

    boolean isHead(){
        return parent == null || head == self();
    }
    boolean isSegment() {
        return !isHead() && !isTail();
    }
    boolean isTail(){
        return child == null;
    }

    @Replace
    @Override
    public boolean isAI(){
        if(head != null && !isHead()) return head.isAI();
        return controller() instanceof AIController;
    }

    @MethodPriority(100)
    @Override
    public void read(Reads read){
        if(read.bool()){
            MillipedeUnitType uType = (MillipedeUnitType)type;
            saveAdd = true;
            int seg = read.s();
            Millipedec current = self();
            for(int i = 0; i < seg; i++){
                Unit u = type.constructor.get();
                Millipedec w = (Millipedec)u;
                current.child(u);
                w.parent((Unit)current);
                w.head(self());
                w.layer(layer + uType.segmentLayerOffset * i);
                w.weaponIdx(read.b());
                u.read(read);
                current = w;
            }
        }
    }

    @Override
    @BreakAll
    public void remove(){
        MillipedeUnitType uType = (MillipedeUnitType)type;
        if(uType.splittable){
            if(child != null && parent != null) uType.splitSound.at(x(), y());
            if(child != null){
                var wc = (Unit & Millipedec)child;
                float z = 0f;
	              wc.parent(null);
                wc.distributeActionBack(u -> u.setupWeapons(uType));
                while(wc != null){
                    wc.layer(z += uType.segmentLayerOffset);
                    wc.splitHealthDiv(wc.splitHealthDiv() * 2f);
                    wc.head(child);
                    if(wc.isTail()) wc.waitTime(5f * 60f);
                    wc = (Unit & Millipedec)wc.child();
                }
            }
            if(parent != null) {
                Millipedec wp = ((Millipedec)parent);
                distributeActionForward(u -> {
                    u.setupWeapons(uType);
                    if(u != self()){
                        u.splitHealthDiv(u.splitHealthDiv() * 2f);
                    }
                });
                wp.child(null);
                wp.waitTime(5f * 60f);
            }
            parent = null;
            child = null;
        }
        if(!isHead() && !uType.splittable && !removing){
            head.remove();
            return;
        }
        if(isHead() && !uType.splittable){
            distributeActionBack(u -> {
                if(u != self()){
                    u.removing(true);
                    u.remove();
                    u.removing(false);
                }
            });
        }
		    parent = null;
		    child = null;
    }

    @Replace
    @Override
    public void resetLegs(float legLength) {
        MillipedeUnitType uType = (MillipedeUnitType)type;
        int count = 0;
        if ((isHead() && isTail()) || isSegment()) {
            count = uType.segmentLegCount;
        } else {
            if (isHead()) count = uType.headLegCount;
            if (isTail()) count = uType.tailLegCount;
        }

        if (legs.length == count) return;

        legs = new Leg[count];
        if (type.lockLegBase) {
            baseRotation = rotation;
        }

        for(int i = 0; i < legs.length; ++i) {
            Leg l = new Leg();
            float dstRot = this.legAngle(i);
            Vec2 baseOffset = this.legOffset(Tmp.v5, i).add(this.x, this.y);
            l.joint.trns(dstRot, legLength / 2.0F).add(baseOffset);
            l.base.trns(dstRot, legLength).add(baseOffset);
            legs[i] = l;
        }

    }

    /**
     * only heads will be written
     */
    @Override
    public boolean serialize(){
        return isHead();
    }

    @Replace(1)
    @Override
    public void setupWeapons(UnitType def) {
        MillipedeUnitType uType = (MillipedeUnitType)def;
        if ((isTail() && uType.tailHasWeapon) || (isHead() && uType.headHasWeapon) || isSegment()) {
            Seq<Weapon> seq = uType.segmentWeapons[Math.min(uType.segmentWeapons.length - 1, countForward())];
            mounts = new WeaponMount[seq.size];
            for (int i = 0; i < mounts.length; i++) {
                mounts[i] = seq.get(i).mountType.get(seq.get(i));
            }
        } else {
            mounts = new WeaponMount[] {};
        }
    }

    @Replace(1)
    @Override
    public SolidPred solidity() {
        if (!isHead()) return null;

        return type.allowLegStep ? EntityCollisions::legsSolid : EntityCollisions::solid;
    }

    @Replace
    @Override
    public boolean moving(){
        if(!isHead()) return head.moving();
        return !vel().isZero(0.01f);
    }

    @Replace
    @Override
    public float speed(){
        if(!isHead()) return 0f;
        float strafePenalty = isGrounded() || !isPlayer() ? 1f : Mathf.lerp(1f, type.strafePenalty, Angles.angleDist(vel().angle(), rotation) / 180f);
        float boost = Mathf.lerp(1f, type.canBoost ? type.boostMultiplier : 1f, elevation);
        return type.speed * strafePenalty * boost * floorSpeedMultiplier();
    }

    @Override
    public void update(){
        MillipedeUnitType uType = (MillipedeUnitType)type;
        if (countAll() < 3) kill();
        if(uType.splittable && isTail() && uType.regenTime > 0f){
            int forward = countForward();
            if(forward < uType.segmentLength - 1){
                regenTime += Time.delta;
                if(regenTime >= uType.regenTime){
                    regenTime = 0f;
                    Unit unit;
                    if((unit = addTail()) != null){
                        health /= 2f;
                        unit.health = health;
                        ((MillipedeUnitType)type).chainSound.at(self());
                    }
                }
            }
        }else regenTime = 0f;
        if(isTail() && waitTime > 0) waitTime -= Time.delta;
        if(!uType.splittable){
            if(!isHead()) health = head.health;
            if((isHead() && isAdded()) || (head != null && head.isAdded())){
                Millipedec t = (Millipedec)child;
                while(t != null && !t.isAdded()){
                    t.add();
                    t = (Millipedec)t.child();
                }
            }
        }
        if(uType.splittable && (parent != null || child != null) && dead){
            destroy();
        }
    }

    @Wrap(value = "update()", block = Boundedc.class)
    boolean updateBounded(){
        return isHead();
    }

    @Insert(value = "update()", block = Statusc.class)
    private void updateHealthDiv(){
        healthMultiplier /= splitHealthDiv;
    }

    @Insert("update()")
    private void updatePost(){
        if(isHead()){
            MillipedeUnitType uType = (MillipedeUnitType)type;
            last = self();
            distributeActionBack(u -> {
                if(u == self()) return;

                u.aim(aimX(), aimY());

                float offset = self() == last ? uType.headOffset : 0f;
                Tmp.v1.trns(last.rotation + 180f, (uType.segmentOffset / 2f) + offset).add(last);

                float rdx = u.deltaX - last.deltaX;
                float rdy = u.deltaY - last.deltaY;

                float angTo = !uType.preventDrifting || (last.deltaLen() > 0.001f && (rdx * rdx) + (rdy * rdy) > 0.00001f) ? u.angleTo(Tmp.v1) : u.rotation;

                u.rotation = angTo - (OlUtils.angleDistSigned(angTo, last.rotation, uType.angleLimit) * (1f - uType.anglePhysicsSmooth));
                u.trns(Tmp.v3.trns(u.rotation, last.deltaLen()));
                Tmp.v2.trns(u.rotation, uType.segmentOffset / 2f).add(u);

                Tmp.v2.sub(Tmp.v1).scl(Mathf.clamp(uType.jointStrength * Time.delta));

                Unit n = u;
                int cast = uType.segmentCast;
                while(cast > 0 && n != null){
                    float scl = cast / (float)uType.segmentCast;
                    n.set(n.x - (Tmp.v2.x * scl), n.y - (Tmp.v2.y * scl));
                    n.updateLastPosition();
                    n = ((Millipedec)n).child();
                    cast--;
                }

                float nextHealth = (last.health() + u.health()) / 2f;
                if(!Mathf.equal(nextHealth, last.health(), 0.0001f)) last.health(Mathf.lerpDelta(last.health(), nextHealth, uType.healthDistribution));
                if(!Mathf.equal(nextHealth, u.health(), 0.0001f)) u.health(Mathf.lerpDelta(u.health(), nextHealth, uType.healthDistribution));

                Millipedec wrm = ((Millipedec)last);
                float nextHealthDv = (wrm.splitHealthDiv() + u.splitHealthDiv()) / 2f;
                if(!Mathf.equal(nextHealth, wrm.splitHealthDiv(), 0.0001f)) wrm.splitHealthDiv(Mathf.lerpDelta(wrm.splitHealthDiv(), nextHealthDv, uType.healthDistribution));
                if(!Mathf.equal(nextHealth, u.splitHealthDiv(), 0.0001f)) u.splitHealthDiv(Mathf.lerpDelta(u.splitHealthDiv(), nextHealthDv, uType.healthDistribution));
                last = u;
            });
            scanTime += Time.delta;
            if(scanTime >= 5f && uType.chainable){
                Tmp.v1.trns(rotation(), uType.segmentOffset / 2f).add(self());
                Tmp.r1.setCentered(Tmp.v1.x, Tmp.v1.y, hitSize());
                Units.nearby(Tmp.r1, u -> {
                    if(u.team == team && u.type == type && u instanceof Millipedec m && m.head() != self() && m.isTail() && m.countForward() + countBackward() < uType.maxSegments && m.waitTime() <= 0f && within(u, uType.segmentOffset) && OlUtils.angleDist(rotation(), angleTo(u)) < uType.angleLimit){
                        connect(u);
                    }
                });
                scanTime = 0f;
            }
        }
    }

    @Replace
    @Override
    public void wobble(){

    }

    @MethodPriority(100)
    @Override
    public void write(Writes write){
        write.bool(isHead());
        if(isHead()){
            Millipedec ch = (Millipedec)child;
            int amount = 0;
            while(ch != null){
                amount++;
                ch = (Millipedec)ch.child();
            }
            write.s(amount);

            ch = (Millipedec)child;
            while(ch != null){
                write.b(weaponIdx);
                ch.write(write);
                ch = (Millipedec)ch.child();
            }
        }
    }
}
