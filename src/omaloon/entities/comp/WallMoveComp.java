package omaloon.entities.comp;

import ent.anno.Annotations.*;
import mindustry.entities.*;
import mindustry.gen.*;

/**
 * A component to make the unit disregard wall collision with blocks.
 */
@EntityComponent
abstract class WallMoveComp implements Unitc {
	@Override
	@Replace(1)
	public EntityCollisions.SolidPred solidity() {
		return null;
	}
}
