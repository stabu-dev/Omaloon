package omaloon.entities.comp;

import arc.math.*;
import arc.util.*;
import ent.anno.Annotations.*;
import omaloon.gen.*;

import static mindustry.Vars.*;

@EntityComponent
abstract class ChainMechComp implements Chainedc {
	transient float walk;

	@SyncField(false) @SyncLocal float baseRotation;

	@Override
	public void update() {
		if((moving() || net.client())) {
			float len = deltaLen();
			walk += len;
			baseRotation = Angles.moveToward(baseRotation, deltaAngle(), type().baseRotateSpeed * Mathf.clamp(len / type().speed / Time.delta) * Time.delta);
		}
	}
}
