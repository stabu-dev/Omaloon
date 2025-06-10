package omaloon.world.graph;

import arc.struct.*;
import omaloon.gen.*;
import omaloon.world.interfaces.*;

/**
 * @author Liz
 */
public class PressureGraph{
    static Seq<HasPressure> tmp = new Seq<>(), tmp2 = new Seq<>();

    public Seq<HasPressure> builds = new Seq<>();

    public boolean changed;

    public PressureGraphUpdater updater = PressureGraphUpdater.create().create(this);

    public void addRaw(HasPressure build){
        builds.add(build);
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

    public void removeRaw(HasPressure build){
        builds.remove(build);
        checkEntity();
        changed = true;
    }

    public void update(){
        if(changed){
            builds.each(HasPressure::onPressureGraphUpdate);
            changed = false;
        }
    }
}
