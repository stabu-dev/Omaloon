package omaloon.world.interfaces;

import arc.struct.*;
import omaloon.type.customshape.*;
import omaloon.world.*;

public interface MultiPropI{
    Seq<CustomShape> shapes();

    default Runnable removed(MultiPropGroup from){
        return () -> {
        };
    }
}
