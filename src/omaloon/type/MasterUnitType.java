package omaloon.type;

import mindustry.content.*;
import mindustry.type.*;

public class MasterUnitType extends GlassmoreUnitType {
	public UnitType attackUnitType = UnitTypes.flare, actionUnitType = UnitTypes.poly;
	public float attackOffset = 12f, actionOffset = -12f;
	public float droneConstructTime = 60f;
	public float actionBuildRange;

	public MasterUnitType(String name) {
		super(name);
		buildRange = -1;
		buildSpeed = 0.0001f;
	}
}
