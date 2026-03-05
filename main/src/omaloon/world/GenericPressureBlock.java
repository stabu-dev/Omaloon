package omaloon.world;

import arc.util.io.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.world.*;
import omaloon.world.graph.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

/**
 * A block class containing the necessary methods to support pressure,
 * it adds no new other functionality, so extend this instead of Block for a new block class.
 */
public class GenericPressureBlock extends Block{
    public PressureConfig pressureConfig = new PressureConfig();

    public GenericPressureBlock(String name){
        super(name);
        hasLiquids = true;
    }

    @Override
    public void init(){
        if(hasLiquids){
            hasLiquids = false;
            pressureConfig.hasPressure = true;
        }
        super.init();
        if(hasLiquids){
            hasLiquids = false;
        }
    }

    @Override
    public void setBars(){
        super.setBars();
        pressureConfig.addBars(this);
    }

    @Override
    public void setStats(){
        super.setStats();
        pressureConfig.addStats(this, stats);
    }

    public class GenericPressureBlockBuild extends Building implements HasPressure{
        public PressureModule pressure;

        @Override
        public Building create(Block block, Team team){
            super.create(block, team);
            if(pressureConfig().hasPressure){
                pressure = new PressureModule();
                pressureGraph().addRaw(this);
            }
            return this;
        }

        @Override
        public void onProximityUpdate(){
            super.onProximityUpdate();
            if(pressureConfig.hasPressure){
                new PressureGraph().floodMergeGraph(this);
            }
        }

        @Override
        public PressureModule pressure(){
            return pressure;
        }

        @Override
        public PressureConfig pressureConfig(){
            return pressureConfig;
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            if(pressureConfig.hasPressure){
                (pressure == null ? new PressureModule() : pressure).read(read);
            }
        }

        @Override
        public void write(Writes write){
            super.write(write);
            if(pressureConfig.hasPressure){
                pressure.write(write);
            }
        }
    }
}
