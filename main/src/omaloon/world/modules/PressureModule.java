package omaloon.world.modules;

import arc.math.*;
import arc.util.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.type.*;
import mindustry.world.modules.*;
import omaloon.world.graph.*;
import omaloon.world.meta.*;

public class PressureModule extends BlockModule{
    public PressureGraph graph = new PressureGraph();
    public PressureTank section = new PressureTank();

    public float[] liquids = new float[Vars.content.liquids().size + 1];
    public float[] pressures = new float[Vars.content.liquids().size + 1];
    protected @Nullable Liquid mainCache;
    protected boolean cacheDirty = true;

    public float getAmount(int liquid){
        return liquids[liquid + 1];
    }

    /**
     * @return The fluid with the greatest amount in the building, null if air.
     */
    public @Nullable Liquid getMain(){
        if(!cacheDirty) return mainCache;

        float val = 0;
        int out = -1;
        for(int i = -1; i < liquids.length - 1; i++){
            float amount = getAmount(i);
            if(amount > val && !Mathf.zero(amount)){
                val = amount;
                out = i;
            }
        }
        cacheDirty = false;
        mainCache = Vars.content.liquid(out);
        return mainCache;
    }

    public float getPressure(int liquid){
        return pressures[liquid + 1];
    }

    @Override
    public void read(Reads read){
        byte size = read.b();
        for(int i = -1; i < size - 1; i++){
            float amount = read.f();
            float pressure = read.f();

            // tempting, but do not change it to break
            if(i >= liquids.length) continue;

            setAmount(i, amount);
            setPressure(i, pressure);
        }
    }

    public void setAmount(int liquid, float amount){
        if(liquids[liquid + 1] != amount){
            liquids[liquid + 1] = amount;
            cacheDirty = true;
        }
    }

    public void setPressure(int liquid, float amount){
        pressures[liquid + 1] = amount;
    }

    public float sumPressure(){
        float out = 0;
        for(float val : pressures) out += val;
        return out;
    }

    @Override
    public void write(Writes write){
        write.b(liquids.length);

        for(int i = -1; i < liquids.length - 1; i++){
            write.f(getAmount(i));
            write.f(getPressure(i));
        }
    }
}
