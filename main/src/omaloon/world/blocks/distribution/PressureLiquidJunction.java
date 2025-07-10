package omaloon.world.blocks.distribution;

import arc.struct.*;
import arc.util.*;
import mindustry.type.*;
import omaloon.world.*;
import omaloon.world.interfaces.*;

public class PressureLiquidJunction extends GenericPressureBlock{
    public PressureLiquidJunction(String name){
        super(name);
        update = true;
        destructible = true;
    }

    public class PressureLiquidJunctionBuild extends GenericPressureBlockBuild{
        @Override
        public boolean acceptsFluid(HasPressure from, @Nullable Liquid liquid, float amount){
            return false;
        }

        @Override
        public boolean connects(HasPressure to){
            return false;
        }

        @Override
        public HasPressure getFluidDestination(HasPressure source, @Nullable Liquid fluid){
            if(!enabled) return this;

            int dir = (source.toBuilding().relativeTo(tile.x, tile.y) + 4) % 4;
            HasPressure next = nearby(dir) instanceof HasPressure ? (HasPressure)nearby(dir) : null;
            if(next == null){
                return this;
            }
            return next.getFluidDestination(this, fluid);
        }

        @Override
        public Seq<HasPressure> connections(){
            return Seq.with();
        }

        @Override
        public boolean outputsFluid(HasPressure to, @Nullable Liquid liquid, float amount){
            return false;
        }
    }
}
