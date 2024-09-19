package omaloon.ai;

import arc.*;
import arc.util.*;
import mindustry.entities.units.*;
import omaloon.gen.*;
import omaloon.type.*;
import omaloon.ui.*;

public class ActionDroneAI extends AIController {
	public float smoothing = 30f;
	public float approachRadius = 20f;

	@Override
	public void updateUnit() {
		super.updateUnit();
		if (unit instanceof Dronec drone && drone.hasMaster()) {
			Masterc master = drone.master();
			MasterUnitType masterType = (MasterUnitType) master.type();
			if ((!unit.isBuilding() || !unit.updateBuilding()) && master.lastMiningTile() != null) {
				drone.mineTile(master.lastMiningTile());
				moveTo(Tmp.v1.set(master.lastMiningTile().worldx(), master.lastMiningTile().worldy()), approachRadius, smoothing);
				unit.lookAt(Tmp.v1);
			} else {
				drone.mineTile(null);
				if (!unit.plans.isEmpty() && unit.updateBuilding() && unit.plans.first().dst(master) <= masterType.actionBuildRange) {
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
		if (!unit.plans.isEmpty() &&
			(!unit.core().items.has(unit.buildPlan().block.requirements)) || Core.input.keyTap(OlBinding.skip_build)
		) {
			unit.plans.addLast(unit.plans.removeFirst());
		}
	}

	@Override
	public void updateVisuals() {}
}
