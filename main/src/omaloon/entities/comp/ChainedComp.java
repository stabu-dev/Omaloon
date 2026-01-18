package omaloon.entities.comp;

import arc.func.*;
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

    public <T extends Chainedc> void propagate(Cons<T> run){
        Chainedc next = self();
        while(next != null){
            run.get((T)next);
            next = next.child();
        }
    }

    @Insert("update()")
    public void updateChain(){
        if(head != self()) return;

        propagate(segment -> {
            Chainedc parent = segment.parent();
            if(parent != null){
                Tmp.v1.set(segment).sub(parent).setLength(type.segmentSpacing).add(parent);
                segment.move(Tmp.v1.sub(segment));
                segment.rotation(segment.angleTo(parent));
            }
        });
    }
}
