package omaloon.entities.comp;

import ent.anno.Annotations.*;
import mindustry.gen.*;
import omaloon.gen.*;

@EntityComponent
abstract class DroneComp implements Unitc {
	transient Masterc master;
}
