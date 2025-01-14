package omaloon.world.modules;

import arc.util.io.*;
import mindustry.world.modules.*;

public class PressureModule extends BlockModule {
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
