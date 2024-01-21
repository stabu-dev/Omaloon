package omaloon.world.graph;

import omaloon.gen.*;

public class PressureLiquidGraph {
	public PressureUpdater entity;

	public PressureLiquidGraph() {
		entity = PressureUpdater.create();
		entity.graph(this);
		entity.add();
	}

	public void update() {

	}
}
