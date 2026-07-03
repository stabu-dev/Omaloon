package omaloon.entities.comp;

import arc.func.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.*;
import mindustry.ai.types.*;
import mindustry.gen.*;
import mindustry.type.*;
import omaloon.annotations.Annotations.*;
import omaloon.gen.*;
import omaloon.type.*;

import java.util.*;

@SuppressWarnings("unused")
@EntityComponent
abstract class ChainedComp implements Unitc{
    @Import
    public UnitType type;
    @Import
    public boolean dead;
    @Import
    public float baseRotation;

    transient Chainedc head, parent, child, tail;
    transient int segment;

    int parentId = -1;

    transient float startX = -1, startY = -1;
    transient float exitAngle = 0;
    transient int segmentsSpawned = 0;
    transient boolean isExiting = false;
    transient int totalSegments = -1;
    transient Vec2 lastSyncPos;
    transient boolean loadedFromSave = false;

    public boolean isExiting(){
        return head != null && head != self() ? head.isExiting() : isExiting;
    }

    @Replace
    public void display(Table table){
        (head != null ? head.type() : type).display(self(), table);
    }

    @Override
    public void afterRead(){
        loadedFromSave = true;
    }

    @Override
    public void add(){
        head = self();
        tail = self();

        if(!Vars.net.client() && parent == null && !loadedFromSave && type instanceof GlasmoreUnitType g && g.segmentUnit != null){
            Unit u = self();
            startX = u.x;
            startY = u.y;
            segmentsSpawned = 0;
            isExiting = true;
            exitAngle = u.rotation;

            int count = 0;
            for(Chainedc n = child; n != null; n = n.child()) count++;
            totalSegments = count > 0 ? count : g.segmentUnits - 1;
        }
    }

    @Override
    public void afterReadAll(){
        loadParent();
    }

    @Override
    public void afterSync(){
        loadParent();
    }

    @Override
    public void beforeWrite(){
        parentId = parent != null ? parent.id() : -1;
    }

    public int chainLength(){
        int size = 0;
        for(Chainedc n = head; n != null; n = n.child()) size++;
        return size;
    }

    public void connect(Unit to){
        connect(to, false);
    }

    public void connect(Unit to, boolean force){
        if(!force && isExiting()) return;
        if(to instanceof Chainedc chained && chained.head() != head){
            Chainedc toHead = chained.head();
            if(toHead == null) return;

            if(toHead.isExiting()){
                ChainedMechUnit ourHead = (ChainedMechUnit)head;
                ChainedMechUnit incomingHead = (ChainedMechUnit)toHead;
                ourHead.isExiting = true;
                ourHead.startX = ourHead.x;
                ourHead.startY = ourHead.y;
                ourHead.exitAngle = ourHead.rotation;
                ourHead.totalSegments = incomingHead.totalSegments - incomingHead.segmentsSpawned;
                ourHead.segmentsSpawned = 0;
                incomingHead.isExiting = false;
            }

            tail.child(toHead);
            toHead.parent(tail);

            tail.propagateUp(s -> s.tail(chained.tail()));
            toHead.propagateDown(s -> s.head(head));

            head.propagateDown(s -> {
                if(s == head) return;
                UnitType h = head.type();
                UnitType target = s == head.tail() ? h.segmentEndUnit : h.segmentUnit;
                UnitType prev = s.type();
                s.type(target == null ? h : target);
                if(prev != s.type()) s.setupWeapons(s.type());
            });
        }
    }

