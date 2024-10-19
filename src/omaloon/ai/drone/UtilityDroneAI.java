package omaloon.ai.drone;

import arc.util.*;
import mindustry.content.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.world.*;
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
			BuildPlan plan = owner.buildPlan();
			Tile tile = plan.tile();
			Tmp.v1.set(plan.drawx(), plan.drawy());
			moveTo(Tmp.v1, unit.type.buildRange * buildRangeScl);
			if (unit.dst(Tmp.v1) <= unit.type.buildRange && !unit.plans.contains(plan)) unit.plans.add(plan);
			if (
				!(tile != null && (!plan.breaking || tile.block() != Blocks.air) && (plan.breaking || (tile.build == null || tile.build.rotation != plan.rotation) && plan.block.rotate || tile.block() != plan.block && (plan.block == null || (!plan.block.isOverlay() || plan.block != tile.overlay()) && (!plan.block.isFloor() || plan.block != tile.floor()))))
			) {
				owner.plans.remove(plan);
				unit.plans.remove(plan);
			}
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
