package omaloon.ai;

import arc.util.*;
import mindustry.entities.units.*;
import omaloon.gen.*;
import omaloon.type.*;

public class ActionDroneAI extends AIController {
	public boolean idle = true;

	@Override
	public void updateMovement() {
		if (unit instanceof Dronec drone) {
			if (!unit.plans.isEmpty()) {
				moveTo(Tmp.v1.set(unit.plans.first().getX(), unit.plans.first().getY()), 20f);
			} else {
				moveTo(Tmp.v1.trns(drone.master().rotation(),
					((MasterUnitType) drone.master().type()).actionOffset).add(drone.master()
				), 2);
			}
		}
	}
}
