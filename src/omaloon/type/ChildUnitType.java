package omaloon.type;

import mindustry.game.*;
import mindustry.gen.*;
import omaloon.gen.*;

public class ChildUnitType extends GlassmoreUnitType {
	public ChildUnitType(String name) {
		super(name);
	}

	public Unit create(Team team, Masterc master) {
		Unit unit = create(team);
		unit.x = master.x();
		unit.y = master.y();
		if (unit instanceof Dronec u) u.master(master);
		return unit;
	}
}
