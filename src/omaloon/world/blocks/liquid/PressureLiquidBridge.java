package omaloon.world.blocks.liquid;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.blocks.sandbox.*;
import omaloon.world.blocks.distribution.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

public class PressureLiquidBridge extends TubeItemBridge {
	public PressureConfig pressureConfig = new PressureConfig();

	public TextureRegion
		end, endBottom, endLiquid,
		bridge, bridgeBottom, bridgeLiquid;

	public PressureLiquidBridge(String name) {
		super(name);
		hasLiquids = true;
		hasItems = false;
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
		public boolean canDumpLiquid(Building to, Liquid liquid) {
			return super.canDumpLiquid(to, liquid) || to instanceof LiquidVoid.LiquidVoidBuild;
		}

		@Override
		public void draw() {
			drawBase();
			HasPressure other = Vars.world.build(link) == null ? null : Vars.world.build(link).as();
			if (other != null) {
				float a = angleTo(other);
				Tmp.v1.trns(a, 4f);
				for (int i : Mathf.signs) {
					HasPressure pos = i == 0 ? this : other;
					if (i == -1) Draw.xscl = -1f;
					Draw.rect(endBottom, pos.x(), pos.y(), 90 - i * 90);
					Draw.color(liquids.current().color, liquids.currentAmount()/liquidCapacity);
					Draw.rect(endLiquid, pos.x(), pos.y(), 90 - i * 90);
					Draw.color();
					Draw.rect(end, pos.x(), pos.y(), 90 - i * 90);
				}
				Draw.reset();
				Lines.stroke(8f);
				Lines.line(bridgeBottom, x + Tmp.v1.x, y + Tmp.v1.y, other.x() - Tmp.v1.x, other.y() - Tmp.v1.y, false);
				Lines.line(bridgeLiquid, x + Tmp.v1.x, y + Tmp.v1.y, other.x() - Tmp.v1.x, other.y() - Tmp.v1.y, false);
				Lines.line(bridge, x + Tmp.v1.x, y + Tmp.v1.y, other.x() - Tmp.v1.x, other.y() - Tmp.v1.y, false);
			}
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
			super.updateTile();
			updateDeath();
			dumpPressure();
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
