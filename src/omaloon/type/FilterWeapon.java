package omaloon.type;

import arc.*;
import arc.func.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
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
 * A weapon that shoots different things depending on things in a unit
 * <p>
 * There is a better way to do this i'm sure, but there's just one weapon like this in the entire mod, so i won't bother
 * @author Liz
 */
public class FilterWeapon extends Weapon {
	/**
	 * separate from bulletFilter for stats
	 */
	public BulletType[] bullets = new BulletType[]{Bullets.placeholder};
	public Func<Unit, BulletType> bulletFilter = unit -> bullets[0];

	public Func<Unit, Color> tint = unit -> Color.white;

	public String[] icons = new String[]{""};

	public TextureRegion liquidRegion;
	public TextureRegion[] iconRegions;

	@Override
	public void addStats(UnitType u, Table t) {
		if (inaccuracy > 0) {
			t.row();
			t.add("[lightgray]" + Stat.inaccuracy.localized() + ": [white]" + (int) inaccuracy + " " + StatUnit.degrees.localized());
		}
		if (!alwaysContinuous && reload > 0) {
			t.row();
			t.add("[lightgray]" + Stat.reload.localized() + ": " + (mirror ? "2x " : "") + "[white]" + Strings.autoFixed(60f / reload * shoot.shots, 2) + " " + StatUnit.perSecond.localized());
		}

		t.row();
		t.table(Styles.grayPanel, weapon -> {
			for (int i = 0; i < bullets.length; i++) {
				int finalI = i;
				weapon.table(Tex.underline, b -> {
					b.left();
					if (iconRegions[finalI].found()) b.image(iconRegions[finalI]).padRight(10).center();
					StatValues.ammo(ObjectMap.of(u, bullets[finalI])).display(b.add(new Table()).get());
				}).growX().row();
			}
		}).margin(10f);
	}

  @Override
  public void draw(Unit unit, WeaponMount mount) {
		super.draw(unit, mount);
	  float z = Draw.z();
	  Draw.z(z + layerOffset);

	  float
		  rotation = unit.rotation - 90,
		  realRecoil = Mathf.pow(mount.recoil, recoilPow) * recoil,
		  weaponRotation  = rotation + (rotate ? mount.rotation : baseRotation),
		  wx = unit.x + Angles.trnsx(rotation, x, y) + Angles.trnsx(weaponRotation, 0, -realRecoil),
		  wy = unit.y + Angles.trnsy(rotation, x, y) + Angles.trnsy(weaponRotation, 0, -realRecoil);

		if (liquidRegion.found()) {
			Draw.color(tint.get(unit));
			Draw.rect(liquidRegion, wx, wy, weaponRotation);
		}
		Draw.z(z);
  }

	@Override
	public void init() {
		super.init();

		for (BulletType bullet : bullets) bullet.init();
	}

	@Override
	public void load() {
		super.load();
		for (BulletType bullet : bullets) bullet.load();

		liquidRegion = Core.atlas.find(name + "-liquid");

		iconRegions = new TextureRegion[bullets.length];
		for (int i = 0; i < iconRegions.length; i++) {
			if (i < icons.length) {
				iconRegions[i] = Core.atlas.find(icons[i]);
			} else {
				iconRegions[i] = Core.atlas.find("error");
			}
		}
	}

	@Override
	protected void shoot(Unit unit, WeaponMount mount, float shootX, float shootY, float rotation) {
		bullet = bulletFilter.get(unit);
		super.shoot(unit, mount, shootX, shootY, rotation);
	}
}
