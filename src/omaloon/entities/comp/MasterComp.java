package omaloon.entities.comp;

import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import ent.anno.Annotations.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import omaloon.gen.*;
import omaloon.type.*;

@EntityComponent
abstract class MasterComp implements Unitc {
	@Import UnitType type;
	@Import Team team;
	@Import Tile mineTile;
	@Import Queue<BuildPlan> plans;
	@Import ItemStack stack;
	@Import Seq<StatusEntry> statuses;
	@Import float mineTimer, rotation;

	transient Unit gunUnit, actionUnit;
	transient Tile lastMiningTile;
	transient int gunUnitID = -1, actionUnitID = -1;

	@Replace
	@Override
	public void draw() {
		drawBuilding();

		for (StatusEntry status : statuses) status.effect.draw(self(), status.time);

		type.draw(self());
	}

	public boolean hasActionUnit() {
		return actionUnit != null && actionUnit.isValid() && actionUnit.team() == team() && !actionUnit.dead();
	}
	public boolean hasGunUnit() {
		return gunUnit != null && gunUnit.isValid() && gunUnit.team() == team() && !gunUnit.dead();
	}

	@Override
	public void read(Reads read) {
		gunUnitID = read.i();
		actionUnitID = read.i();
	}

	public void spawnUnits() {
		if (actionUnitID != -1) {
			actionUnit = Groups.unit.getByID(actionUnitID);
			if (actionUnit instanceof Dronec drone) drone.master(self());
			actionUnitID = -1;
		}
		if (gunUnitID != -1) {
			gunUnit = Groups.unit.getByID(gunUnitID);
			if (gunUnit instanceof Dronec drone) drone.master(self());
			gunUnitID = -1;
		}

		if (!hasGunUnit() && type().gunUnitType instanceof DroneUnitType type) {
			gunUnit = type.create(team, as());
			gunUnit.set(Tmp.v1.trns(rotation - 90, type().gunOffset).add(self()));
			gunUnit.add();
		}
		if (!hasActionUnit() && type().actionUnitType instanceof DroneUnitType type) {
			actionUnit = type.create(team, as());
			gunUnit.set(Tmp.v1.trns(rotation - 90, type().actionOffset).add(self()));
			actionUnit.add();
		}
	}

	@Override public MasterUnitType type() {
		return (MasterUnitType) type;
	}

	@Override
	public void update() {
		mineTimer = 0f;
		spawnUnits();

		actionUnit.plans(plans);
		if (mineTile != null) {
			lastMiningTile = mineTile;
			mineTile = null;
		}
		if (!validMine(lastMiningTile)) lastMiningTile = null;

		if (!actionUnit.mining() && stack.amount == 0) {
			actionUnit.stack.amount = 0;
			stack = new ItemStack(actionUnit.stack.item, actionUnit.stack.amount);
		}
	}

	@Override
	public void write(Writes write) {
		write.i(hasGunUnit() ? gunUnit.id : -1);
		write.i(hasActionUnit() ? actionUnit.id : -1);
	}
}
