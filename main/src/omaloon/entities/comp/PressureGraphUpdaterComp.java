package omaloon.entities.comp;

import mindustry.gen.*;
import omaloon.annotations.Annotations.*;
import omaloon.gen.*;
import omaloon.world.graph.*;

@EntityDef(value = PressureGraphUpdaterc.class, genio = false, serialize = false)
@EntityComponent
abstract class PressureGraphUpdaterComp implements Entityc{
    public transient PressureGraph graph;

    public PressureGraphUpdater create(PressureGraph pressureGraph){
        graph = pressureGraph;
        return self();
    }

    @Override
    public void update(){
        if(graph != null){
            graph.update();
        }else remove();
    }
}
