package omaloon.type;

import arc.func.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;

/**
 * a weapon that shoots different things depending on things in a unit
 */
public class FilterWeapon extends Weapon {
	/**
	 * separate from bulletFilter for stats
	 */
	public BulletType[] bullets = new BulletType[] {Bullets.placeholder};
	public Func<Unit, BulletType> bulletFilter = unit -> bullets[0];

	@Override
	protected void shoot(Unit unit, WeaponMount mount, float shootX, float shootY, float rotation) {
		bullet = bulletFilter.get(unit);
		super.shoot(unit, mount, shootX, shootY, rotation);
	}
}
