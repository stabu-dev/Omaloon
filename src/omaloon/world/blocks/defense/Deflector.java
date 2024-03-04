package omaloon.world.blocks.defense;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.layout.*;
import arc.util.io.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.*;
import mindustry.world.meta.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

public class Deflector extends Block {
	public TextureRegion baseRegion;

	public PressureConfig pressureConfig = new PressureConfig();

	public int rotations = 8;
	public float deflectAlpha = 0.2f;
	public float deflectAngle = 120f;
	public float deflectHealth = 120f;
	public float deflectRange = 80f;
	public float deflectRechargeStandard = 0.1f;
	public float deflectRechargeBroken = 1f;
	public float deflectWarmup = 0.1f;
	public Color deflectColor = Pal.heal;

	public Deflector(String name) {
		super(name);
		update = true;
		solid = true;
		configurable = true;
		hasLiquids = true;
		group = BlockGroup.projectors;
		ambientSound = Sounds.shield;
		ambientSoundVolume = 0.08f;
	}

	@Override
	public void load() {
		super.load();
		baseRegion = Core.atlas.find(name + "-base", "block-" + size);
	}

	public class DeflectorBuild extends Building implements HasPressure {
		public PressureModule pressure = new PressureModule();

		public int rot = 2;
		public float damageShield = 0;
		public float warmup = 0;
		public boolean broken = false;

		@Override
		public void buildConfiguration(Table table) {
			table.button(Icon.undo, () -> rot = (rot + 1 + 8) % 8).size(50f);
			table.button(Icon.redo, () -> rot = (rot - 1 + 8) % 8).size(50f);
		}

		@Override
		public void draw() {
			Draw.rect(baseRegion, x, y, 0);
			Draw.rect(region, x, y, rot * 360f/rotations - 90f);
			Draw.color(deflectColor, deflectAlpha);
			Draw.z(Layer.blockOver);
			Fill.arc(x, y, deflectRange * warmup, deflectAngle/360f, -deflectAngle/2f + rot * 360f/rotations);
		}

		@Override
		public void updateTile() {
			updateDeath();
			if (efficiency > 0) {
				if (damageShield >= 0) {
					damageShield -= edelta();
				} else {
					broken = false;
				}

				if (broken) {
					warmup = Mathf.approachDelta(warmup, 0f, deflectWarmup);
				} else {
					warmup = Mathf.approachDelta(warmup, efficiency, deflectWarmup);
					Groups.bullet.intersect(x - deflectRange, y - deflectRange, deflectRange * 2f, deflectRange * 2f, b -> {
						if (b.team == Team.derelict) {
							float distance = Mathf.dst(x, y, b.x, b.y);
							float angle = Math.abs(((b.angleTo(x, y) - rot * 360f / rotations) % 360f + 360f) % 360f - 180f);

							if (distance <= deflectRange && angle <= deflectAngle / 2f || distance <= size * 8) {
								b.absorb();
								damageShield += b.damage;
								if (damageShield >= deflectHealth) broken = true;
							}
						}
					});
				}
			} else {
				warmup = Mathf.approachDelta(warmup, 0f, deflectWarmup);
			}
		}

		@Override
		public void onProximityAdded() {
			super.onProximityAdded();
			pressureGraph().addBuild(this);
		}

		@Override
		public void onProximityRemoved() {
			super.onProximityRemoved();
			pressureGraph().removeBuild(this, true);
		}

		@Override
		public void onProximityUpdate() {
			super.onProximityUpdate();
			pressureGraph().removeBuild(this, false);
		}

		@Override public PressureModule pressure() {
			return pressure;
		}
		@Override public PressureConfig pressureConfig() {
			return pressureConfig;
		}

		@Override
		public void read(Reads read, byte revision) {
			super.read(read, revision);
			pressure.read(read);
			rot = read.i();
			damageShield = read.f();
			warmup = read.f();
			broken = read.bool();
		}
		@Override
		public void write(Writes write) {
			super.write(write);
			pressure.write(write);
			write.i(rot);
			write.f(damageShield);
			write.f(warmup);
			write.bool(broken);
		}
	}
}
