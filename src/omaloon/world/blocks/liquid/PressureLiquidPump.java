package omaloon.world.blocks.liquid;

import arc.graphics.g2d.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.blocks.liquid.*;
import omaloon.world.blocks.liquid.PressureLiquidJunction.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

public class PressureLiquidPump extends LiquidBlock {
	public PressureConfig pressureConfig = new PressureConfig();

	public float pressureTransfer = 0.1f;

	public PressureLiquidPump(String name) {
		super(name);
		rotate = true;
	}

	@Override public TextureRegion[] icons() {
		return new TextureRegion[]{region, topRegion};
	}

	public class PressureLiquidPumpBuild extends LiquidBuild implements HasPressure {
		PressureModule pressure = new PressureModule();

		@Override
		public void draw() {
			Draw.rect(region, x, y, 0);
			Draw.rect(topRegion, x, y, rotdeg());
		}

		@Override
		public Building getLiquidDestination(Building source, Liquid liquid){
			if(!enabled) return this;

			int dir = (source.relativeTo(tile.x, tile.y) + 4) % 4;
			Building next = nearby(dir);
			if(dir%2 == 0 || next == null || (!next.acceptLiquid(this, liquid) && !(next.block instanceof LiquidJunction))){
				return this;
			}
			return next.getLiquidDestination(this, liquid);
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

		@Override
		public void updateTile() {
			super.updateTile();
			if (efficiency > 0
				&& front() instanceof HasPressure front
				&& back() instanceof HasPressure back
			) {
				front.handlePressure(pressureTransfer * edelta());
				back.removePressure(pressureTransfer * edelta());
			}
		}

		@Override public PressureModule pressure() {
			return pressure;
		}
		@Override public PressureConfig pressureConfig() {
			return pressureConfig;
		}
	}
}
