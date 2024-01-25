package omaloon.world.modules;

import arc.util.io.*;
import mindustry.world.modules.*;
import omaloon.world.graph.*;

public class PressureModule extends BlockModule {
	public PressureLiquidGraph graph = new PressureLiquidGraph();
	public float pressure;

	@Override
	public void read(Reads read) {
		pressure = read.f();
	}
	@Override
	public void write(Writes write) {
		write.f(pressure);
	}
}
