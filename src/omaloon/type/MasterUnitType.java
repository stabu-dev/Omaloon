package omaloon.type;

import mindustry.content.*;
import mindustry.type.*;
import omaloon.gen.*;

public class MasterUnitType extends GlassmoreUnitType {
	public UnitType gunUnitType = UnitTypes.flare, actionUnitType = UnitTypes.poly;
	public float
	gunOffset = 12f, actionOffset = -12f;

	public MasterUnitType(String name) {
		super(name);
		buildRange = -1;
		buildSpeed = 0.0001f;
		constructor = MasterMechUnit::create;
	}
}
