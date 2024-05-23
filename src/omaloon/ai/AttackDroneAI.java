package omaloon.ai;

import arc.util.*;
import mindustry.entities.units.*;
import omaloon.gen.*;
import omaloon.type.*;

public class AttackDroneAI extends AIController {
	public float smoothing = 30f;
	public float approachRadius = 40f;

	@Override
	public void updateMovement() {
		if (unit instanceof Dronec drone && drone.hasMaster()) {
			Masterc master = drone.master();
			MasterUnitType masterType = (MasterUnitType) master.type();

			target = master.mounts()[0].target;
			if (target != null) {
				moveTo(target, approachRadius, smoothing);
				unit.lookAt(target);
			} else {
				moveTo(Tmp.v1.trns(master.rotation() - 90f, masterType.attackOffset).add(master), 1f, smoothing);
				if (unit.dst(Tmp.v1) < 5) {
					unit.lookAt(master.rotation());
				} else {
					unit.lookAt(Tmp.v1);
				}
			}
		}
	}

	@Override
	public void updateVisuals() {}
}
