package omaloon.entities.comp;

import arc.func.*;
import arc.math.Angles;
import arc.util.*;
import mindustry.gen.*;
import mindustry.type.*;
import omaloon.annotations.Annotations.*;
import omaloon.gen.*;

@EntityComponent
abstract class ChainedComp implements Unitc{
    @Import public UnitType type;

    transient Chainedc head, parent, child, tail;
    transient int segment;
    transient boolean grown;

    int parentId;

    @Override
    public void add(){
        head = self();
        tail = self();
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

    public void connect(Unit to){
        if(to instanceof Chainedc chained){
            if (chained.head() != chained || chained == self()) return;
            chained.parent(tail);
            tail.child(chained);
            chained.head(head);
            tail = chained;
        }
    }

    public void loadParent(){
        if(parentId != -1) {
            Unit p = Groups.unit.getByID(parentId);
            parentId = -1;

            if (p instanceof Chainedc chained) {
                chained.child(self());
                parent = chained;
                chained.tail(self());
                head = chained.head();
            }
        }
    }

    public <T extends Chainedc> void propagateDown(Cons<T> run){
        Chainedc next = self();
        while(next != null){
            run.get((T)next);
            next = next.child();
        }
    }
    public <T extends Chainedc> void propagateUp(Cons<T> run){
        Chainedc next = self();
        while(next != null){
            run.get((T)next);
            next = next.parent();
        }
    }

    @Override
    public void remove() {
        split();
    }

    public void split() {
        if (child != null) {
            propagateDown(segment -> {
                segment.head(child);
            });
            child.parent(null);
        }
        if (parent != null) {
            propagateUp(segment -> {
                segment.tail(parent);
            });
            parent.child(null);
        }
    }

    @Insert("update()")
    public void updateChain(){
        if(head != self()) return;

        propagateDown(segment -> {
            Chainedc parent = segment.parent();
            if(parent != null){
                float targetAngle = Angles.clampRange(parent.angleTo(segment), parent.rotation() + 180f, type.segmentRotationRange);
                Tmp.v1.trns(targetAngle, type.segmentSpacing).add(parent);
                segment.move(Tmp.v1.sub(segment));
                segment.rotation(targetAngle + 180);
            }
        });
    }
}
