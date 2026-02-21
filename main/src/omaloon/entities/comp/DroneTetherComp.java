package omaloon.entities.comp;

import mindustry.game.*;
import mindustry.gen.*;
import omaloon.annotations.Annotations.*;

/**
 * Copy of UnitTetherComp cause that one doesn't work with this annotation processor.
 */
@SuppressWarnings("unused")
@EntityComponent
abstract class DroneTetherComp implements Unitc {
    @Import
    Team team;
    @Import
    float id;

    int abilityIndex = -1;
    int parentId = -1;
    transient Unit parent;

    @Override
    public void beforeWrite(){
        parentId = parent != null ? parent.id() : -1;
    }

    public void loadParent(){
        if (parentId != -1) {
            parent = Groups.unit.getByID(parentId);
        }

        if(abilityIndex != -1){
            parent.abilities[abilityIndex].data = id;
//            for(int i = 0; i < abilities.length; i++){
//                if(!(abilities[i] instanceof DroneAbility droneAbility)) continue;
//                if(!droneAbility.registerDrone(self(), owner, true)) continue;
//                abilityIndex = i;
//                return;
//            }
//            if(shouldReset) ownerID = -1;
            return;
        }
//        if(
//                abilityIndex >= owner.abilities.length ||
//                        !(owner.abilities[abilityIndex] instanceof DroneAbility a) ||
//                        !a.registerDrone(self(), owner, true)
//        ){
//            if(shouldReset) ownerID = -1;
//            return;
//        }
//        return;
    }

//    @Import
//    private transient float buildCounter;
//    @Import
//    private transient BuildPlan lastActive;

    public boolean validParent(){
        return parent != null && parent.isValid() && parent.team() == team;
    }

//    public float buildCounter(){
//        return buildCounter;
//    }
//
//    public void buildCounter(float buildCounter){
//        this.buildCounter = buildCounter;
//    }

//    public BuildPlan lastActive(){
//        return lastActive;
//    }
//
//    public void lastActive(BuildPlan lastActive){
//        this.lastActive = lastActive;
//    }


//    @Override
//    public void read(Reads read){
//        int rawOwnerID = read.i();
//        int rawAbilityIndex = read.i();
//        if(rawAbilityIndex != -1){
//            abilityIndex = rawAbilityIndex;
//        }
//        if(rawOwnerID != -1){
//            ownerID = rawOwnerID;
//        }
//    }

    @Override
    public void afterSync(){
        loadParent();
    }
    @Override
    public void afterRead(){
        loadParent();
    }


    @Override
    public void update(){
        if(!validParent()){
            Call.unitDespawn(self());
        }
    }

//    @Override
//    public void write(Writes write){
//        write.i(-1);
//        write.i(-1);
//    }
}
