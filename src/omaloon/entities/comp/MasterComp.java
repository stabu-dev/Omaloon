package omaloon.entities.comp;

import arc.graphics.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import ent.anno.Annotations.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
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

//	@Replace
//	@Override
//	public void draw() {
//		drawBuilding();
//
//		for (StatusEntry status : statuses) status.effect.draw(self(), status.time);
//
//		type.draw(self());
//	}

	public boolean hasActionUnit() {
		return actionUnit != null && actionUnit.isValid() && actionUnit.team() == team() && !actionUnit.dead();
	}

	public boolean hasAttackUnit() {
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

		if (!hasAttackUnit() && type().attackUnitType instanceof DroneUnitType type && (Vars.net.server() || !Vars.net.active())) {
			gunUnit = type.create(team, as());
			gunUnit.set(Tmp.v1.trns(rotation - 90, type().attackOffset/3f).add(self()));
			gunUnit.add();
			Call.effect(Fx.spawn, gunUnit.x(), gunUnit.y(), 0f, Color.white);
			
		}
		if (!hasActionUnit() && type().actionUnitType instanceof DroneUnitType type && (Vars.net.server() || !Vars.net.active())) {
			actionUnit = type.create(team, as());
			actionUnit.set(Tmp.v1.trns(rotation - 90, type().actionOffset/3f).add(self()));
			actionUnit.add();
			Call.effect(Fx.spawn, actionUnit.x(), actionUnit.y(), 0f, Color.white);
		}
	}

	@Replace(1)
	@Override
	public EntityCollisions.SolidPred solidity() {
		return null;
	}

	@Override public MasterUnitType type() {
		return (MasterUnitType) type;
	}

	@Override
	public void update() {
		mineTimer = 0f;

		// TODO effect doesn't show up
		if ((!hasActionUnit() || !hasAttackUnit())) spawnUnits();

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
	}

	@Override
	public void write(Writes write) {
		write.i(hasAttackUnit() ? gunUnit.id : -1);
		write.i(hasActionUnit() ? actionUnit.id : -1);
	}
}
