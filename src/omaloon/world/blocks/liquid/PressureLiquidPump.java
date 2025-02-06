package omaloon.world.blocks.liquid;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.meta.*;
import mindustry.world.blocks.liquid.*;
import omaloon.annotations.Load;
import omaloon.content.*;
import omaloon.math.*;
import omaloon.utils.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

import static mindustry.Vars.*;
import static mindustry.type.Liquid.*;

public class PressureLiquidPump extends Block {
	public PressureConfig pressureConfig = new PressureConfig();

	public float pumpStrength = 0.1f;

	public float pressureDifference = 10;

	public float liquidPadding = 3f;

	public float effectInterval = 5f;
	public Effect pumpEffectOut = Fx.none;
	public Effect pumpEffectIn = Fx.none;

	public TextureRegion[][] liquidRegions;
	public TextureRegion[] tiles;
	public TextureRegion topRegion, bottomRegion, filterRegion;
	@Load("@-arrow") public TextureRegion arrowRegion;

	public PressureLiquidPump(String name) {
		super(name);
		rotate = true;
		destructible = true;
		update = true;
		saveConfig = copyConfig = true;
		config(Liquid.class, (PressureLiquidPumpBuild build, Liquid liquid) -> {
			build.filter = liquid == null ? -1 : liquid.id;
		});
	}

	@Override
	public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
		var tiling = new Object() {
			int tiling = 0;
		};
		Point2
			front = new Point2(1, 0).rotate(plan.rotation).add(plan.x, plan.y),
			back = new Point2(-1, 0).rotate(plan.rotation).add(plan.x, plan.y);

		boolean inverted = plan.rotation == 1 || plan.rotation == 2;
		list.each(next -> {
			if (!(next.block instanceof PressureLiquidPump)) {
				if (new Point2(next.x, next.y).equals(front) && next.block.outputsLiquid) tiling.tiling |= inverted ? 2 : 1;
				if (new Point2(next.x, next.y).equals(back) && next.block.outputsLiquid) tiling.tiling |= inverted ? 1 : 2;
			}
		});

