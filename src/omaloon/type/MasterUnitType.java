package omaloon.type;

import arc.graphics.g2d.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import omaloon.gen.*;

public class MasterUnitType extends GlassmoreUnitType {
	public UnitType gunUnitType = UnitTypes.flare, actionUnitType = UnitTypes.poly;
	public float gunOffset = 12f, actionOffset = -12f;
	public float droneConstructTime = 60f;
	public float actionBuildRange;

	public MasterUnitType(String name) {
		super(name);
		buildSpeed = 0.000001f;
		rotateToBuilding = false;
	}

	@Override
	public void draw(Unit unit) {
		super.draw(unit);
		if (unit instanceof Masterc) drawConstruct((Unit & Masterc) unit);
	}

	public <T extends Unit & Masterc> void drawConstruct(T u) {
		Draw.z(Layer.groundUnit - 0.1f);
		if (!u.hasActionUnit()) {
			Draw.draw(Draw.z(), () -> {
				Tmp.v1.trns(u.rotation - 90, actionOffset/3f).add(u);
				float progress = u.clientActionDroneConstructTime() / droneConstructTime;
				Drawf.construct(Tmp.v1.x, Tmp.v1.y, actionUnitType.fullIcon, u.rotation - 90, progress, 1f, u.clientActionDroneConstructTime());
			});
		}
		if (!u.hasAttackUnit()) {
			Draw.draw(Draw.z(), () -> {
				Tmp.v1.trns(u.rotation - 90, gunOffset/3f).add(u);
				float progress = u.clientGunDroneConstructTime() / droneConstructTime;
				Drawf.construct(Tmp.v1.x, Tmp.v1.y, gunUnitType.fullIcon, u.rotation - 90, progress, 1f, u.clientGunDroneConstructTime());
			});
		}
	}
}
