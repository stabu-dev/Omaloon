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
			if (
				unit.dst(Tmp.v1) <= unit.type.buildRange && (
					(!owner.buildPlan().breaking && owner.buildPlan().progress >= 1f) ||
					(owner.buildPlan().breaking && owner.buildPlan().progress <= 0f)
				)
			) owner.plans.removeFirst();
		} else {
			unit.plans.clear();
			if (
				owner.mining() && (
					(owner.getMineResult(owner.mineTile) == owner.stack.item && owner.stack.amount > 0) ||
					(owner.stack.amount == 0)
				)
			) {
				Tmp.v1.set(owner.mineTile.worldx(), owner.mineTile.worldy());
				if (unit.dst(Tmp.v1) <= unit.type.mineRange) unit.mineTile = owner.mineTile;
				moveTo(Tmp.v1, unit.type.mineRange * mineRangeScl);
			} else {
				unit.mineTile = null;
				rally();
			}
		}

		if (unit.stack.amount > 0) {
			if (!unit.within(unit.closestCore(), unit.type.range)) {
				for(int i = 0; i < unit.stack.amount; i++) {
					Call.transferItemToUnit(unit.stack.item, unit.x, unit.y, owner);
				}
				unit.clearItem();
			}
		}
	}
}
