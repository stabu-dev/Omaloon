package omaloon.entities.comp;

import arc.graphics.*;
import arc.math.*;
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

//TODO: idk, maybe there is something to improve
@EntityComponent
abstract class MasterComp implements Unitc {
	@Import UnitType type;
	@Import Team team;
	@Import Tile mineTile;
	@Import Queue<BuildPlan> plans;
	@Import ItemStack stack;
	@Import float mineTimer, rotation, elevation;

	transient Unit gunUnit, actionUnit;
	transient Tile lastMiningTile;
	transient int gunUnitID = -1, actionUnitID = -1;
	transient int itemAmount = 0;

	boolean gunDroneSpawned = false;
	boolean actionDroneSpawned = false;

	public float gunDroneConstructTime = 0;
	public float actionDroneConstructTime = 0;

	@SuppressWarnings("unused") public float clientGunDroneConstructTime() {
		return hasAttackUnit() ? 0 : gunDroneConstructTime;
	}
	@SuppressWarnings("unused") public float clientActionDroneConstructTime() {
		return hasActionUnit() ? 0 : actionDroneConstructTime;
	}

	private void createSpawnEffect(float x, float y) {
		Fx.spawn.at(x, y);
		if (Vars.net.server()) {
			Call.effect(Fx.spawn, x, y, 0f, Color.white);
		}
	}

	@Override
	@Replace
	public void drawBuildingBeam(float v, float v1) {

	}

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
		gunDroneConstructTime = read.f();
		actionDroneConstructTime = read.f();
	}

	public void spawnUnits() {
		spawnGunUnit();
		spawnActionUnit();
	}

	private void spawnGunUnit() {
		if (!gunDroneSpawned || !hasAttackUnit()) {
			if (gunDroneConstructTime < type().droneConstructTime) {
				if (!Vars.net.client()) {
					gunDroneConstructTime += Time.delta;
				}
				return;
			}

			if (type().gunUnitType instanceof DroneUnitType type && (Vars.net.server() || !Vars.net.active())) {
				gunUnit = type.create(team, as());
				gunUnit.set(Tmp.v1.trns(rotation - 90, type().gunOffset/3f).add(self()));
				gunUnit.add();
				createSpawnEffect(gunUnit.x, gunUnit.y);
				gunDroneSpawned = true;
				gunDroneConstructTime = 0f;
			}
		}
	}

	private void spawnActionUnit() {
		if (!actionDroneSpawned || !hasActionUnit()) {
			if (actionDroneConstructTime < type().droneConstructTime) {
				if (!Vars.net.client()) {
					actionDroneConstructTime += Time.delta;
				}
				return;
			}

			if (type().actionUnitType instanceof DroneUnitType type && (Vars.net.server() || !Vars.net.active())) {
				actionUnit = type.create(team, as());
				actionUnit.set(Tmp.v1.trns(rotation - 90, type().actionOffset/3f).add(self()));
				actionUnit.add();
				createSpawnEffect(actionUnit.x, actionUnit.y);
				actionDroneSpawned = true;
				actionDroneConstructTime = 0f;
			}
		}
	}

	@Replace(1)
	@Override
	public EntityCollisions.SolidPred solidity() {
		return null;
	}

	@Override
	public MasterUnitType type() {
		return (MasterUnitType) type;
	}

	@Override
	public void update() {
		elevation = Mathf.approachDelta(elevation, onSolid() ? 1f : 0f, type.riseSpeed);

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

		if (hasAttackUnit()) {
			gunDroneConstructTime = 0;
		}
		if (hasActionUnit()) {
			actionDroneConstructTime = 0;
		}
	}

	@Override
	public void write(Writes write) {
		write.i(hasAttackUnit() ? gunUnit.id : -1);
		write.i(hasActionUnit() ? actionUnit.id : -1);
		write.bool(gunDroneSpawned);
		write.bool(actionDroneSpawned);
		write.f(gunDroneConstructTime);
		write.f(actionDroneConstructTime);
	}
}
