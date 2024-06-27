package omaloon.type;

import arc.func.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.bullet.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.meta.*;

/**
 * a weapon that shoots different things depending on things in a unit
 */
public class FilterWeapon extends Weapon {
	/**
	 * separate from bulletFilter for stats
	 */
	public OrderedMap<BulletType, String> bullets = OrderedMap.of(Bullets.placeholder, "");
	public Func<Unit, BulletType> bulletFilter = unit -> bullets.keys().toSeq().first();
// TODO i don't know how to make those icons work, and i have no clue as to why it is casting a string to BulletType
//	public TextureRegion[] iconRegions;

	@Override
	public void addStats(UnitType u, Table t) {
		if(inaccuracy > 0){
			t.row();
			t.add("[lightgray]" + Stat.inaccuracy.localized() + ": [white]" + (int)inaccuracy + " " + StatUnit.degrees.localized());
		}
		if(!alwaysContinuous && reload > 0){
			t.row();
			t.add("[lightgray]" + Stat.reload.localized() + ": " + (mirror ? "2x " : "") + "[white]" + Strings.autoFixed(60f / reload * shoot.shots, 2) + " " + StatUnit.perSecond.localized());
		}

		t.table(Styles.grayPanel, weapon -> bullets.each((bullet, icon) -> weapon.table(Tex.underline, b -> {
			b.left();
//			if (iconRegions[bullets.orderedKeys().indexOf(bullet)].found()) b.image(iconRegions[bullets.orderedKeys().indexOf(bullet)]).padRight(10).center();
			StatValues.ammo(ObjectMap.of(u, bullet)).display(b.add(new Table()).get());
		}).growX().row())).margin(10f);
	}

//	@Override
//	public void load() {
//		super.load();
//		Seq<TextureRegion> icons = new Seq<>();
//		bullets.orderedKeys().each(bullet -> icons.add(Core.atlas.find(bullets.get(bullet))));
//		iconRegions = icons.toArray();
//	}

	@Override
	protected void shoot(Unit unit, WeaponMount mount, float shootX, float shootY, float rotation) {
		bullet = bulletFilter.get(unit);
		super.shoot(unit, mount, shootX, shootY, rotation);
	}
}
