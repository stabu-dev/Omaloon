package omaloon.world.blocks.liquid;

import arc.*;
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
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.blocks.liquid.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

import static mindustry.Vars.*;

public class PressureLiquidBridge extends LiquidBlock {
	public PressureConfig pressureConfig = new PressureConfig();
	public float range = 32f;

	public @Nullable PressureLiquidBridgeBuild lastBuild;
	private BuildPlan otherReq;

	public TextureRegion end, bridge, endBottom, bridgeBottom, endLiquid, bridgeLiquid;

	public PressureLiquidBridge(String name) {
		super(name);
		configurable = true;
		saveConfig = true;
		copyConfig = false;

		config(Point2.class, (PressureLiquidBridgeBuild tile, Point2 i) -> tile.link = Point2.pack(i.x + tile.tileX(), i.y + tile.tileY()));
		config(Integer.class, (PressureLiquidBridgeBuild tile, Integer i) -> tile.link = i);
		configClear((PressureLiquidBridgeBuild b) -> b.link = -1);
	}

	@Override
	public void drawPlanConfigTop(BuildPlan plan, Eachable<BuildPlan> list) {
		otherReq = null;
		list.each(other -> {
			if(other.block == this && plan != other && plan.config instanceof Point2 p && p.equals(other.x - plan.x, other.y - plan.y)){
				otherReq = other;
			}
		});

		if(otherReq != null){
			drawBridge(plan.drawx(), plan.drawy(), otherReq.drawx(), otherReq.drawy());
		}
	}

	public void drawBridge(float x, float y, float ox, float oy) {
		if(Mathf.zero(Renderer.bridgeOpacity)) return;
		Draw.alpha(Renderer.bridgeOpacity);
		float rot = Angles.angle(x, y, ox, oy);
		Draw.rect(endBottom, x, y, rot);
		Draw.rect(endBottom, ox, oy, -8, 8, rot);
		Draw.rect(end, x, y, rot);
		Draw.rect(end, ox, oy, -8, 8, rot);

		Tmp.v1.trns(rot, 4);
		Lines.stroke(8f);
		Lines.line(bridgeBottom, x + Tmp.v1.x, y + Tmp.v1.y, ox - Tmp.v1.x, oy - Tmp.v1.y, false);
		Lines.line(bridge, x + Tmp.v1.x, y + Tmp.v1.y, ox - Tmp.v1.x, oy - Tmp.v1.y, false);
	}

	@Override
	public void drawOverlay(float x, float y, int rotation) {
		Drawf.dashCircle(x, y, range, Pal.placing);
	}

	public Tile findLink(int x, int y){
		Tile tile = world.tile(x, y);
		if(tile != null && lastBuild != null && linkValid(tile, lastBuild.tile, true) && lastBuild.tile != tile && lastBuild.link == -1){
			return lastBuild.tile;
		}
		return null;
	}

	@Override
	public void handlePlacementLine(Seq<BuildPlan> plans){
		for(int i = 0; i < plans.size - 1; i++){
			var cur = plans.get(i);
			var next = plans.get(i + 1);
			if(Mathf.dst(cur.drawx(), cur.drawy(), next.drawx(), next.drawy()) <= range){
				cur.config = new Point2(next.x - cur.x, next.y - cur.y);
			}
		}
	}

	@Override
	public TextureRegion[] icons() {
		return new TextureRegion[]{region};
	}

	public boolean linkValid(Tile tile, Tile other, boolean checkDouble){
		if(other == null || tile == null || tile.dst(other.drawx(), other.drawy()) > range) return false;

		return (
			(other.block() == tile.block() && tile.block() == this) || (!(tile.block() instanceof PressureLiquidBridge) && other.block() == this)
		) && (other.team() == tile.team() || tile.block() != this)
			&& (!checkDouble || ((PressureLiquidBridgeBuild)other.build).link != tile.pos());
	}

	@Override
	public void load() {
		super.load();
		end = Core.atlas.find(name + "-bridge-end");
		endBottom = Core.atlas.find(name + "-bridge-end-bottom");
		endLiquid = Core.atlas.find(name + "-bridge-end-liquid");
		bridge = Core.atlas.find(name + "-bridge");
		bridgeBottom = Core.atlas.find(name + "-bridge-bottom");
		bridgeLiquid = Core.atlas.find(name + "-bridge-liquid");
	}

	@Override
	public void setBars() {
		super.setBars();
		addBar("pressure", entity -> {
			HasPressure build = (HasPressure) entity;
			return new Bar(
				() -> Core.bundle.get("pressure") + Strings.fixed(build.getPressure(), 2),
				build::getBarColor,
				build::getPressureMap
			);
		});
	}

	@Override
	public void setStats() {
		super.setStats();
		pressureConfig.addStats(stats);
	}

	public class PressureLiquidBridgeBuild extends Building implements HasPressure {
		PressureModule pressure = new PressureModule();

		public int link = -1;

		@Override
		public Point2 config(){
			return Point2.unpack(link).sub(tile.x, tile.y);
		}

		@Override
		public void draw() {
			super.draw();
			Draw.z(Layer.blockOver);
			if (getLink() != null) drawBridge(x, y, getLink().x, getLink().y);
		}

		@Override
		public void drawSelect() {
			drawOverlay(x, y, rotation);
			if (getLink() != null) Drawf.select(getLink().x, getLink().y, 4, Pal.place);
		}

		@Override
		public void drawConfigure() {
			Drawf.select(x, y, 4, Pal.placing);
			drawOverlay(x, y, rotation);
			if (getLink() != null) Drawf.select(getLink().x, getLink().y, 4, Pal.place);
		}

		public @Nullable PressureLiquidBridgeBuild getLink() {
			return Vars.world.build(link) instanceof PressureLiquidBridgeBuild b ? b : null;
		}

		@Override
		public Seq<HasPressure> nextBuilds(boolean flow) {
			Seq<HasPressure> o = HasPressure.super.nextBuilds(flow);
			if (getLink() != null) o.add(getLink());
			return o;
		}

		@Override
		public boolean onConfigureBuildTapped(Building other){
			//reverse connection
			if(other instanceof ItemBridge.ItemBridgeBuild b && b.link == pos()){
				configure(other.pos());
				other.configure(-1);
				return true;
			}

			if(linkValid(tile, other.tile, true)){
				if(link == other.pos()){
					configure(-1);
				}else{
					configure(other.pos());
				}
				return false;
			}
			return true;
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

		@Override
		public void playerPlaced(Object config){
			super.playerPlaced(config);

			Tile link = findLink(tile.x, tile.y);
			if(linkValid(tile, link, true) && this.link != link.pos() && !proximity.contains(link.build)){
				link.build.configure(tile.pos());
			}

			lastBuild = this;
		}

		@Override public PressureModule pressure() {
			return pressure;
		}
		@Override public PressureConfig pressureConfig() {
			return pressureConfig;
		}

		@Override
		public void updateTile() {
			super.updateTile();
			updateDeath();
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
