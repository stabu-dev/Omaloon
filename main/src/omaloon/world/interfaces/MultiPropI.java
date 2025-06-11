package omaloon.world.interfaces;

import arc.struct.*;
import omaloon.type.customshape.*;
import omaloon.world.blocks.environment.customsshapeproop.*;

public interface MultiPropI{
    Seq<CustomShape> shapes();

    default Runnable removed(MultiPropGroup from){
        return () -> {
        };
    }
}