    public void loadParent(){
        int cur = parent == null ? -1 : parent.id();
        if(parentId != cur){
            if(parentId == -1){
                splitTop();
            }else{
                Unit p = Groups.unit.getByID(parentId);
                if(p instanceof Chainedc chained){
                    if(parent != null) splitTop();
                    chained.connect(self());
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Chainedc> void propagateDown(Cons<T> run){
        for(Chainedc n = self(); n != null; n = n.child()) run.get((T)n);
    }

    @SuppressWarnings("unchecked")
    public <T extends Chainedc> void propagateUp(Cons<T> run){
        for(Chainedc n = self(); n != null; n = n.parent()) run.get((T)n);
    }

    @Override
    public void remove(){
        boolean wasDead = dead;
        Chainedc p = parent, c = child;
        UnitType hType = head.type();

        splitTop();
        splitBottom();

        if(wasDead){
            if(p != null){
                Chainedc h = p.head();
                if(h.type() instanceof GlasmoreUnitType g && g.killSmallChains && h.chainLength() < g.segmentUnits){
                    h.kill();
                }
            }
            if(c != null && hType instanceof GlasmoreUnitType g){
                if(!g.splittable || (g.killSmallChains && c.chainLength() < g.segmentUnits)){
                    c.kill();
                }
            }
        }
    }

    public void splitBottom(){
        if(child == null) return;
        Chainedc c = child, oldHead = head;
        child = null;
        c.parent(null);
        c.propagateDown(s -> s.head(c));
        propagateUp(s -> s.tail(self()));
        if(oldHead != null && oldHead.type() instanceof GlasmoreUnitType h){
            c.type(h);
            c.setupWeapons(h);
        }
    }

    public void splitTop(){
        if(parent == null) return;
        Chainedc p = parent, oldHead = head;
        parent = null;
        p.child(null);
        p.propagateUp(s -> s.tail(p));
        propagateDown(s -> s.head(self()));
        if(oldHead != null && oldHead.type() instanceof GlasmoreUnitType h){
            type = h;
            setupWeapons(h);
        }
    }

    @Replace
    public void rotateMove(Vec2 vec){
        if(head != null && head != self()){
            if(((Unit)self()).isPlayer()) head.rotateMove(vec);
            return;
        }
        float len = vec.len();
        if(len < 0.001f) return;
        float ang = vec.angle();
        moveAt(Tmp.v2.trns(baseRotation, len * Mathf.clamp(Mathf.cosDeg(Angles.angleDist(baseRotation, ang)))));
        baseRotation = Angles.moveToward(baseRotation, ang, type.rotateSpeed * Time.delta);
    }

    @Replace
    public void moveAt(Vec2 vec){
        if(head == null || head == self()) ((Unitc)self()).moveAt(vec, type.accel);
    }

    @Insert("update()")
    public void updateChain(){
        Unit u = self();
        if(head != null && head != u && !dead){
            if(u.isPlayer()){
                head.propagateDown(s -> {
                    Unit su = (Unit)s;
                    if(su.controller() instanceof CommandAI ai){
                        if(ai.hasCommand()) ai.command(null);
                        ai.targetPos = null;
                        ai.attackTarget = null;
                    }
                    su.lastCommanded = null;
                });
            }
        }

        if(head == self()){
            CommandAI hai = u.controller() instanceof CommandAI ? (CommandAI)u.controller() : null;
            if(hai != null){
                boolean changed = !Objects.equals(hai.targetPos, lastSyncPos);
                if(!changed){
                    for(Chainedc s = child; s != null; s = s.child()){
                        if(s.controller() instanceof CommandAI ai && ai.hasCommand()){
                            if(!Objects.equals(ai.targetPos, hai.targetPos)){
                                hai.clearCommands();
                                hai.command(ai.command);
                                if(ai.attackTarget != null) hai.commandTarget(ai.attackTarget);
                                else if(ai.targetPos != null) hai.commandPosition(ai.targetPos);
                                break;
                            }
                        }
                    }
                }

                for(Chainedc s = child; s != null; s = s.child()){
                    if(s.controller() instanceof CommandAI ai){
                        if(hai.hasCommand()){
                            if(ai.command != hai.command || ai.attackTarget != hai.attackTarget || !Objects.equals(ai.targetPos, hai.targetPos)){
                                ai.clearCommands();
                                ai.command(hai.command);
                                if(hai.attackTarget != null) ai.commandTarget(hai.attackTarget);
                                else if(hai.targetPos != null) ai.commandPosition(hai.targetPos);
                            }
                        }else if(ai.hasCommand()){
                            ai.clearCommands();
                            ai.command(null);
                        }
                    }
                }
                lastSyncPos = hai.targetPos;
            }
        }

        if(!Vars.net.client() && head != null && !head.isExiting() && head.type() instanceof GlasmoreUnitType headType){
            if(headType.killSmallChains && chainLength() < headType.segmentUnits){
                if(dead) Call.unitDestroy(id());
                else Call.unitDespawn(self());
            }
        }

        if(parent != null && self() instanceof Mechc && !type.omniMovement) baseRotation = rotation();

        if(head != self() || !(type instanceof GlasmoreUnitType g)) return;

        Chainedc tailNode = tail;
        for(Chainedc curr = self(); curr != null; curr = curr.child()){
            UnitType target = curr == self() ? g : (curr == tailNode && g.segmentEndUnit != null ? g.segmentEndUnit : (g.segmentUnit != null ? g.segmentUnit : g));
            if(curr.type() != target){
                curr.type(target);
                curr.setupWeapons(target);
            }
        }

        if(isExiting && !Vars.net.client()){
            int total = totalSegments == -1 ? g.segmentUnits - 1 : totalSegments;

            if(segmentsSpawned < total){
                moveAt(Tmp.v1.trns(exitAngle, type.speed * 1.5f));

                if(Mathf.dst(u.x, u.y, startX, startY) >= (segmentsSpawned + 1) * g.segmentSpacing){
                    Chainedc targetSegment = null;
                    for(Chainedc curr = child; curr != null; curr = curr.child()){
                        if(!curr.isAdded()){
                            targetSegment = curr;
                            break;
                        }
                    }

                    boolean isNew = targetSegment == null;
                    if(isNew){
                        UnitType segT = (segmentsSpawned == total - 1 && g.segmentEndUnit != null) ? g.segmentEndUnit : g.segmentUnit;
                        targetSegment = (Chainedc)segT.create(u.team);
                    }

                    Unit segment = (Unit)targetSegment;
                    segment.set(((Unit)tail).x, ((Unit)tail).y);
                    segment.rotation(exitAngle);
                    if(segment instanceof Mechc m) m.baseRotation(exitAngle);
                    segment.add();

                    if(isNew) connect(segment, true);
                    if(++segmentsSpawned == total) isExiting = false;
                }
            }else{
                isExiting = false;
            }
        }

        if(child == null) return;

        float t = angleTo(child) - 180f;
        if(isExiting){
            rotation(exitAngle);
            if(self() instanceof Mechc m) m.baseRotation(exitAngle);
        }else{
            float intent = self() instanceof Mechc m ? m.baseRotation() : rotation();
            rotation(Angles.moveToward(rotation(), Angles.clampRange(intent, t, g.segmentRotationRange), type.rotateSpeed * Time.delta));
        }

        if(vel().len() > 0.01f && !isExiting) vel().scl(Mathf.cosDeg(Angles.angleDist(rotation(), t) * (g.segmentRotationRange / 180f)));

        segment = 0;
        for(Chainedc s = child; s != null; s = s.child()){
            Chainedc p = s.parent();
            UnitType st = s == tail ? s.type() : p.type();
            float a = Angles.moveToward(s.rotation() - 180f, Angles.clampRange(p.angleTo(s), p.rotation() + 180f, st.segmentRotationRange), st.rotateSpeed * Time.delta);
            s.move(Tmp.v1.trns(a, st.segmentSpacing).add(p).sub(s));
            s.moveAt(Tmp.v2.set(Tmp.v1), 0f);
            s.rotation(a + 180f);
            s.segment(p.segment() + 1);
        }
    }

    public boolean chainHasPlayer(){
        for(Chainedc n = head; n != null; n = n.child()) if(n.isPlayer()) return true;
        return false;
    }

    @Replace
    public boolean isCommandable(){
        return !chainHasPlayer() && ((Unitc)self()).controller() instanceof CommandAI;
    }

    @Replace
    public boolean isLocal(){
        return (head != null && head != self() && head.controller() == Vars.player) || ((Unitc)self()).controller() == Vars.player;
    }
}