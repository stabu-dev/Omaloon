package omaloon.entities.comp;

import ent.anno.Annotations.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;

/**
 * unlocks building and mining, also makes the drawing of those hidden
 * TODO somehow remove mining effect
 * TODO maybe make the second part be a separate component?
 */
@SuppressWarnings("unused")
@EntityComponent
abstract class CoreComp implements Unitc {
	@Import float rotation;
	@Import UnitType type;
	@Import StatusEntry[] statuses;

	@Replace(1)
	@Override
	public boolean canBuild() {
		return true;
	}
	@Replace(1)
	@Override
	public boolean canMine() {
		return true;
	}

	@Replace(1)
	@Override
	public float prefRotation(){
		if(activelyBuilding() && type.rotateToBuilding){
			return angleTo(buildPlan());
		} else if(moving() && type.omniMovement){
			return vel().angle();
		}
		return rotation;
	}

	@Replace(1)
	@Override
	public void draw() {
		type.draw(self());
		for (StatusEntry e : statuses) {
			e.effect.draw(self(), e.time);
		}
	}
}
