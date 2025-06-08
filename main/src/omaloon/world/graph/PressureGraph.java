package omaloon.world.graph;

import arc.struct.*;
import omaloon.world.interfaces.*;

/**
 * @author Liz
 */
@SuppressWarnings("unchecked")
public class PressureGraph{
    Seq<HasPressure> tmp = new Seq<>(), tmp2 = new Seq<>();

    public Seq<HasPressure> builds = new Seq<>();

    public void addRaw(HasPressure build){
        builds.add(build);
        build.pressure().graph = this;
    }

    public void floodMergeGraph(HasPressure start) {
        tmp.clear().add(start);
        tmp2.clear();
        while (!tmp.isEmpty()) {
            HasPressure current = tmp.pop();
            tmp2.add(current);

            for(HasPressure next : current.connections()) {
                if (!tmp2.contains(next)) {
                    tmp.add(next);
                    tmp2.add(next);

                    if (next.pressureGraph() != this) {
                        next.pressureGraph().removeRaw(next);
                        addRaw(next);
                    }
                };
            }
        }
    }

    public void mergeGraph(PressureGraph other){
        if(other.builds.size > builds.size){
            other.mergeGraph(this);
        }else{
            other.builds.each(build -> {
                other.removeRaw(build);
                addRaw(build);
            });
        }
    }

    public void removeRaw(HasPressure build){
        builds.remove(build);
    }
}
