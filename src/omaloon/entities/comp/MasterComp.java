package omaloon.entities.comp;

import arc.struct.*;
import ent.anno.Annotations.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.input.*;
import mindustry.type.*;
import mindustry.world.*;
import omaloon.type.*;

@EntityComponent
abstract class MasterComp implements Unitc {
	@Import UnitType type;
	@Import Team team;
	@Import Tile mineTile;
	@Import Queue<BuildPlan> plans;
	@Import ItemStack stack;
	@Import float x, y, mineTimer;

	transient Unit gunUnit, actionUnit;

	public boolean hasActionUnit() {
		return actionUnit != null && actionUnit.isValid() && actionUnit.team() == team() && !actionUnit.dead();
	}
	public boolean hasGunUnit() {
		return gunUnit != null && gunUnit.isValid() && gunUnit.team() == team() && !gunUnit.dead();
	}

	@Override
	public void update() {
		mineTimer = 0f;
		spawnUnits();

		actionUnit.plans(plans);
		if (mineTile != null) {
			actionUnit.mineTile(mineTile);
			mineTile = null;
		}

		if (actionUnit.stack.amount > 0 && actionUnit.stack.item == stack.item) {
			InputHandler.transferItemToUnit(actionUnit.stack.item, actionUnit.x, actionUnit.y, this);
			actionUnit.stack.amount--;
		}
	}

	public void spawnUnits() {
		if (!hasGunUnit() && type().gunUnitType instanceof ChildUnitType type) {
			type.create(team, as());
		}
		if (!hasActionUnit() && type().actionUnitType instanceof ChildUnitType type) {
			type.create(team, as());
		}
	}

	@Override public MasterUnitType type() {
		return (MasterUnitType) type;
	}
}
