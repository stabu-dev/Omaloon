package omaloon.world.blocks.distribution;

import arc.util.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;

// TODO animation breaks if items flow too fast, unlikely to be fixed.
public class TubeGate extends TubeRouter{
    public boolean reverse = false;

    public TubeGate(String name){
        super(name);
        rotate = false;
    }

    public class TubeGateBuild extends TubeRouterBuild{
        @Override
        public boolean acceptItem(Building source, Item item){
            if(lastInput == null) lastInput = source.tile;
            return team == source.team && lastItem == null && items.total() == 0;
        }

        // TODO movement depends on lastInput, please do not
        @Override
        public Building getTileTarget(Item item, Tile from, boolean set){
            if(item == null || from == null) return null;

            int otherDir = relativeTo(from);
            Building front = nearby((otherDir + 2) % 4);

            if(reverse){
                if(lookSides(item, from, false) == null && front != null && front.acceptItem(this, item)){
                    return front;
                }else{
                    return lookSides(item, from, set);
                }
            }else{
                if(front != null && front.acceptItem(this, item)){
                    return front;
                }else{
                    return lookSides(item, from, set);
                }
            }
        }

        public @Nullable Building lookSides(Item item, Tile from, boolean set){
            int otherDir = relativeTo(from);
            for(int i = 0; i < 4; i += 2){
                Building other = nearby((otherDir + 1 + i + targetRot) % 4);
                if(set) targetRot = (byte)((i + targetRot + 2) % 4);
                if(other == null || other.tile == from) continue;
                if(other.acceptItem(this, item)){
                    return other;
                }
            }

            return null;
        }
    }
}
