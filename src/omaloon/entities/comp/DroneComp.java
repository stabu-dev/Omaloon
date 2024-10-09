package omaloon.entities.comp;

import arc.util.io.*;
import ent.anno.Annotations.*;
import mindustry.gen.*;
import mindustry.game.*;
import mindustry.type.*;

@EntityComponent
abstract class DroneComp implements Unitc, Flyingc {
    @Import float x, y, rotation;
    @Import boolean dead;
    @Import Team team;
    @Import UnitType type;

    transient Unit owner;
    transient int ownerID = -1;

    public boolean hasOwner() {
        return owner != null && owner.isValid() && owner.team() == team;
    }

    @Override
    public void read(Reads read) {
        ownerID = read.i();
        if (ownerID != -1) {
            owner = Groups.unit.getByID(ownerID);
        }
    }

    @Override
    public void update() {
        if (ownerID != -1 && owner == null) {
            owner = Groups.unit.getByID(ownerID);
            ownerID = -1;
        }

        if (!hasOwner()) {
            Call.unitDespawn(self());
        }
    }

    @Override
    public void write(Writes write) {
        write.i(hasOwner() ? owner.id() : -1);
    }
}