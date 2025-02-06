package omaloon.entities.comp;

import arc.func.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import ent.anno.Annotations.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;
import omaloon.type.*;
import omaloon.utils.*;

@SuppressWarnings({"unused", "unchecked", "RedundantVariable", "UnnecessaryReturnStatement"})
@EntityComponent
abstract class ChainedComp implements Unitc{
    transient Unit head, tail, parent, child;

    transient float growTime = 0, chainTime = 0;

    // internal values that i wish i had the entityanno knowledge to not make it generate io
    private int parentID = -1, childID = -1;
    boolean grown;

    @Import
    UnitType type;
    @Import
    UnitController controller;
    @Import
    Team team;
    @Import
    float x, y, rotation;
    @Import
    WeaponMount[] mounts;

    /**
     * Add first segments if this unit is not grown.
     */
    @Override
    public void add(){
        head = tail = self();

        if(!grown){
            for(int i = 0; i < checkType(type).minSegments - 1; i++){
                grow();
            }
            // i need this because again, i don't know how to not genio.
            grown = true;
        }
    }

    @Override
    public void aim(float x, float y){
        if(isHead()){
            final float finalX = x, finalY = y;
            consBackwards(u -> {
                if(!u.isHead()) u.aim(finalX, finalY);
            });
        }
    }

    /**
     * Multiplies the unit cap to fit the amount of biggest sized chains. If it overflows, then just pick the regular unit cap.
     */
    @Override
    @Replace
    public int cap(){
        MillipedeUnitType uType = checkType(type);
        return Math.max(Units.getCap(team) * Math.max(uType.segmentLength, uType.maxSegments), Units.getCap(team));
    }

    /**
     * Wrong cast errors are way too long. So long in fact that the crash box is too small for it.
     */
    public MillipedeUnitType checkType(UnitType def){
        if(!(def instanceof MillipedeUnitType)) throw new RuntimeException("Unit's type must be MillipedeUnitType");
        return (MillipedeUnitType)def;
    }

    /**
     * Connects this centipede with another one.
     * <li>
     * If this unit is not the chain unit's tail, it'll call this method on the tail unit.
     * </li>
     * <li>
     * If Parameter "to" is not the chain unit's head, "to" will become its head() method.
     * </li>
     */
    public void connect(Unit to){
        Chainedc cast = ((Chainedc)to).head().as();
        if(isTail()){
            cast.parent(self());
            child = to;

            ((Chainedc)head).consBackwards(u -> {
                u.head(head);
                u.tail(cast.tail());
            });

            ((Chainedc)head).consBackwards(u -> {
                u.setupWeapons(u.type);
                if(!(u.controller() instanceof Player)) u.resetController();
            });
        }else ((Chainedc)tail).connect(to);
    }

    /**
     * @param cons will run through this unit and it's children recursively.
     */
    public <T extends Unit&Chainedc> void consBackwards(Cons<T> cons){
        T current = as();
        cons.get(current);
        while(current.child() != null){
            cons.get(current.child().as());
            current = current.child().as();
        }
    }

    /**
     * @param cons will run through this unit and it's parents recursively.
     */
    public <T extends Unit&Chainedc> void consForward(Cons<T> cons){
        T current = as();
        cons.get(current);
        while(current.parent() != null){
            cons.get(current.parent().as());
            current = current.parent().as();
        }
    }

    /**
     * Force a specific controller on certain parts of the chain.
     */
    @MethodPriority(-1)
    @Override
    @BreakAll
    public void controller(UnitController next){
        if(!isHead()){
            if(next instanceof Player){
                head.controller(next);
                return;
            }

            controller = new AIController();
            if(controller.unit() != self()) controller.unit(self());
            return;
        }
    }

    @Override
    public void controlWeapons(boolean rotate, boolean shoot){
        if(isHead()) consBackwards(unit -> {
            if(!unit.isHead()) unit.controlWeapons(rotate, shoot);
        });
    }

    /**
     * Counts the amount of children from this unit recursively.
     */
    public <T extends Unit&Chainedc> int countBackwards(){
        int out = 0;

        T current = as();
        while(current.child() != null){
            out++;
            current = current.child().as();
        }

        return out;
    }

    /**
     * Counts the amount of parents from this unit recursively.
     */
    public <T extends Unit&Chainedc> int countForward(){
        int out = 0;

        T current = as();
        while(current.parent() != null){
            out++;
            current = current.parent().as();
        }

        return out;
    }

