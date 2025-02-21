package omaloon.entities.comp;

import arc.struct.*;
import ent.anno.Annotations.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.*;

import static mindustry.Vars.state;

/**
 * unlocks building and mining, also makes the drawing of those hidden
 */
@SuppressWarnings("unused")
@EntityComponent
abstract class CoreComp implements Unitc{
    @Import
    float rotation;
    @Import
    UnitType type;
    @Import
    StatusEntry[] statuses;

    @Replace(1)
    @Override
    public void updateBuildLogic(){
        Queue<BuildPlan> plans = plans();
        for(BuildPlan req : plans){
            boolean valid =
                ((req.tile() != null && req.tile().build instanceof ConstructBlock.ConstructBuild cons && cons.current == req.block) ||
                    (req.breaking ? Build.validBreak(team(), req.x, req.y) :
                        Build.validPlace(req.block, team(), req.x, req.y, req.rotation)));
            if(!valid) plans.remove(req);
        }
    }

    @Replace(1)
    @Override
    public boolean canBuild(){
        return true;
    }

    @Replace(1)
    @Override
    public boolean mining(){
        return false;
    }

    @Replace(1)
    @Override
    public float prefRotation(){
        if(moving() && type.omniMovement){
            return vel().angle();
        }
        return rotation;
    }

    @Replace(1)
    @Override
    public void drawBuilding(){
    }

    @Replace(1)
    @Override
    public boolean activelyBuilding(){
        if(isBuilding()){
            var plan = buildPlan();
            if(!state.isEditor() && plan != null && !within(plan, state.rules.infiniteResources ? Float.MAX_VALUE : type.buildRange)){
                return false;
            }
        }
        return isBuilding() && updateBuilding();
    }
}
