package omaloon.entities.comp;

import arc.util.io.*;
import ent.anno.Annotations.*;
import mindustry.entities.abilities.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import omaloon.entities.abilities.*;

@SuppressWarnings("unused")
@EntityComponent
abstract class DroneComp implements Unitc, Flyingc{
    public int abilityIndex = -1;
    @Import
    Team team;
    int ownerID = -1;
    private transient Unit owner;
    @Import
    private transient float buildCounter;
    @Import
    private transient BuildPlan lastActive;

    public static boolean validOwner(Unit owner, Unit self){
        return owner != null && owner.isValid() && owner.team() == self.team();
    }

    public float buildCounter(){
        return buildCounter;
    }

    public void buildCounter(float buildCounter){
        this.buildCounter = buildCounter;
    }

    public BuildPlan lastActive(){
        return lastActive;
    }

    public void lastActive(BuildPlan lastActive){
        this.lastActive = lastActive;
    }

    @Override
    public void afterSync(){
        owner = null;
    }

    @Override
    public void read(Reads read){
        int rawOwnerID = read.i();
        int rawAbilityIndex = read.i();
        if(rawAbilityIndex != -1){
            abilityIndex = rawAbilityIndex;
        }
        if(rawOwnerID != -1){
            ownerID = rawOwnerID;
        }
    }

    @Override
    public void afterRead(){
        owner = null;
    }


    @Override
    public void update(){
        if(ownerID == -1){
            Call.unitDespawn(self());
            return;
        }

        if(owner == null){
            owner = Groups.unit.getByID(ownerID);
            if(!validOwner(owner, self())){
                ownerID = -1;
                return;
            }

            if(abilityIndex == -1){
                Ability[] abilities = owner.abilities;
                for(int i = 0; i < abilities.length; i++){
                    if(!(abilities[i] instanceof DroneAbility droneAbility)) continue;
                    if(!droneAbility.registerDrone(self(), owner, true)) continue;
                    abilityIndex = i;
                    return;
                }
                ownerID = -1;
                return;
            }
            if(
                abilityIndex >= owner.abilities.length ||
                    !(owner.abilities[abilityIndex] instanceof DroneAbility a) ||
                    !a.registerDrone(self(), owner, true)
            ){
                ownerID = -1;
                return;
            }
            return;
        }
        if(ownerID != owner.id){
            owner = null;
            return;
        }
        if(!validOwner(owner, self())){
            ownerID = -1;
        }
    }

    @Override
    public void write(Writes write){
        write.i(-1);
        write.i(-1);
    }
}