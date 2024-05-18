package omaloon.ai;

import arc.util.*;
import mindustry.entities.units.*;
import omaloon.gen.*;
import omaloon.type.*;

public class ActionDroneAI extends AIController {
	public float smoothing = 10f;
	public float approachRadius = 20f;

	@Override
	public void updateMovement() {
		if (unit instanceof Dronec drone && drone.hasMaster()) {
			Masterc master = drone.master();
			MasterUnitType masterType = (MasterUnitType) master.type();
			if (master.lastMiningTile() != null) {
				drone.mineTile(master.lastMiningTile());
				moveTo(Tmp.v1.set(master.lastMiningTile().worldx(), master.lastMiningTile().worldy()), approachRadius, smoothing);
				unit.lookAt(Tmp.v1);
			} else {
				drone.mineTile(null);
				if (!unit.plans.isEmpty() && unit.plans.first().dst(master) <= masterType.actionBuildRange) {
					moveTo(Tmp.v1.set(unit.plans.first().getX(), unit.plans.first().getY()), approachRadius, smoothing);
					unit.lookAt(Tmp.v1);
				} else {
					moveTo(Tmp.v1.trns(drone.master().rotation() - 90, masterType.actionOffset).add(master), 1f, smoothing);
					if (unit.dst(Tmp.v1) < 5) {
						unit.lookAt(drone.master().rotation());
					} else {
						unit.lookAt(Tmp.v1);
					}
				}
			}
		}
	}

	@Override
	public void updateVisuals() {}
}
