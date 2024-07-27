package omaloon.entities.abilities;

import arc.*;
import arc.audio.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.meta.*;
import omaloon.content.*;

import static mindustry.Vars.*;

/**
 * An ability for a shield covering the unit that protects from hail, but not enemy bullets.
 */
public class HailShieldAbility extends Ability {
	/**
	 * Name to find region sprite.
	 */
	public String name = "";

	/**
	 * Position relative to unit.
	 */
	public float x, y;

	/**
	 * Shield radius, defaults to twice the unit's hitsize.
	 */
	public float radius = -1f;

	/**
	 * Shield regen amount per tick while active.
	 */
	public float regen = 0.1f;
	/**
	 * Shield regen amount per tick while broken.
	 */
	public float regenBroken = 1f;
	/**
	 * Maximum shield health.
	 */
	public float maxHealth = 100f;

	/**
	 * Radius where shield will rotate around. Visual only.
	 */
	public float spinRadius = -1f;
	/**
	 * Speed for spin, rotation, and base rotation. Visual only.
	 */
	public float spinSpeed = 1f, rotateSpeed = 1f, rotateBaseSpeed = 1f, particleSpeed = 0.02f;

	/**
	 * Number of particles.
	 */
	public int particles = 10;
	/**
	 * Min distance for particles.
	 */
	public float particleMinDst = 4f;
	/**
	 * Max distance for particles.
	 */
	public float particleDst = 10f;
	/**
	 * Angle offset of particle.
	 */
	public float particleCone = 10f;

	/**
	 * When true, effects and sounds will be played at the position of the shield instead of the ability's position.
	 */
	public boolean parentizeShield = true;

	/**
	 * Interpolation curve for particles.
	 */
	public Interp curve = Interp.exp10Out;

	/**
	 * Layer offset for ability.
	 */
	public float layerOffset = 0f;

	/**
	 * Effect displayed when something hits the shield.
	 */
	public Effect hitEffect = Fx.absorb;
	/**
	 * Effect displayed when the shield is broken.
	 */
	public Effect breakEffect = Fx.none;
	/**
	 * Effect displayed when the shield regenerates.
	 */
	public Effect regenEffect = Fx.none;

	/**
	 * Sound played when something hits the shield.
	 */
	public Sound hitSound = OlSounds.shelterPush;
	public float hitSoundVolume = 1;

	/**
	 * Color displayed in shield health bar.
	 */
	public Color barColor = Pal.heal;

	/**
	 * Color used in HitEffect;
	 */
	public Color hitColor = Color.white;

	public TextureRegion region, baseRegion;

	protected float damage;
	protected boolean broken;
	protected Rand rand = new Rand();

	@Override
	public void addStats(Table t) {
		t.add("[lightgray]" + Stat.health.localized() + ": [white]" + Math.round(maxHealth)).row();
		t.add("[lightgray]" + Stat.range.localized() + ": [white]" +  Strings.autoFixed(radius / tilesize, 2) + " " + StatUnit.blocks.localized()).row();
		t.add("[lightgray]" + Stat.repairSpeed.localized() + ": [white]" + Strings.autoFixed(regen * 60f, 2) + StatUnit.perSecond.localized()).row();
		t.add("[lightgray]" + Stat.cooldownTime.localized() + ": [white]" + Strings.autoFixed(maxHealth/regenBroken/60f, 2) + " " + StatUnit.seconds.localized()).row();
	}

	@Override
	public void death(Unit unit) {
		float
			dx = unit.x + x + (parentizeShield ? Angles.trnsx(Time.time * spinSpeed + unit.id, spinRadius) : 0f),
			dy = unit.y + y + (parentizeShield ? Angles.trnsy(Time.time * spinSpeed + unit.id, spinRadius) : 0f);

		breakEffect.at(dx, dy);
	}

	@Override
	public void displayBars(Unit unit, Table bars) {
		bars.add(new Bar("bar.hail-shield-health", barColor, () -> 1f - (damage/maxHealth)).blink(Color.white));
	}

	@Override
	public void draw(Unit unit) {
		if (broken) return;
		rand.setSeed(unit.id);
		float z = Draw.z();
		float opacity = Core.settings.getInt("@setting.omaloon-shield-opacity", 100)/100f;
		Draw.z(z + layerOffset);
		if (region == null || baseRegion == null) {
			region = Core.atlas.find(name, "omaloon-hail-shield");
			baseRegion = Core.atlas.find(name + "-base", "omaloon-hail-shield-base");
		}

		float
			dx = unit.x + x + Angles.trnsx(Time.time * spinSpeed + unit.id, spinRadius),
			dy = unit.y + y + Angles.trnsy(Time.time * spinSpeed + unit.id, spinRadius);

		Draw.alpha(0.2f * opacity);
		for (int side = 0; side < 4; side++) {
			for (int i = 0; i < particles; i++) {
				float
					angle = rand.range(particleCone) + 90f * side + (Time.time - unit.id) * rotateBaseSpeed,
					fin = curve.apply((rand.random(1f) + (Time.time + unit.id) * particleSpeed) % 1f),
					dst = particleMinDst + fin * particleDst;

				Fill.circle(
					dx + Angles.trnsx(angle, dst),
					dy + Angles.trnsy(angle, dst),
					2f * (1f - fin)
				);
			}
		}
		Draw.alpha(opacity);

		if (baseRegion.found()) Drawf.spinSprite(baseRegion, dx, dy, (Time.time - unit.id) * rotateBaseSpeed);
		Drawf.spinSprite(region, dx, dy, Time.time * rotateSpeed - unit.id);

		Draw.z(z);
	}

	@Override
	public void init(UnitType type) {
		if (radius == -1) radius = type.hitSize * 2f;
		if (spinRadius == -1) spinRadius = type.hitSize;
	}

	@Override
	public void update(Unit unit) {
		float
			dx = unit.x + x + (parentizeShield ? Angles.trnsx(Time.time * spinSpeed + unit.id, spinRadius) : 0f),
			dy = unit.y + y + (parentizeShield ? Angles.trnsy(Time.time * spinSpeed + unit.id, spinRadius) : 0f);

		if (broken) {
			if (damage > 0) {
				damage -= Time.delta * regenBroken;
			} else {
				broken = false;
				regenEffect.at(dx, dy);
			}
		} else {
			if (damage > 0) damage -= Time.delta * regen;
			Groups.bullet.intersect(unit.x + x - radius, unit.y + y - radius, radius * 2f, radius * 2f, b -> {
				if (b.team == Team.derelict) {
					if (Mathf.dst(unit.x + x, unit.y + y, b.x, b.y) <= radius) {
						b.absorb();
						hitEffect.at(b.x, b.y, b.hitSize, hitColor);
						hitSound.at(b.x, b.y, Mathf.random(0.9f, 1.1f), hitSoundVolume);
						damage += b.damage;
						if (damage > maxHealth) {
							broken = true;
							breakEffect.at(dx, dy);
						}
					}
				}
			});
		}
	}
}
