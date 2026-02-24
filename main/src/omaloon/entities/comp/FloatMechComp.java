package omaloon.entities.comp;

import arc.math.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.type.*;
import omaloon.annotations.Annotations.*;

@EntityComponent
abstract class FloatMechComp implements Unitc, Mechc{
    @Import
    UnitType type;
    @Import
    float elevation;

    @Override
    public void update(){
        elevation = Mathf.approachDelta(elevation, onSolid() ? 1f : 0f, type.riseSpeed);
    }

    @Replace(1)
    @Override
    public EntityCollisions.SolidPred solidity(){
        return null;
    }
}