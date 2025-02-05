package omaloon.world.blocks.liquid;

import arc.*;
import arc.func.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.input.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.distribution.*;
import omaloon.content.blocks.*;
import omaloon.utils.*;
import omaloon.world.blocks.liquid.PressureLiquidValve.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

import static mindustry.Vars.*;
import static mindustry.type.Liquid.*;

public class PressureLiquidConduit extends Block {
	public PressureConfig pressureConfig = new PressureConfig();

	public TextureRegion bottomRegion;
	public TextureRegion[] topRegions;
	public TextureRegion[][] liquidRegions;

	public float liquidPadding = 3f;

	public @Nullable Block junctionReplacement, bridgeReplacement;

	public PressureLiquidConduit(String name) {
		super(name);
		rotate = true;
		destructible = true;
		update = true;
	}

	@Override
	public void init(){
		super.init();

		if(junctionReplacement == null) junctionReplacement = OlDistributionBlocks.liquidJunction;
		if(bridgeReplacement == null || !(bridgeReplacement instanceof ItemBridge)) bridgeReplacement = OlDistributionBlocks.liquidBridge;

		if (pressureConfig.fluidGroup == null) pressureConfig.fluidGroup = FluidGroup.transportation;
	}

	@Override
	public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list) {
		int tiling = 0;
		BuildPlan[] proximity = new BuildPlan[4];

		list.each(next -> {
			for(int i = 0; i < 4; i++) {
				Point2 side = new Point2(plan.x, plan.y).add(Geometry.d4[i]);
				if(new Point2(next.x, next.y).equals(side) && (
						(next.block instanceof PressureLiquidConduit || next.block instanceof PressureLiquidPump || next.block instanceof PressureLiquidValve) ?
							(plan.rotation%2 == i%2 || next.rotation%2 == i%2) : (next.block.outputsLiquid))
				){
					proximity[i] = next;
					break;
				}
			}
		});

		for(int i = 0; i < 4; i++){
			if (proximity[i] != null) tiling |= (1 << i);
		}

		Draw.rect(bottomRegion, plan.drawx(), plan.drawy(), 0);
		if(tiling == 0){
			Draw.rect(topRegions[tiling], plan.drawx(), plan.drawy(), (plan.rotation + 1) * 90f % 180 - 90);
		}else{
			Draw.rect(topRegions[tiling], plan.drawx(), plan.drawy(), 0);
		}
	}

	@Override
	public void load() {
		super.load();

		topRegions = OlUtils.split(name + "-tiles", 32, 0);
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
	public Block getReplacement(BuildPlan req, Seq<BuildPlan> plans){
		if(junctionReplacement == null) return this;

		Boolf<Point2> cont = p -> plans.contains(o -> o.x == req.x + p.x && o.y == req.y + p.y && (req.block instanceof PressureLiquidConduit || req.block instanceof PressureLiquidJunction));
		return cont.get(Geometry.d4(req.rotation)) &&
				cont.get(Geometry.d4(req.rotation - 2)) &&
				req.tile() != null &&
				req.tile().block() instanceof PressureLiquidConduit &&
				Mathf.mod(req.build().rotation - req.rotation, 2) == 1 ? junctionReplacement : this;
	}

	@Override
	public void handlePlacementLine(Seq<BuildPlan> plans){
		if(bridgeReplacement == null) return;

		Placement.calculateBridges(plans, (ItemBridge)bridgeReplacement);
	}

	@Override
	public void setBars() {
		super.setBars();
		pressureConfig.addBars(this);
	}

	@Override
	public void setStats() {
		super.setStats();
		pressureConfig.addStats(stats);
	}

	public class PressureLiquidConduitBuild extends Building implements HasPressureImpl {

		public int tiling = 0;
		public float smoothAlpha;

		@Override
		public boolean acceptsPressurizedFluid(HasPressure from, @Nullable Liquid liquid, float amount) {
			return HasPressureImpl.super.acceptsPressurizedFluid(from, liquid, amount) && (liquid == pressure.getMain() || liquid == null || pressure.getMain() == null || from.pressure().getMain() == null);
		}

		@Override
		public boolean connects(HasPressure to) {
			return (
				to instanceof PressureLiquidConduitBuild || to instanceof PressureLiquidValveBuild) ?
			    (front() == to || back() == to || to.front() == this || to.back() == this) :
					to != null && HasPressureImpl.super.connects(to);
		}

		@Override
		public void draw() {
			Draw.rect(bottomRegion, x, y);
			Liquid main = pressure.getMain();

			smoothAlpha = Mathf.approachDelta(smoothAlpha, main == null ? 0f : pressure.liquids[main.id]/(pressure.liquids[main.id] + pressure.air), PressureModule.smoothingSpeed);

			if (smoothAlpha > 0.001f) {
				int frame = pressure.current.getAnimationFrame();
				int gas = pressure.current.gas ? 1 : 0;

				float xscl = Draw.xscl, yscl = Draw.yscl;
				Draw.scl(1f, 1f);
				Drawf.liquid(liquidRegions[gas][frame], x, y, Mathf.clamp(smoothAlpha), pressure.current.color.write(Tmp.c1).a(1f));
				Draw.scl(xscl, yscl);
			}
			Draw.rect(topRegions[tiling], x, y, tiling != 0 ? 0 : (rotdeg() + 90) % 180 - 90);
		}

		@Override
		public void onProximityUpdate() {
			super.onProximityUpdate();

			tiling = 0;
			for (int i = 0; i < 4; i++) {
				HasPressure build = nearby(i) instanceof HasPressure ? (HasPressure) nearby(i) : null;
				if (
					build != null && connected(build)
				) tiling |= (1 << i);
			}
		}

		@Override
		public boolean outputsPressurizedFluid(HasPressure to, Liquid liquid, float amount) {
			return HasPressureImpl.super.outputsPressurizedFluid(to, liquid, amount) && (liquid == to.pressure().getMain() || liquid == null || pressure.getMain() == null || to.pressure().getMain() == null);
		}

		@Override
		public void read(Reads read, byte revision) {
			super.read(read, revision);
			smoothAlpha = read.f();
		}

		@Override
		public void write(Writes write) {
			super.write(write);
			write.f(smoothAlpha);
		}
	}
}