    /**
     * Adds an extra segment to the chain.
     */
    public <T extends Unit&Chainedc> void grow(){
        if(!isTail()){
            ((Chainedc)tail).grow();
        }else{
            MillipedeUnitType uType = checkType(type);

            T tail = ((T)type.create(team));
            tail.grown(true);
            tail.set(
            x + Angles.trnsx(rotation + 90, 0, uType.segmentOffset),
            y + Angles.trnsy(rotation + 90, 0, uType.segmentOffset)
            );
            tail.rotation = rotation;
            tail.add();
            connect(tail);
        }
    }

    /**
     * Self explanatory, they'll return true if it is the head, if it isn't the head nor the tail, or if it is tje tail. Respectively.
     */
    public boolean isHead(){
        return head == self();
    }

    public boolean isSegment(){
        return head != self() && tail != self();
    }

    public boolean isTail(){
        return tail == self();
    }

    /**
     * Read parent and child id.
     */
    @Override
    public void read(Reads read){
        parentID = read.i();
        childID = read.i();
    }

    /**
     * Split the chain or kill the whole chain if an unit is removed.
     */
    @Override
    public void remove(){
        if(checkType(type).splittable){
            if(parent != null){
                ((Chainedc)parent).child(null);
                ((Chainedc)parent).consForward(u -> {
                    u.tail(parent);
                    u.setupWeapons(u.type);
                });
            }
            if(child != null){
                ((Chainedc)child).parent(null);
                ((Chainedc)child).consBackwards(u -> {
                    u.head(child);
                    u.setupWeapons(u.type);
                });
            }
            if(parent != null && child != null) checkType(type).splitSound.at(x, y);
        }else{
            if(parent != null) ((Chainedc)parent).consForward(Unitc::kill);
            if(child != null) ((Chainedc)child).consBackwards(Unitc::kill);
        }
    }

    /**
     * Updates the mounts to be based on the unit's position in the chain. Called when the chain connects.
     */
    @Override
    @Replace
    public void setupWeapons(UnitType def){
        MillipedeUnitType uType = checkType(def);
        Seq<Weapon> weapons = uType.chainWeapons.get(uType.weaponsIndex.get(self()));
        mounts = new WeaponMount[weapons.size];
        for(int i = 0; i < mounts.length; i++){
            mounts[i] = weapons.get(i).mountType.get(weapons.get(i));
        }
    }

    /**
     * Connect the units together after read.
     */
    @Override
    public void update(){
        if(parentID != -1){
            if(parent == null) ((Chainedc)Groups.unit.getByID(parentID)).connect(self());
            parentID = -1;
        }
        if(childID != -1){
            if(child == null) connect(Groups.unit.getByID(childID));
            childID = -1;
        }

        if(isTail()){
            MillipedeUnitType uType = checkType(type);

            if(countForward() + 1 < uType.segmentLength && uType.regenTime > 0){
                growTime += Time.delta;
                if(growTime > uType.regenTime){
                    grow();
                    uType.chainSound.at(x, y);
                    growTime %= uType.regenTime;
                }
            }

            Tmp.r1.setCentered(
            x + Angles.trnsx(rotation + 90, 0, uType.segmentOffset),
            y + Angles.trnsy(rotation + 90, 0, uType.segmentOffset),
            uType.segmentOffset
            );
            Units.nearby(Tmp.r1, u -> {
                if(u instanceof Chainedc chain && chain.isHead() && u != head && countForward() + chain.countBackwards() + 2 <= uType.maxSegments && uType.chainTime > 0){
                    chainTime += Time.delta;
                    if(chainTime > uType.chainTime){
                        Log.info("a");
                        connect(u);
                        uType.chainSound.at(x, y);
                        chainTime %= uType.chainTime;
                    }
                }
            });

            if(countForward() + 1 < uType.minSegments){
                consBackwards(Unitc::kill);
            }
        }
    }

    /**
     * Updates the position and rotation of each segment in the chain.
     */
    @Insert("update()")
    public void updateChain(){
        MillipedeUnitType uType = checkType(type);
        if(isHead()) consBackwards(c -> {
            if(c.parent() != null){
                Tmp.v1.set(c).sub(c.parent()).nor().scl(uType.segmentOffset);
                float angleDst = OlUtils.angleDistSigned(Tmp.v1.angle(), c.parent().rotation + 180);
                if(Math.abs(angleDst) > uType.angleLimit){
                    Tmp.v1.rotate(-Tmp.v1.angle() + c.parent().rotation + 180 + (angleDst > 0 ? uType.angleLimit : -uType.angleLimit));
                }
                Tmp.v1.add(c.parent());
                c.set(Tmp.v1.x, Tmp.v1.y);
                c.rotation = c.angleTo(c.parent());
            }
        });
    }

    /**
     * Save parent and child id to be read later.
     */
    @Override
    public void write(Writes write){
        write.i(parent == null ? -1 : parent.id);
        write.i(child == null ? -1 : child.id);
    }
}
