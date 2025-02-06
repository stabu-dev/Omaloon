package omaloon.entities.comp;

import arc.util.io.*;
import ent.anno.Annotations.*;
import mindustry.game.*;
import mindustry.gen.*;
import omaloon.entities.abilities.*;

@SuppressWarnings("unused")
@EntityComponent
abstract class DroneComp implements Unitc, Flyingc{
    @Import
    Team team;

    transient int abilityIndex = -1;

    transient Unit owner;
    transient int ownerID = -1;

    public boolean hasOwner(){
        return owner != null && owner.isValid() && owner.team() == team;
    }

    @Override
    public void read(Reads read){
        ownerID = read.i();
        abilityIndex = read.i();
        if(ownerID != -1){
            owner = Groups.unit.getByID(ownerID);
        }
    }

    @Override
    public void update(){
        if(ownerID != -1 && owner == null){
            owner = Groups.unit.getByID(ownerID);
            ownerID = -1;

            if(hasOwner() && abilityIndex < owner.abilities.length && owner.abilities[abilityIndex] instanceof DroneAbility a){
                a.drones.add(0, self());
                a.data++;
                controller(a.droneController.apply(owner));
            }else abilityIndex = -1;
        }

        if(!hasOwner() || abilityIndex == -1){
            Call.unitDespawn(self());
        }
    }

    @Override
    public void write(Writes write){
        write.i(hasOwner() ? owner.id() : -1);
        write.i(abilityIndex);
    }
}