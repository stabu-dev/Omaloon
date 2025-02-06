package omaloon.world.blocks.sandbox;

import mindustry.gen.*;
import mindustry.type.*;
import mindustry.world.blocks.sandbox.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;

public class PressureLiquidVoid extends LiquidVoid{
    public PressureConfig pressureConfig = new PressureConfig();

    public PressureLiquidVoid(String name){
        super(name);
    }

    public class PressureLiquidVoidBuild extends LiquidVoidBuild implements HasPressureImpl{


        @Override
        public boolean acceptLiquid(Building source, Liquid liquid){
            return enabled;
        }


        @Override
        public void updateTile(){
            super.updateTile();
            pressure.pressure = 0f;
        }
    }
}

