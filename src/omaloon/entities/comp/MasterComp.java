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
	@Import Tile mineTile, tile;
	@Import Queue<BuildPlan> plans;
	@Import ItemStack stack;
	@Import float mineTimer, rotation, elevation;

	transient Unit gunUnit, actionUnit;
	transient Tile lastMiningTile;
	transient int gunUnitID = -1, actionUnitID = -1;
	transient int itemAmount = 0;

	float gunDroneConstructTime = 0;
	float actionDroneConstructTime = 0;
	boolean gunDroneSpawned = false;
	boolean actionDroneSpawned = false;

	public float serverGunDroneConstructTime = 0;
	public float serverActionDroneConstructTime = 0;

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
		gunDroneSpawned = read.bool();
		actionDroneSpawned = read.bool();
		serverGunDroneConstructTime = read.f();
		serverActionDroneConstructTime = read.f();
	}

	public void spawnUnits() {
		spawnGunUnit();
		spawnActionUnit();
	}

	private void spawnGunUnit() {
		if (!gunDroneSpawned || !hasAttackUnit()) {
			if (serverGunDroneConstructTime < type().droneConstructTime) {
				if (!Vars.net.client()) {
					serverGunDroneConstructTime += Time.delta;
				}
				return;
			}

			if (type().gunUnitType instanceof DroneUnitType type && (Vars.net.server() || !Vars.net.active())) {
				gunUnit = type.create(team, as());
				gunUnit.set(Tmp.v1.trns(rotation - 90, type().gunOffset/3f).add(self()));
				gunUnit.add();
				createSpawnEffect(gunUnit.x, gunUnit.y);
				gunDroneSpawned = true;
				serverGunDroneConstructTime = 0f;
			}
		}
	}

	private void spawnActionUnit() {
		if (!actionDroneSpawned || !hasActionUnit()) {
			if (serverActionDroneConstructTime < type().droneConstructTime) {
				if (!Vars.net.client()) {
					serverActionDroneConstructTime += Time.delta;
				}
				return;
			}

			if (type().actionUnitType instanceof DroneUnitType type && (Vars.net.server() || !Vars.net.active())) {
				actionUnit = type.create(team, as());
				actionUnit.set(Tmp.v1.trns(rotation - 90, type().actionOffset/3f).add(self()));
				actionUnit.add();
				createSpawnEffect(actionUnit.x, actionUnit.y);
				actionDroneSpawned = true;
				serverActionDroneConstructTime = 0f;
			}
		}
	}

	private void createSpawnEffect(float x, float y) {
		if (Vars.net.server()) {
			Call.effect(Fx.spawn, x, y, 0f, Color.white);
		} else {
			Fx.spawn.at(x, y);
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

		spawnUnits();

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

		if (!hasActionUnit()) {
			actionDroneSpawned = false;
		}
		if (!hasAttackUnit()) {
			gunDroneSpawned = false;
		}

		if (Vars.net.client()) {
			gunDroneConstructTime = serverGunDroneConstructTime;
			actionDroneConstructTime = serverActionDroneConstructTime;
		}

		if (tile != null && EntityCollisions.solid(tile.x, tile.y)) {
			elevation = 1f;
		}
	}

	@Override
	public void write(Writes write) {
		write.i(hasAttackUnit() ? gunUnit.id : -1);
		write.i(hasActionUnit() ? actionUnit.id : -1);
		write.bool(gunDroneSpawned);
		write.bool(actionDroneSpawned);
		write.f(serverGunDroneConstructTime);
		write.f(serverActionDroneConstructTime);
	}
}