package omaloon.world.blocks.production;

import arc.util.io.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.blocks.production.*;
import mindustry.world.consumers.*;
import omaloon.world.graph.*;
import omaloon.world.interfaces.*;
import omaloon.world.meta.*;
import omaloon.world.meta.PressureTank.*;
import omaloon.world.modules.*;

public class PressureDrill extends Drill{
    public PressureConfig pressureConfig = new PressureConfig();

    public boolean useConsumerMultiplier = true;

    public PressureDrill(String name){
        super(name);
    }

    @Override
    public void init(){
        if(hasLiquids){
            hasLiquids = false;
            pressureConfig.hasPressure = true;
        }

        super.init();

        if(hasLiquids) hasLiquids = false;

        if(pressureConfig.group == null) pressureConfig.group = TankGroup.drills;
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

    public class PressureDrillBuild extends DrillBuild implements HasPressure{
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

        public float efficiencyMultiplier(){
            float val = 1;
            if(!useConsumerMultiplier) return val;
            for(Consume consumer : consumers){
                val *= consumer.efficiencyMultiplier(this);
            }
            return val;
        }

        @Override
        public float efficiencyScale(){
            return super.efficiencyScale() * efficiencyMultiplier();
        }

        @Override
        public float getProgressIncrease(float baseTime){
            return super.getProgressIncrease(baseTime) * efficiencyMultiplier();
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