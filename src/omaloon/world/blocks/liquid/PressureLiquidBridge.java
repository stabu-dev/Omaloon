package omaloon.world.blocks.liquid;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.world.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

public class PressureLiquidBridge extends Block {
	public PressureConfig pressureConfig = new PressureConfig();

	public TextureRegion end, bridge, endBottom, bridgeBottom, endLiquid, bridgeLiquid;

	public PressureLiquidBridge(String name) {
		super(name);
		hasLiquids = true;

		config(Point2.class, (PressureLiquidBridgeBuild tile, Point2 i) -> tile.link = Point2.pack(i.x + tile.tileX(), i.y + tile.tileY()));
		config(Integer.class, (PressureLiquidBridgeBuild tile, Integer i) -> tile.link = i);
		configClear((PressureLiquidBridgeBuild b) -> b.link = -1);
	}

	@Override
	public void load() {
		super.load();
		end = Core.atlas.find(name + "-bridge-end");
		endBottom = Core.atlas.find(name + "-bridge-end-bottom");
		endLiquid = Core.atlas.find(name + "-bridge-end-liquid");
		bridge = Core.atlas.find(name + "-bridge-bridge");
		bridgeBottom = Core.atlas.find(name + "-bridge-bridge-bottom");
		bridgeLiquid = Core.atlas.find(name + "-bridge-bridge-liquid");
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
		public boolean onConfigureBuildTapped(Building other) {
			if (other instanceof PressureLiquidBridgeBuild b) {
				link = b.pos();
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
