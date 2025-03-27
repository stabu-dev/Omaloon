package omaloon.entities.abilities;

import arc.scene.ui.layout.*;
import arc.struct.*;
import mindustry.entities.abilities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.meta.*;

/**
 * An ability that creates a bullet if the unit has a specific status effect.
 * @author Liz
 */
public class TankAbility extends Ability {
	public StatusEffect effect;
	public BulletType bullet;

	public TankAbility(StatusEffect effect, BulletType bullet) {
		this.effect = effect;
		this.bullet = bullet;
	}

	@Override
	public void addStats(Table t) {
		StatValues.ammo(ObjectMap.of(effect, bullet)).display(t.table().get());
	}

	@Override
	public void death(Unit unit) {
		if (unit.hasEffect(effect)) {
			bullet.create(unit, unit.x, unit.y, unit.rotation);
		}
	}
}
