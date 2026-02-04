package omaloon.entities.comp;

import arc.func.*;
import arc.math.Angles;
import arc.util.*;
import mindustry.gen.*;
import mindustry.type.*;
import omaloon.annotations.Annotations.*;
import omaloon.gen.*;
import omaloon.type.GlassmoreUnitType;

@SuppressWarnings("unused")
@EntityComponent
abstract class ChainedComp implements Unitc{
    @Import public UnitType type;

    transient Chainedc head, parent, child, tail;
    transient int segment;

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

    public int chainLength() {
        int size = 0;
        Chainedc next = head;
        while(next != null){
            next = next.child();
            size++;
        }
        return size;
    }

    /**
     * Appends the following chain to the end of this chain. Works regardless of what segment is called as long as it doesn't try to connect with itself.
     */
    public void connect(Unit to){
        if(to instanceof Chainedc chained && chained.head() != head){
            tail.child(chained.head());
            chained.head().parent(tail);

            tail.propagateUp(segment -> segment.tail(chained.tail()));
            chained.propagateDown(segment -> segment.head(head));
        }
    }

    public void loadParent(){
        if(parentId != -1) {
            Unit p = Groups.unit.getByID(parentId);
            parentId = -1;

            if (p instanceof Chainedc chained) {
                chained.connect(self());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Chainedc> void propagateDown(Cons<T> run){
        Chainedc next = self();
        while(next != null){
            run.get((T)next);
            next = next.child();
        }
    }
    @SuppressWarnings("unchecked")
    public <T extends Chainedc> void propagateUp(Cons<T> run){
        Chainedc next = self();
        while(next != null){
            run.get((T)next);
            next = next.parent();
        }
    }

    @Override
    public void remove() {
        GlassmoreUnitType segmentType = (GlassmoreUnitType) type;
        if (segmentType.splittable) {
            splitTop();
            splitBottom();
        } else {
            if (child != null) child.propagateDown(segment -> Call.unitDestroy(segment.id()));
            if (parent != null) parent.propagateUp(segment -> Call.unitDestroy(segment.id()));
        }
    }

    public void splitBottom() {
        if (child != null) {
            propagateDown(segment -> segment.head(child));
            child.parent(null);
        }
        if (parent != null) {
            propagateUp(segment -> segment.tail(parent));
            parent.child(null);
        }
    }
    public void splitTop() {
        if (child != null) {
            propagateDown(segment -> segment.head(child));
            child.parent(null);
        }
        if (parent != null) {
            propagateUp(segment -> segment.tail(parent));
            parent.child(null);
        }
    }

    @Override
    public void update() {
        GlassmoreUnitType segmentType = (GlassmoreUnitType) type;
        if (segmentType.killSmallChains && chainLength() < type.segmentUnits) Call.unitDestroy(id());
    }

    @Insert("update()")
    public void updateChain(){
        if(head != self()) return;

        segment = 0;
        propagateDown(segment -> {
            Chainedc parent = segment.parent();
            if(parent != null){
                float targetAngle = Angles.clampRange(parent.angleTo(segment), parent.rotation() + 180f, type.segmentRotationRange);
                Tmp.v1.trns(targetAngle, type.segmentSpacing).add(parent);
                segment.move(Tmp.v1.sub(segment));
                segment.rotation(targetAngle + 180);
                segment.segment(parent.segment() + 1);
            }
        });
    }
}
