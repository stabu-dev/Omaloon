package omaloon.world.modules;

import arc.util.io.*;
import mindustry.*;
import mindustry.world.modules.*;
import omaloon.world.graph.*;
import omaloon.world.meta.*;

public class PressureModule extends BlockModule{
    public PressureGraph graph = new PressureGraph();
    public PressureTank section = new PressureTank();

    public float[] liquids = new float[Vars.content.liquids().size + 1];
    public float[] pressures = new float[Vars.content.liquids().size + 1];

    public float getAmount(int liquid){
        return liquids[liquid + 1];
    }

    public float getPressure(int liquid){
        return pressures[liquid + 1];
    }

    @Override
    public void read(Reads read){
        byte size = read.b();
        for(int i = 0; i < size; i++){
            // tempting, but do not change it to break
            if(i >= liquids.length) continue;

            float amount = read.f();
            float pressure = read.f();
            setAmount(i, amount);
            setPressure(i, pressure);
        }
    }

    public void setAmount(int liquid, float amount){
        liquids[liquid + 1] = amount;
    }

    public void setPressure(int liquid, float amount){
        liquids[liquid + 1] = amount;
    }

    @Override
    public void write(Writes write){
        write.b(liquids.length);

        for(int i = 0; i < liquids.length; i++){
            write.f(getAmount(i));
            write.f(getPressure(i));
        }
    }
}
