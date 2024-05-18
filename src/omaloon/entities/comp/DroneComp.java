package omaloon.entities.comp;

import arc.util.io.*;
import ent.anno.Annotations.*;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.type.UnitType;
import omaloon.gen.*;

@EntityComponent
abstract class DroneComp implements Unitc {
	transient Masterc master;
	transient int masterID = -1;

	public boolean hasMaster() {
		return master != null && master.isValid() && !master.dead();
	}

	@Override
	public void read(Reads read) {
		masterID = read.i();
	}

	@Override
	public void update() {
		if (masterID != -1) {
			master = (Masterc) Groups.unit.getByID(masterID);
			masterID = -1;
		}

		if (!hasMaster()) Call.unitDespawn(self());
	}

	@Override
	public void write(Writes write) {
		write.i(master.id());
	}
}
