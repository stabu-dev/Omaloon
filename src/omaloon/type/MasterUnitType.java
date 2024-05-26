package omaloon.type;

import arc.graphics.g2d.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import omaloon.gen.*;

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

	@Override
	public void draw(Unit unit) {
		super.draw(unit);
		if (unit instanceof Masterc u) drawConstruct(u);
	}

	public void drawConstruct(Masterc u) {
		Draw.z(Layer.groundUnit - 0.1f);
		if (!u.hasActionUnit()) {
			Draw.draw(Draw.z(), () -> {
				Tmp.v1.trns(u.rotation() - 90, actionOffset/3).add(u);
				Drawf.construct(Tmp.v1.x, Tmp.v1.y, actionUnitType.fullIcon, u.rotation() - 90, u.droneConstructTime() / droneConstructTime, 1f, u.droneConstructTime());
			});
		}
		if (!u.hasAttackUnit()) {
			Draw.draw(Draw.z(), () -> {
				Tmp.v1.trns(u.rotation() - 90, attackOffset/3).add(u);
				Drawf.construct(Tmp.v1.x, Tmp.v1.y, attackUnitType.fullIcon, u.rotation() - 90, u.droneConstructTime() / droneConstructTime, 1f, u.droneConstructTime());
			});
		}
	}
}
