package omaloon.entities.comp;

import ent.anno.Annotations.*;
import mindustry.gen.*;
import omaloon.gen.*;
import omaloon.world.graph.*;

@EntityComponent(base = true)
@EntityDef(value = {PressureUpdaterc.class}, genIO = false)
abstract class PressureUpdaterComp implements Entityc {
	transient PressureLiquidGraph graph;

	@Override
	public void update() {
		if (graph == null) {
			remove();
		} else {
			graph.update();
		}
	}
}
