package omaloon.entities.comp;

import arc.util.io.*;
import ent.anno.Annotations.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;
import omaloon.gen.*;

@EntityComponent
abstract class DroneComp implements Unitc {
	@Import Team team;
	@Import ItemStack stack;

	@Import float x, y;

	transient Masterc master;
	transient int masterID = -1;

	public boolean hasMaster() {
		return master != null && master.isValid() && !master.dead() && master.team() == team;
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

		if (!hasMaster()) {
			Call.unitDespawn(self());
			return;
		}

		if (stack.amount > 0 && (master.stack().item == stack.item || master.stack().amount == 0)) {
			Call.transferItemToUnit(stack.item, x, y, master);
			stack.amount --;
		}
	}

	@Override
	public void write(Writes write) {
		write.i(hasMaster() ? master.id() : -1);
	}
}
