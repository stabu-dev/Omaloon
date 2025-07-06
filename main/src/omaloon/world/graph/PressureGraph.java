package omaloon.world.graph;

import arc.struct.*;
import arc.struct.ObjectMap.*;
import arc.util.*;
import mindustry.*;
import mindustry.type.*;
import omaloon.content.*;
import omaloon.gen.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;

/**
 * @author Liz
 */
public class PressureGraph{
    static Seq<HasPressure> tmp = new Seq<>(), tmp2 = new Seq<>(), tmp3 = new Seq<>();

    static ObjectMap<HasPressure, HasPressure> edges = new ObjectMap<>();
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
                ;
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
            others.each(other -> {
                edges.put(build, other);
            });
        });

        for(int i = 0; i < Vars.content.liquids().size + 1; i++){
            flows.clear();
            int liquidID = i - 1;
            Liquid liquid = Vars.content.liquid(liquidID);

            edges.each((to, from) -> {
                float flow = to.pressureConfig().fluidCapacity * from.pressure().getPressure(liquidID);
                flow += from.pressureConfig().fluidCapacity * to.pressure().getPressure(liquidID);
                flow /= (from.pressureConfig().fluidCapacity + to.pressureConfig().fluidCapacity);
                flow -= from.pressure().getPressure(liquidID);
                flow *= from.pressureConfig().fluidCapacity;
                flow *= OlLiquids.getDensity(liquid);
                flow /= Math.max(1, OlLiquids.getViscosity(liquid) / Time.delta);
                flow /= connections.get(to);
                flow /= 2f;

                flows.add(flow);
            });

            int edgeIndex = 0;
            for(Entry<HasPressure, HasPressure> currentEdge : edges){
                if(HasPressure.canTransfer(currentEdge.key, currentEdge.value, liquid, flows.get(edgeIndex))){
                    currentEdge.key.pressureSection().removeFluid(liquid, flows.get(edgeIndex));
                    currentEdge.value.pressureSection().addFluid(liquid, flows.get(edgeIndex));
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
    }
}
