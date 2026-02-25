package omaloon.entities.comp;
import arc.math.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;
import omaloon.annotations.Annotations.*;

@EntityComponent
abstract class FloatMechComp implements Unitc, Mechc {
    @Import
    UnitType type;
    @Import
    float elevation;
    @Override
    public void update(){
        elevation = Mathf.approachDelta(elevation, onSolid() || isUnderBuildPlan() ? 1f : 0f, type.riseSpeed);
    }

    boolean isUnderBuildPlan(){
        hitboxTile(Tmp.r1);
        for(Unit unit : Groups.unit){
            for(BuildPlan plan : unit.plans()){
                if(plan.breaking || plan.block == null) continue;
                if(!plan.block.solid && !plan.block.solidifes) continue;
                plan.hitbox(Tmp.r2);
                if(Tmp.r1.overlaps(Tmp.r2)) return true;
            }
        }
        return false;
    }

    @Replace(1)
    @Override
    public EntityCollisions.SolidPred solidity(){
        return null;
    }
}