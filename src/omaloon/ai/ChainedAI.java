package omaloon.ai;

import arc.util.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;
import omaloon.gen.*;

public class ChainedAI extends AIController {
	public <T extends Unit & Chainedc> T cast() {
		return (T) unit;
	}

	@Override
	public void updateWeapons() {
		if (cast().head().controller() instanceof Player p) {
			unit.isShooting = p.unit().isShooting;

			for (var mount : unit.mounts) {
				Weapon weapon = mount.weapon;

				//let uncontrollable weapons do their own thing
				if (!weapon.controllable || weapon.noAttack) continue;

				if (!weapon.aiControllable) {
					mount.rotate = false;
					continue;
				}

				mount.rotate = true;
				Tmp.v1.trns(unit.rotation + mount.weapon.baseRotation, 5f);
				mount.aimX = p.unit().aimX();
				mount.aimY = p.unit().aimY();

				unit.aimX = mount.aimX;
				unit.aimY = mount.aimY;
			}
		} else super.updateWeapons();
	}
}
