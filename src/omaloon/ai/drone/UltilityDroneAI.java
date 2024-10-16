package omaloon.ai.drone;

import mindustry.gen.*;
import omaloon.ai.*;

public class UltilityDroneAI extends DroneAI {
	public float mineRangeScl = 0.75f;
	public UltilityDroneAI(Unit owner) {
		super(owner);
	}

	@Override
	public void updateMovement() {
		if (owner.mineTile() != null) {
			moveTo(owner.mineTile(), unit.type.mineRange * mineRangeScl);
		}
	}
}
