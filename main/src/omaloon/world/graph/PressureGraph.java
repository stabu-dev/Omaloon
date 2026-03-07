package omaloon.world.graph;

import arc.struct.*;
import arc.struct.ObjectMap.*;
import arc.util.*;
import mindustry.*;
import mindustry.type.*;
import omaloon.content.*;
import omaloon.gen.*;
import omaloon.math.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;

/**
 * @author Liz
 */
public class PressureGraph{
    static Seq<HasPressure> tmp = new Seq<>(), tmp2 = new Seq<>(), tmp3 = new Seq<>();

    static Seq<Entry<HasPressure, HasPressure>> edges = new Seq<>();
    static ObjectIntMap<HasPressure> connections = new ObjectIntMap<>();
    static FloatSeq flows = new FloatSeq(Vars.content.liquids().size + 1);

    public Seq<HasPressure> builds = new Seq<>(false);

    public boolean changed;

    public PressureGraphUpdater updater = PressureGraphUpdater.create().create(this);

    public void addRaw(HasPressure build){
        builds.addUnique(build);
        build.pressure().graph = this;
        checkEntity();
        changed = true;
    }

//    public void checkDamage(){
//        builds.each(HasPressure::doPressureDamage, build -> {
//            float pressure = build.pressure().sumPressure();
//
//            if(pressure > build.pressureConfig().maxPressure + 1) build.toBuilding().damageContinuous(build.pressureConfig().overPressureDamage);
//            if(pressure < build.pressureConfig().minPressure - 1) build.toBuilding().damageContinuous(build.pressureConfig().underPressureDamage);
//        });
//    }

    public void checkEntity(){
        if(builds.isEmpty()){
            updater.remove();
        }else{
            updater.add();
        }
    }

    public void floodMergeGraph(HasPressure start){
        tmp.clear().add(start);
        tmp2.clear();
        while(!tmp.isEmpty()){
            HasPressure current = tmp.pop();
            tmp2.add(current);

            if(current.pressureGraph() != this){
                current.pressureGraph().removeRaw(current);
                addRaw(current);
            }

            for(HasPressure next : current.connections()){
                if(!tmp2.contains(next)){
                    tmp.add(next);
                    tmp2.add(next);
                }
            }
        }
    }

    public void rebuildTanks(){
        tmp.clear().add(builds.first());
        tmp2.clear();
        tmp3.clear();

        PressureTank section;
        while(!tmp.isEmpty()){
            section = new PressureTank();
            tmp2.add(tmp.pop());
            while(!tmp2.isEmpty()){
                HasPressure current = tmp2.pop();

                section.builds.addUnique(current);
                current.pressure().section = section;

                for(HasPressure other : current.connections()){
                    if(!tmp3.contains(other)){
                        if(other.pressureConfig().group != current.pressureConfig().group || other.pressureConfig().group == null){
                            tmp.add(other);
                        }else{
                            tmp2.add(other);
                        }
                        tmp3.add(other);
                    }
                }
            }
            section.equalize();
        }
    }

    public void removeRaw(HasPressure build){
        builds.remove(build);
        checkEntity();
        changed = true;
    }

    public void transferFluids(){
        edges.clear();
        connections.clear();

        builds.each(build -> {
            Seq<HasPressure> others = build.connections().retainAll(other -> other.pressureSection() != build.pressureSection());
            connections.put(build, Math.max(1, others.size));
            others.each(other -> edges.add(new Entry<>(){{
                key = build;
                value = other;
            }}));
        });

        for(int i = 0; i < Vars.content.liquids().size + 1; i++){
            flows.clear();
            int liquidID = i - 1;
            Liquid liquid = Vars.content.liquid(liquidID);

            edges.each(e -> {
                HasPressure from = e.key;
                HasPressure to = e.value;

                flows.add(Physics.fluidFlow(
                from.pressure().getPressure(liquidID),
                from.pressureConfig().fluidCapacity,
                to.pressure().getPressure(liquidID),
                to.pressureConfig().fluidCapacity,
                OlLiquids.getDensity(liquid),
                OlLiquids.getViscosity(liquid),
                Time.delta
                ) / connections.get(to) / 2f);
            });

            int edgeIndex = 0;
            for(Entry<HasPressure, HasPressure> currentEdge : edges){
                float flow = flows.get(edgeIndex);
                if(HasPressure.canTransfer(currentEdge.key, currentEdge.value, liquid, flow)){
//                    if(currentEdge.value.reacts(liquid) && flow > 0){
//                        @Nullable Liquid react = currentEdge.value.fluidReacts(liquid);
//
//                        float remove = Math.min(currentEdge.value.getFluid(react), flow);
//
//                        currentEdge.key.pressureSection().removeFluid(liquid, flow);
//                        flow = Mathf.maxZero(flow - remove);
//                        Fx.steam.at(currentEdge.key.toBuilding());
//                        Fx.steam.at(currentEdge.value.toBuilding());
//                    }
                    currentEdge.key.pressureSection().removeFluid(liquid, flow);
                    currentEdge.value.pressureSection().addFluid(liquid, flow);
                }
                edgeIndex++;
            }
        }
    }

    public void update(){
        if(changed){
            rebuildTanks();

            builds.each(HasPressure::onPressureGraphUpdate);
            changed = false;
        }

        transferFluids();

//        checkDamage();

        builds.each(HasPressure::updateFluids);
    }
}
