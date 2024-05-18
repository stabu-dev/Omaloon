package omaloon.ai;

import arc.util.*;
import mindustry.entities.units.*;
import omaloon.gen.*;
import omaloon.type.*;

public class GunDroneAI extends AIController {
	public float smoothing = 10f;
	public float approachRadius = 40f;

	@Override
	public void updateMovement() {
		if (unit instanceof Dronec drone) {
			target = drone.master().mounts()[0].target;
			if (target != null) {
				moveTo(target, approachRadius, smoothing);
				unit.lookAt(target);
			} else {
				moveTo(Tmp.v1.trns(drone.master().rotation() - 90f,
					((MasterUnitType) drone.master().type()).gunOffset).add(drone.master()
				), 1f, smoothing);
				if (unit.dst(Tmp.v1) < 5) {
					unit.lookAt(drone.master().rotation());
				} else {
					unit.lookAt(Tmp.v1);
				}
			}
		}
	}

	@Override
	public void updateVisuals() {}
}
