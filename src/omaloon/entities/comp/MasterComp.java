package omaloon.entities.comp;

import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import ent.anno.Annotations.*;
import mindustry.content.*;
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
	transient float droneConstructTime = 0;
	transient int itemAmount = 0;

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
		if (droneConstructTime <= type().droneConstructTime) {
			droneConstructTime += Time.delta;
			return;
		}
		droneConstructTime %= 1f;
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

		if (!hasGunUnit() && type().attackUnitType instanceof DroneUnitType type) {
			gunUnit = type.create(team, as());
			gunUnit.set(Tmp.v1.trns(rotation - 90, type().attackOffset/3).add(self()));
			gunUnit.add();
			Fx.spawn.at(gunUnit.x(), gunUnit.y());
		}
		if (!hasActionUnit() && type().actionUnitType instanceof DroneUnitType type) {
			actionUnit = type.create(team, as());
			actionUnit.set(Tmp.v1.trns(rotation - 90, type().actionOffset/3).add(self()));
			actionUnit.add();
			Fx.spawn.at(actionUnit.x(), actionUnit.y());
		}
	}

	@Override public MasterUnitType type() {
		return (MasterUnitType) type;
	}

	@Override
	public void update() {
		mineTimer = 0f;

		if (!hasActionUnit() || !hasGunUnit()) spawnUnits();

		if (mineTile != null) {
			if (mineTile == lastMiningTile) mineTile = null;
			lastMiningTile = mineTile;
			mineTile = null;
		}
		if (!validMine(lastMiningTile) || stack.amount >= type.itemCapacity) lastMiningTile = null;
		if (hasActionUnit()) {
			actionUnit.updateBuilding = updateBuilding();
			actionUnit.plans = plans;
		}
		itemAmount = stack.amount;
	}

	@Override
	public void write(Writes write) {
		write.i(hasGunUnit() ? gunUnit.id : -1);
		write.i(hasActionUnit() ? actionUnit.id : -1);
	}
}
