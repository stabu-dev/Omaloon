package omaloon.ai.drone;

import arc.util.*;
import mindustry.gen.*;
import omaloon.ai.*;

public class UtilityDroneAI extends DroneAI {
	public float mineRangeScl = 0.75f;
	public float buildRangeScl = 0.75f;

	public UtilityDroneAI(Unit owner) {
		super(owner);
	}

	@Override
	public void updateMovement() {
		if (owner.activelyBuilding()) {
			Tmp.v1.set(owner.buildPlan().drawx(), owner.buildPlan().drawy());
			moveTo(Tmp.v1, unit.type.buildRange * buildRangeScl);
			if (unit.dst(Tmp.v1) <= unit.type.buildRange && !unit.plans.contains(owner.buildPlan())) unit.plans.add(owner.buildPlan());
		} else {
			unit.plans.clear();
			if (owner.mining()) {
				Tmp.v1.set(owner.mineTile.worldx(), owner.mineTile.worldy());
				if (unit.dst(Tmp.v1) <= unit.type.mineRange) unit.mineTile = owner.mineTile;
				moveTo(Tmp.v1, unit.type.mineRange * mineRangeScl);
			} else {
				unit.mineTile = null;
				rally();
			}
		}
	}
}
