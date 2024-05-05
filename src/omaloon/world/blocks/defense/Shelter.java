package omaloon.world.blocks.defense;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import arclibrary.graphics.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.consumers.*;
import mindustry.world.meta.*;
import omaloon.world.blocks.production.OlDrill.*;
import omaloon.world.blocks.production.OlGenericCrafter.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

import static arc.Core.*;

public class Shelter extends Block {
	public TextureRegion baseRegion;

	public PressureConfig pressureConfig = new PressureConfig();

	public int rotations = 8;
	public float deflectAlpha = 0.7f;
	public float shieldAngle = 120f;
	public float shieldHealth = 120f;
	public float shieldRange = 80f;
	public float rechargeStandard = 0.1f;
	public float rechargeBroken = 1f;
	public float warmupTime = 0.1f;
	public boolean useConsumerMultiplier = true;
	public Color deflectColor = Pal.heal;
	private static final FrameBuffer fieldBuffer = new FrameBuffer();
	public static final Seq<Runnable> runs = new Seq<>();

	{
		Events.run(EventType.Trigger.draw, () -> {
			fieldBuffer.resize(graphics.getWidth(), graphics.getHeight());
			Seq<Runnable> buffer = runs.copy();
			runs.clear();

			Draw.draw(Layer.shields, () -> {
				Draw.flush();
				fieldBuffer.begin(Color.clear);
				buffer.each(Runnable::run);
				fieldBuffer.end();
				Draw.color(deflectColor, deflectAlpha);
				EDraw.drawBuffer(fieldBuffer);
				Draw.flush();
				Draw.color();
			});
		});
	}

	public Shelter(String name) {
		super(name);
		update = true;
		solid = true;
		configurable = true;
		saveConfig = true;
		hasLiquids = true;
		group = BlockGroup.projectors;
		ambientSound = Sounds.shield;
		ambientSoundVolume = 0.08f;
		config(Integer.class, (build, rot) -> ((ShelterBuild) build).rot = (rot + 8) % 8);
		configClear((ShelterBuild build) -> build.rot = 2);
	}

	@Override
	public void load() {
		super.load();
		baseRegion = Core.atlas.find(name + "-base", "block-" + size);
	}

	@Override
	public TextureRegion[] icons() {
		return new TextureRegion[]{baseRegion, region};
	}

	@Override
	public void init() {
		super.init();
		pressureConfig.linkBlackList.add(ShelterBuild.class, OlGenericCrafterBuild.class, OlDrillBuild.class);
	}

	@Override
	public void setStats() {
		super.setStats();
		pressureConfig.addStats(stats);

		stats.add(Stat.shieldHealth, shieldHealth, StatUnit.none);
		stats.add(Stat.cooldownTime, (int) (rechargeStandard * 60f), StatUnit.perSecond);
	}

	@Override
	public void setBars() {
		super.setBars();
		pressureConfig.addBars(this);
		addBar("shield", (ShelterBuild entity) -> new Bar("stat.shieldhealth", Pal.accent, () -> entity.broken ? 0f : 1 - entity.shieldDamage / shieldHealth).blink(Color.white));
	}

	@Override
	public void drawOverlay(float x, float y, int rotation) {
		Drawf.dashCircle(x, y, shieldRange, Pal.accent);
	}

	@Override
	public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
		Draw.rect(baseRegion, plan.drawx(), plan.drawy());
		int rot = plan.config instanceof Integer ? (int) plan.config : 2;
		Draw.rect(region, plan.drawx(), plan.drawy(), rot * 360f/rotations - 90f);
	}

	public class ShelterBuild extends Building implements HasPressure {
		public PressureModule pressure = new PressureModule();

		public int rot = 2;
		public float shieldDamage = 0;
		public float warmup = 0;
		public boolean broken = false;

		@Override
		public void buildConfiguration(Table table) {
			table.button(Icon.undo, () -> configure(rot + 1)).size(50f);
			table.button(Icon.redo, () -> configure(rot - 1)).size(50f);
		}

		@Override
		public Integer config() {
			return rot;
		}

		@Override
		public void draw() {
			Draw.rect(baseRegion, x, y, 0);
			Draw.rect(region, x, y, rot * 360f/rotations - 90f);
			runs.add(() -> {
				Draw.color();
				Fill.circle(x, y, warmup * (hitSize() * 1.2f));
				Fill.arc(x, y, shieldRange * warmup, shieldAngle/360f, -shieldAngle/2f + rot * 360f/rotations);
				Draw.color();
			});
		}

		@Override public float edelta() {
			return super.edelta() * efficiencyMultiplier();
		}

		public float efficiencyMultiplier() {
			float val = 1;
			if (!useConsumerMultiplier) return val;
			for (Consume consumer : consumers) {
				val *= consumer.efficiencyMultiplier(this);
			}
			return val;
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
		public void updateTile() {
			updateDeath();
			dumpPressure();
			if (efficiency > 0) {
				if (shieldDamage >= 0) {
					shieldDamage -= edelta() * (broken ? rechargeBroken : rechargeStandard);
				} else {
					broken = false;
				}

				if (broken) {
					warmup = Mathf.approachDelta(warmup, 0f, warmupTime);
				} else {
					warmup = Mathf.approachDelta(warmup, efficiency, warmupTime);
					Groups.bullet.intersect(x - shieldRange, y - shieldRange, shieldRange * 2f, shieldRange * 2f, b -> {
						if (b.team == Team.derelict) {
							float distance = Mathf.dst(x, y, b.x, b.y);
							float angle = Math.abs(((b.angleTo(x, y) - rot * 360f / rotations) % 360f + 360f) % 360f - 180f);
							boolean inWarmupRadius = distance <= warmup * (hitSize() * 1.4f);

							if ((distance <= shieldRange * warmup && angle <= shieldAngle / 2f) || inWarmupRadius) {
								b.absorb();
								shieldDamage += b.damage;
								if (shieldDamage >= shieldHealth) broken = true;
							}
						}
					});
				}
			} else {
				warmup = Mathf.approachDelta(warmup, 0f, warmupTime);
			}
		}

		@Override
		public void read(Reads read, byte revision) {
			super.read(read, revision);
			pressure.read(read);
			rot = read.i();
			shieldDamage = read.f();
			warmup = read.f();
			broken = read.bool();
		}
		@Override
		public void write(Writes write) {
			super.write(write);
			pressure.write(write);
			write.i(rot);
			write.f(shieldDamage);
			write.f(warmup);
			write.bool(broken);
		}
	}
}
