package omaloon.world.blocks.liquid;

import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

public class PressureLiquidJunction extends Block {
	public PressureConfig pressureConfig = new PressureConfig();

	public PressureLiquidJunction(String name) {
		super(name);
		update = true;
		destructible = true;
	}

	public class PressureLiquidJunctionBuild extends Building implements HasPressure {
		PressureModule pressure = new PressureModule();

		@Override public boolean acceptsPressurizedFluid(HasPressure from, @Nullable Liquid liquid, float amount) {
			return false;
		}

		@Override
		public boolean connects(HasPressure to) {
			return HasPressure.super.connects(to) && !(to instanceof PressureLiquidPump);
		}

		@Override
		public HasPressure getPressureDestination(HasPressure source, float pressure) {
			if(!enabled) return this;

			int dir = (source.relativeTo(tile.x, tile.y) + 4) % 4;
			HasPressure next = nearby(dir) instanceof HasPressure ? (HasPressure) nearby(dir) : null;
			if(next == null) {
				return this;
			}
			return next.getPressureDestination(this, pressure);
		}

		@Override public HasPressure getSectionDestination(HasPressure from) {
			return null;
		}

		@Override
		public Seq<HasPressure> nextBuilds() {
			return Seq.with();
		}

		@Override
		public void onProximityUpdate() {
			super.onProximityUpdate();

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
		}
		@Override
		public void write(Writes write) {
			super.write(write);
			pressure.write(write);
		}
	}
}
