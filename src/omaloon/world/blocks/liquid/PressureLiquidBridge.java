package omaloon.world.blocks.liquid;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.core.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.sandbox.*;
import omaloon.world.blocks.distribution.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

import static mindustry.Vars.*;

public class PressureLiquidBridge extends TubeItemBridge {
	public PressureConfig pressureConfig = new PressureConfig();

	public TextureRegion
		endBottomRegion, endLiquidRegion,
		bridgeBottomRegion, bridgeLiquidRegion;

	public PressureLiquidBridge(String name) {
		super(name);
		hasLiquids = true;
		hasItems = false;
		outputsLiquid = true;
	}

	@Override
	public void drawBridge(BuildPlan req, float ox, float oy, float flip) {
		drawBridge(bridgeBottomRegion, endBottomRegion, new Vec2(req.drawx(), req.drawy()), new Vec2(ox, oy));
		drawBridge(bridgeRegion, endRegion, new Vec2(req.drawx(), req.drawy()), new Vec2(ox, oy));
	}

	@Override
	public void load() {
		super.load();
		endBottomRegion = Core.atlas.find(name + "-end-bottom");
		endLiquidRegion = Core.atlas.find(name + "-end-liquid");
		bridgeBottomRegion = Core.atlas.find(name + "-bridge-bottom");
		bridgeLiquidRegion = Core.atlas.find(name + "-bridge-liquid");
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

	public class PressureLiquidBridgeBuild extends TubeItemBridgeBuild implements HasPressure {
		PressureModule pressure = new PressureModule();

		@Override
		public boolean acceptLiquid(Building source, Liquid liquid) {
			return source.block.hasLiquids;
		}

		@Override
		public boolean canDumpLiquid(Building to, Liquid liquid) {
			return super.canDumpLiquid(to, liquid) || to instanceof LiquidVoid.LiquidVoidBuild;
		}

		@Override
		public void draw() {
			drawBase();

			Draw.z(Layer.power);
			Tile other = Vars.world.tile(link);
			Building build = Vars.world.build(link);
			if(build == this) build = null;
			if(build != null) other = build.tile;
			if(!linkValid(this.tile, other) || build == null || Mathf.zero(Renderer.bridgeOpacity)) return;
			Vec2 pos1 = new Vec2(x, y), pos2 = new Vec2(other.drawx(), other.drawy());

			if(pulse) Draw.color(Color.white, Color.black, Mathf.absin(Time.time, 6f, 0.07f));

			Draw.alpha(Renderer.bridgeOpacity);
			drawBridge(bridgeBottomRegion, endBottomRegion, pos1, pos2);
			Draw.color(liquids.current().color, liquids.currentAmount()/liquidCapacity * Renderer.bridgeOpacity);
			drawBridge(bridgeLiquidRegion, endLiquidRegion, pos1, pos2);
			Draw.color();
			Draw.alpha(Renderer.bridgeOpacity);
			drawBridge(bridgeRegion, endRegion, pos1, pos2);

			Draw.reset();
		}

		@Override
		public Seq<HasPressure> nextBuilds(boolean flow) {
			Seq<HasPressure> o = HasPressure.super.nextBuilds(flow);
			if (Vars.world.build(link) instanceof PressureLiquidBridgeBuild b) o.add(b);
			for(int pos : incoming.items) if (Vars.world.build(pos) instanceof PressureLiquidBridgeBuild b) o.add(b);
			return o;
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
			incoming.size = Math.min(incoming.size, maxConnections - (link == -1 ? 0 : 1));
			incoming.shrink();

			checkIncoming();

			nextBuilds(true).each(b -> moveLiquidPressure(b, liquids.current()));
			updateDeath();

			Tile other = world.tile(link);
			if(linkValid(tile, other)) {
				if(other.build instanceof TubeItemBridgeBuild && !cast(other.build).acceptIncoming(this.tile.pos())){
					configureAny(-1);
					return;
				}

				IntSeq inc = ((ItemBridgeBuild) other.build).incoming;
				int pos = tile.pos();
				if(!inc.contains(pos)){
					inc.add(pos);
				}

				warmup = Mathf.approachDelta(warmup, efficiency(), 1f / 30f);
				dumpPressure();
			}
		}

		@Override
		public void read(Reads read, byte revision) {
			super.read(read, revision);
			pressure.read(read);
		}
		@Override
		public void write(Writes write) {
			super.write(write);
			pressure.write(write);
		}
	}
}
