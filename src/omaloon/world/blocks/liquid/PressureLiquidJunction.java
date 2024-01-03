package omaloon.world.blocks.liquid;

import arc.*;
import arc.graphics.g2d.*;
import arc.util.io.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.blocks.liquid.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

public class PressureLiquidJunction extends LiquidJunction {
	public PressureConfig pressureConfig = new PressureConfig();

	public TextureRegion liquidRegion, bottomRegion;

	public PressureLiquidJunction(String name) {
		super(name);
	}

	@Override
	public TextureRegion[] icons() {
		return new TextureRegion[]{bottomRegion, region};
	}

	@Override
	public void load() {
		super.load();
		bottomRegion = Core.atlas.find(name + "-bottom");
		liquidRegion = Core.atlas.find(name + "-liquid");
	}

	public class PressureLiquidJunctionBuild extends LiquidJunctionBuild implements HasPressure {
		PressureModule pressure = new PressureModule();

		@Override
		public void draw() {
			float rotation = rotate ? rotdeg() : 0;
			Draw.rect(bottomRegion, x, y, rotation);

			if(liquids.currentAmount() > 0.001f){
				Drawf.liquid(liquidRegion, x, y, liquids.currentAmount() / liquidCapacity, liquids.current().color);
			}

			Draw.rect(region, x, y, rotation);
		}

		@Override
		public HasPressure getPressureDestination(HasPressure source, float pressure) {
			if(!enabled) return this;

			int dir = (source.relativeTo(tile.x, tile.y) + 4) % 4;
			Building next = nearby(dir);
			if(next instanceof PressureLiquidJunctionBuild to && to.acceptsPressure(this, pressure)){
				return to.getPressureDestination(this, pressure);
			}
			return this;
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
