package omaloon.entities.comp;

import mindustry.entities.*;
import mindustry.gen.*;
import omaloon.annotations.Annotations.*;

@EntityComponent
abstract class FloatMechComp implements Unitc, Mechc {
    @Replace(1)
    @Override
    public EntityCollisions.SolidPred solidity() {
        return null;
    }
}
