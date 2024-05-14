package omaloon.type;

import mindustry.content.*;
import mindustry.type.*;

public class MasterUnitType extends GlassmoreUnitType {
	public UnitType gunUnitType = UnitTypes.flare, actionUnitType = UnitTypes.poly;

	public MasterUnitType(String name) {
		super(name);
		buildRange = -1;
		buildSpeed = 0.0001f;
		mineRange = Float.POSITIVE_INFINITY;
	}
}
