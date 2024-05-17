package omaloon.ai;

import arc.util.*;
import mindustry.entities.units.*;
import omaloon.gen.*;
import omaloon.type.*;

public class GunDroneAI extends AIController {

	@Override
	public void updateMovement() {
		if (unit instanceof Dronec drone) {
			target = drone.master().mounts()[0].target;
			if (target != null) {
				moveTo(target, 40f);
				faceTarget();
			} else {
				moveTo(Tmp.v1.trns(drone.master().rotation(),
					((MasterUnitType) drone.master().type()).actionOffset).add(drone.master()
				), 2);
			}
		}
	}
}