		Draw.rect(bottomRegion, plan.drawx(), plan.drawy(), 0);
		if (tiling.tiling != 0) Draw.rect(arrowRegion, plan.drawx(), plan.drawy(), (plan.rotation) * 90f);
		Draw.rect(tiles[tiling.tiling], plan.drawx(), plan.drawy(), (plan.rotation + 1) * 90f % 180 - 90);
		if (tiling.tiling == 0) Draw.rect(topRegion, plan.drawx(), plan.drawy(), (plan.rotation) * 90f);
	}

	@Override public TextureRegion[] icons() {
		return new TextureRegion[]{region, topRegion};
	}

	@Override
	public void init() {
		super.init();

		pressureConfig.fluidGroup = FluidGroup.pumps;
	}

	@Override
	public void load() {
		super.load();
		tiles = OlUtils.split(name + "-tiles", 32, 0);
		arrowRegion = Core.atlas.find(name + "-arrow");
		topRegion = Core.atlas.find(name + "-top");
		filterRegion = Core.atlas.find(name + "-filter");
		bottomRegion = Core.atlas.find(name + "-bottom", "omaloon-liquid-bottom");

		liquidRegions = new TextureRegion[2][animationFrames];
		if(renderer != null){
			var frames = renderer.getFluidFrames();

			for (int fluid = 0; fluid < 2; fluid++) {
				for (int frame = 0; frame < animationFrames; frame++) {
					TextureRegion base = frames[fluid][frame];
					TextureRegion result = new TextureRegion();
					result.set(base);

					result.setHeight(result.height - liquidPadding);
					result.setWidth(result.width - liquidPadding);
					result.setX(result.getX() + liquidPadding);
					result.setY(result.getY() + liquidPadding);

					liquidRegions[fluid][frame] = result;
				}
			}
		}
	}

	@Override
	public void setStats() {
		super.setStats();
		pressureConfig.addStats(stats);
		stats.remove(OlStats.fluidCapacity);
		stats.add(OlStats.pumpStrength, pumpStrength * 60f, StatUnit.liquidSecond);
		stats.add(OlStats.pressureGradient, OlStats.pressure(pressureDifference, true));
	}

	public class PressureLiquidPumpBuild extends Building implements HasPressure {
		PressureModule pressure = new PressureModule();

		public float effectTimer;
		public int tiling;
		public float smoothAlpha;

		public boolean functioning;

		public int filter = -1;

		@Override public boolean acceptsPressurizedFluid(HasPressure from, @Nullable Liquid liquid, float amount) {
			return false;
		}

		@Override
		public float ambientVolume() {
			return 1f/chainSize();
		}

		@Override
		public void buildConfiguration(Table table) {
			ItemSelection.buildTable(table, Vars.content.liquids(), () -> Vars.content.liquid(filter), other -> filter = other == null ? -1 : other.id);
		}

		/**
		 * Returns the length of the pump chain
		 */
		public int chainSize() {
			return pressure.section.builds.size;
		}

		@Override
		public Liquid config() {
			return content.liquid(filter);
		}

		@Override public boolean connects(HasPressure to) {
			return HasPressure.super.connects(to) && (front() == to || back() == to) && (!(to instanceof PressureLiquidPumpBuild) || to.rotation() == rotation);
		}

		@Override
		public void draw() {
			float rot = rotate ? (90 + rotdeg()) % 180 - 90 : 0;
			if (tiling != 0) {
				Draw.rect(bottomRegion, x, y, rotdeg());

				HasPressure front = getTo();
				HasPressure back = getFrom();

				if (
					(front != null && front.pressure().getMain() != null) ||
					(back != null && back.pressure().getMain() != null)
				) {

					Color tmpColor = Tmp.c1;
					if (front != null && front.pressure().getMain() != null) {
						tmpColor.set(front.pressure().getMain().color);
					} else if (back != null && back.pressure().getMain() != null) {
						tmpColor.set(back.pressure().getMain().color);
					}

					if (
						front != null && front.pressure().getMain() != null &&
						back != null && back.pressure().getMain() != null
					) tmpColor.lerp(back.pressure().getMain().color, 0.5f);


					float alpha =
						(front != null && front.pressure().getMain() != null ? Mathf.clamp(front.pressure().liquids[front.pressure().getMain().id]/(front.pressure().liquids[front.pressure().getMain().id] + front.pressure().air)) : 0) +
						(back != null && back.pressure().getMain() != null ? Mathf.clamp(back.pressure().liquids[back.pressure().getMain().id]/(back.pressure().liquids[back.pressure().getMain().id] + back.pressure().air)) : 0);
					alpha /= ((front == null ? 0 : 1f) + (back == null ? 0 : 1f));

					smoothAlpha = Mathf.approachDelta(smoothAlpha, alpha, PressureModule.smoothingSpeed);

					Liquid drawLiquid = Liquids.water;
					if (front != null && front.pressure().getMain() != null) {
						drawLiquid = front.pressure().current;
					} else if (back != null && back.pressure().getMain() != null) {
						drawLiquid = back.pressure().current;
					}

					int frame = drawLiquid.getAnimationFrame();
					int gas = drawLiquid.gas ? 1 : 0;

					float xscl = Draw.xscl, yscl = Draw.yscl;
					Draw.scl(1f, 1f);
					Drawf.liquid(liquidRegions[gas][frame], x, y, smoothAlpha, tmpColor);
					Draw.scl(xscl, yscl);
				}
				Draw.rect(arrowRegion, x, y, rotdeg());
			}
			Draw.rect(tiles[tiling], x, y, rot);
			if (filterRegion.found() && configurable && content.liquid(filter) != null) {
				Draw.color(content.liquid(filter).color);
				Draw.rect(filterRegion, x, y, rot);
				Draw.color();
			}
			if (tiling == 0) Draw.rect(topRegion, x, y, rotdeg());
		}

		/**
		 * Returns the building at the start of the pump chain.
		 */
		public @Nullable HasPressure getFrom() {
			PressureLiquidPumpBuild last = this;
			HasPressure out = back() instanceof HasPressure back ? back.getPressureDestination(last, 0) : null;
			while (out instanceof PressureLiquidPumpBuild pump) {
				if (!pump.connected(last)) return null;
				last = pump;
				out = pump.back() instanceof HasPressure back ? back.getPressureDestination(last, 0) : null;
			}
			return (out != null && out.connected(last)) ? out : null;
		}
		/**
		 * Returns the building at the end of the pump chain.
		 */
		public @Nullable HasPressure getTo() {
			PressureLiquidPumpBuild last = this;
			HasPressure out = front() instanceof HasPressure front ? front.getPressureDestination(last, 0) : null;
			while (out instanceof PressureLiquidPumpBuild pump) {
				if (!pump.connected(last)) return null;
				last = pump;
				out = pump.front() instanceof HasPressure front ? front.getPressureDestination(last, 0) : null;
			}
			return (out != null && out.connected(last)) ? out : null;
		}

		@Override
		public void onProximityUpdate() {
			super.onProximityUpdate();

			tiling = 0;
			boolean inverted = rotation == 1 || rotation == 2;
			if (front() instanceof HasPressure front && connected(front)) tiling |= inverted ? 2 : 1;
			if (back() instanceof HasPressure back && connected(back)) tiling |= inverted ? 1 : 2;

			new PressureSection().mergeFlood(this);
		}

		@Override public boolean outputsPressurizedFluid(HasPressure to, @Nullable Liquid liquid, float amount) {
			return false;
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
			filter = read.i();
			smoothAlpha = read.f();
		}

		@Override
		public boolean shouldAmbientSound() {
			return functioning;
		}

		@Override
		public void updateTile() {
			if (efficiency > 0) {
				HasPressure front = getTo();
				HasPressure back = getFrom();

				@Nullable Liquid pumpLiquid = configurable ? Vars.content.liquid(filter) : (back == null ? null : back.pressure().getMain());

				float frontPressure = front == null ? 0 : front.pressure().getPressure(pumpLiquid);
				float backPressure = back == null ? 0 : back.pressure().getPressure(pumpLiquid);

				float maxFlow = OlMath.flowRate(
					backPressure + pressureDifference * chainSize(),
					frontPressure,
					back == null ? 5 : back.pressureConfig().fluidCapacity,
					front == null ? 5 : front.pressureConfig().fluidCapacity,
					OlLiquids.getDensity(pumpLiquid),
					1
				);

				if (back != null) {
					pressure.pressure = back.pressure().getPressure(pumpLiquid);
					updatePressure();
				}
				if (front != null) {
					pressure.pressure = front.pressure().getPressure(pumpLiquid);
					updatePressure();
				}
				pressure.pressure = 0;

				float flow = Mathf.clamp(
					(maxFlow > 0 ? pumpStrength : -pumpStrength)/chainSize() * Time.delta,
					-Math.abs(maxFlow),
					Math.abs(maxFlow)
				);

				if (effectTimer >= effectInterval && !Mathf.zero(flow, 0.001f)) {
					if (flow < 0) {
						if (pumpLiquid == null || (front != null && front.pressure().get(pumpLiquid) > 0.001f)) {
							if (back == null && !(back() instanceof PressureLiquidPumpBuild p && p.rotation == rotation)) pumpEffectOut.at(x, y, rotdeg() + 180f, pumpLiquid == null ? Color.white : pumpLiquid.color);
							if (front == null && !(front() instanceof PressureLiquidPumpBuild p && p.rotation == rotation)) pumpEffectIn.at(x, y, rotdeg(), pumpLiquid == null ? Color.white : pumpLiquid.color);
						}
					} else {
						if (pumpLiquid == null || (back != null && back.pressure().get(pumpLiquid) > 0.001f)) {
							if (back == null && !(back() instanceof PressureLiquidPumpBuild p && p.rotation == rotation)) pumpEffectIn.at(x, y, rotdeg() + 180f, pumpLiquid == null ? Color.white : pumpLiquid.color);
							if (front == null && !(front() instanceof PressureLiquidPumpBuild p && p.rotation == rotation)) pumpEffectOut.at(x, y, rotdeg(), pumpLiquid == null ? Color.white : pumpLiquid.color);
						}
					}
					effectTimer %= 1;
				}

				functioning = !Mathf.zero(flow, 0.001f);

				if (
					front == null || back == null ||
					(front.acceptsPressurizedFluid(back, pumpLiquid, flow) &&
					back.outputsPressurizedFluid(front, pumpLiquid, flow))
				) {
					effectTimer += edelta();
					if (front != null) front.addFluid(pumpLiquid, flow);
					if (back != null) back.removeFluid(pumpLiquid, flow);
				}
			}
		}

		@Override
		public void updatePressure() {
			if (pressure().pressure < pressureConfig().minPressure - 1f) damage(pressureConfig().underPressureDamage);
			if (pressure().pressure > pressureConfig().maxPressure + 1f) damage(pressureConfig().overPressureDamage);
		}

		@Override
		public void write(Writes write) {
			super.write(write);
			pressure.write(write);
			write.i(filter);
			write.f(smoothAlpha);
		}
	}
}
