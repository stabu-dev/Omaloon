package omaloon.entities.comp;

import ent.anno.Annotations.*;
import mindustry.gen.*;
import omaloon.gen.*;

@EntityComponent
abstract class DroneComp implements Unitc {
	transient Masterc master;

	@Override
	public void update() {
		if (!master.isValid() || master == null || master.dead()) Call.unitDespawn(self());
	}
}
