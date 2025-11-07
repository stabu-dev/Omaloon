package omaloon.world.interfaces;

import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.entities.units.*;

public interface ConnectedTile{
    IntSeq tmpEdges = new IntSeq();

    static boolean connects(BuildPlan a, BuildPlan b) {
        if(a.block instanceof ConnectedTile t){
            if(b.block instanceof ConnectedTile t2){
                return t.connectsTo(a, b) && t2.connectsTo(b, a);
            }else{
                return t.connectsTo(a, b);
            }
        }else{
            if(b.block instanceof ConnectedTile t){
                return t.connectsTo(b, a);
            }else{
                return true;
            }
        }
    }

    default boolean connectsTo(BuildPlan ref, BuildPlan other){
        return true;
    }

    default int[] facingEdges(BuildPlan ref, BuildPlan other){
        tmpEdges.clear();
        Tmp.r1.setSize(other.block.size).setPosition(other.x - (other.block.size - 1) / 2, other.y - (other.block.size - 1) / 2);

        int i = 0;
        for(Point2 edge : ref.block.getEdges()){
            if(Tmp.r1.contains(ref.x + edge.x + ref.block.size / 2f, ref.y + edge.y + ref.block.size / 2f)) tmpEdges.add(i);
            if(tmpEdges.size >= ref.block.size) break;
            i++;
        }

        return tmpEdges.toArray();
    }

    int mask(BuildPlan plan, Eachable<BuildPlan> list);
}
