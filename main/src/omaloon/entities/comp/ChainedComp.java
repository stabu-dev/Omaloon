package omaloon.entities.comp;

import arc.func.*;
import arc.math.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.type.*;
import omaloon.annotations.Annotations.*;
import omaloon.gen.*;
import omaloon.type.*;

@SuppressWarnings("unused")
@EntityComponent
abstract class ChainedComp implements Unitc{
    @Import
    public UnitType type;
    @Import
    public boolean dead;

    transient Chainedc head, parent, child, tail;
    transient int segment;

    int parentId;

    @Replace
    public void display(Table table){
        if(head != null){
            head.type().display(self(), table);
        }else{
            type.display(self(), table);
        }
    }

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

    public int chainLength(){
        int size = 0;
        Chainedc next = head;
        while(next != null){
            next = next.child();
            size++;
        }
        return size;
    }

    public void connect(Unit to){
        if(to instanceof Chainedc chained && chained.head() != head){
            tail.child(chained.head());
            chained.head().parent(tail);

            tail.propagateUp(segment -> segment.tail(chained.tail()));
            chained.propagateDown(segment -> segment.head(head));

            head.propagateDown(segment -> {
                if(segment == head) return;
                UnitType h = head.type();
                UnitType target = segment == head.tail() ? h.segmentEndUnit : h.segmentUnit;
                UnitType prev = segment.type();
                segment.type(target == null ? h : target);
                if(prev != segment.type()) segment.setupWeapons(segment.type());
            });
        }
    }

    public void loadParent(){
        if(parentId != -1){
            Unit p = Groups.unit.getByID(parentId);
            parentId = -1;

            if(p instanceof Chainedc chained){
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
    public void remove(){
        boolean wasDead = dead;
        Chainedc p = parent;
        Chainedc c = child;
        UnitType hType = head.type();

        splitTop();
        splitBottom();

        if(wasDead){
            if(p != null){
                Chainedc h = p.head();
                if(h.type() instanceof GlassmoreUnitType g && g.killSmallChains && h.chainLength() < g.segmentUnits){
                    h.kill();
                }
            }
            if(c != null && hType instanceof GlassmoreUnitType g){
                if(!g.splittable || (g.killSmallChains && c.chainLength() < g.segmentUnits)){
                    c.kill();
                }
            }
        }
    }

    public void splitBottom(){
        if(child != null){
            Chainedc c = child;
            child = null;
            c.parent(null);
            c.propagateDown(segment -> segment.head(c));

            if(head.type() instanceof GlassmoreUnitType h && h.splittable){
                if(!h.killSmallChains || c.chainLength() >= h.segmentUnits){
                    UnitType prev = c.type();
                    c.type(h);
                    if(prev != c.type()) c.setupWeapons(c.type());

                    c.propagateDown(segment -> {
                        if(segment == c) return;
                        UnitType target = segment == c.tail() ? h.segmentEndUnit : h.segmentUnit;
                        UnitType p = segment.type();
                        segment.type(target == null ? h : target);
                        if(p != segment.type()) segment.setupWeapons(segment.type());
                    });
                }
            }
        }
    }

    public void splitTop(){
        if(parent != null){
            Chainedc p = parent;
            parent = null;
            p.child(null);
            p.propagateUp(segment -> segment.tail(p));

            if(head.type() instanceof GlassmoreUnitType h){
                if(!h.killSmallChains || head.chainLength() >= h.segmentUnits){
                    head.propagateDown(segment -> {
                        if(segment == head) return;
                        UnitType target = segment == head.tail() ? h.segmentEndUnit : h.segmentUnit;
                        UnitType prev = segment.type();
                        segment.type(target == null ? h : target);
                        if(prev != segment.type()) segment.setupWeapons(segment.type());
                    });
                }
            }
        }
    }

    @Override
    public void update(){
        if(head.type() instanceof GlassmoreUnitType headType){
            if(headType.killSmallChains && chainLength() < headType.segmentUnits){
                if(dead) Call.unitDestroy(id());
                else Call.unitDespawn(self());
            }
        }
    }

    @Insert("update()")
    public void updateChain(){
        if(head != self()) return;

        segment = 0;
        propagateDown(segment -> {
            Chainedc parent = segment.parent();
            if(parent != null){
                // Parents control their children, but the tail controls itself
                UnitType pType = (segment == tail) ? segment.type() : parent.type();
                float targetAngle = Angles.clampRange(parent.angleTo(segment), parent.rotation() + 180f, pType.segmentRotationRange);
                Tmp.v1.trns(targetAngle, pType.segmentSpacing).add(parent);
                segment.moveAt(Tmp.v2.set(Tmp.v1).sub(segment), 0f);
                segment.move(Tmp.v1.sub(segment));
                segment.rotation(targetAngle + 180);
                segment.segment(parent.segment() + 1);
            }
        });
    }
}