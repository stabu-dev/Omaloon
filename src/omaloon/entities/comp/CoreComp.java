package omaloon.entities.comp;

import ent.anno.Annotations.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;

/**
 * unlocks building and mining, also makes the drawing of those hidden
 * TODO maybe make the second part be a separate component?
 */
@SuppressWarnings("unused")
@EntityComponent
abstract class CoreComp implements Unitc {
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
	public void draw() {
		type.draw(self());
		for (StatusEntry e : statuses) {
			e.effect.draw(self(), e.time);
		}
	}
}
