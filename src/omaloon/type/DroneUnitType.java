package omaloon.type;

import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.UnitType;
import omaloon.gen.*;

public class DroneUnitType extends GlassmoreUnitType {
	public DroneUnitType(String name) {
		super(name);
		hidden = flying = true;
		hittable = targetable = killable = false;
		playerControllable = logicControllable = false;
		isEnemy = false;
		drawItems = true;
		constructor = DroneUnit::create;
	}

	public Unit create(Team team, Masterc master) {
		Unit unit = create(team);
		unit.x = master.x();
		unit.y = master.y();
		if (unit instanceof Dronec u) u.master(master);
		return unit;
	}
}
