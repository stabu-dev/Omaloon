package omaloon.world.blocks.liquid;

import arc.*;
import arc.graphics.g2d.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.blocks.liquid.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

public class PressureLiquidPump extends LiquidBlock {
	public PressureConfig pressureConfig = new PressureConfig();

	public float pressureTransfer = 0.1f;
	public float frontMaxPressure = 100f;
	public float backMinPressure = 100f;

	public PressureLiquidPump(String name) {
		super(name);
		rotate = true;
	}

	@Override public TextureRegion[] icons() {
		return new TextureRegion[]{region, topRegion};
	}

	@Override
	public void setBars() {
		super.setBars();
		addBar("pressure", entity -> {
			HasPressure build = (HasPressure) entity;
			return new Bar(
				Core.bundle.get("pressure"),
				Pal.accent,
				build::getPressureMap
			);
		});
	}

	public class PressureLiquidPumpBuild extends LiquidBuild implements HasPressure {
		PressureModule pressure = new PressureModule();

		@Override
		public boolean acceptLiquid(Building source, Liquid liquid) {
			return super.acceptLiquid(source, liquid) && source instanceof HasPressure build && acceptsPressure(build, 0);
		}
		@Override
		public boolean acceptsPressure(HasPressure from, float pressure) {
			return HasPressure.super.acceptsPressure(from, pressure) && from == back();
		}

		@Override
		public boolean connects(HasPressure to) {
			return HasPressure.super.connects(to) && (to == front() || to == back());
		}

		@Override public boolean canDumpLiquid(Building to, Liquid liquid) {
			return super.canDumpLiquid(to, liquid) && to == front();
		}

		@Override
		public void draw() {
			Draw.rect(region, x, y, 0);
			Draw.rect(topRegion, x, y, rotdeg());
		}

		@Override
		public void updateTile() {
			super.updateTile();
			if (efficiency > 0
				&& front() instanceof HasPressure front
				&& back() instanceof HasPressure back
			) {
				if (liquids.currentAmount() > 0.001f) dumpLiquid(liquids.current());
				if (
					front.getPressure() < frontMaxPressure &&
					back.getPressure() > backMinPressure
				) {
					front.handlePressure(pressureTransfer * edelta());
					back.removePressure(pressureTransfer * edelta());
				}
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
