package omaloon.world.blocks.distribution;

import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import arc.util.*;

public class TubeUnderflowGate extends TubeOverflowGate {
    public TubeUnderflowGate(String name) {
        super(name);
    }
    
    public class TubeUnderflowGateBuild extends TubeOverflowGateBuild {
        @Override
        public Building getTileTarget(Item item, Tile from, boolean set){
            if(item == null || from == null) return null;

            int otherDir = relativeTo(from);
            Building front = nearby((otherDir + 2) % 4);

            Building side = lookSides(item, from, set);
            if(side != null){
                return side;
            }else if (front != null && front.acceptItem(this, item)){
                return front;
            }
            
            return null;
        }
    }
}
