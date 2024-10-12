package omaloon.entities.comp;

import arc.math.*;
import ent.anno.Annotations.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.type.*;

@EntityComponent
abstract  class FloatMechComp implements Unitc, Mechc {
    @Import UnitType type;
    @Import float elevation;

    @Replace(1)
    @Override
    public EntityCollisions.SolidPred solidity() {
        return null;
    }

    @Override
    public void update() {
        elevation = Mathf.approachDelta(elevation, onSolid() ? 1f : 0f, type.riseSpeed);
    }
}
