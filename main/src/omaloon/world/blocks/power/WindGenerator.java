package omaloon.world.blocks.power;

import arc.graphics.*;
import arc.math.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
import arc.util.noise.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.world.blocks.power.*;
import mindustry.world.meta.*;

public class WindGenerator extends PowerGenerator{
    public float boostWeather = 0.25f;
    public float rotateSpeed = 1.0f, speedBoost = 3.0f;

    public WindGenerator(String name){
        super(name);
        flags = EnumSet.of();
        envEnabled = Env.any;
    }

    @Override
    public void setBars(){
        super.setBars();
        this.<WindGeneratorBuild>addBar("test",it-> {
            return new Bar(()->String.valueOf(it.baseRotation()), ()->Tmp.c1.fromHsv(182.18182f,0.4700855f,0.91764706f), () -> it.baseRotation()/360f);
        });
    }

    @Override
    public void setStats(){
        super.setStats();

        stats.remove(generationType);
        stats.add(generationType, powerProduction * 60.0f, StatUnit.powerSecond);
    }


    public class WindGeneratorBuild extends GeneratorBuild{
        public float boost = 0.0f;
        @Override
        public void updateTile(){
            if(enabled){
                boost = Mathf.lerpDelta(boost, !Groups.weather.isEmpty() ? 1.1f : 0.0f, 0.05f);
                productionEfficiency = 1 + (boostWeather * boost);
            }
        }

        public float baseRotation(){
//            return (Ridged.noise3d(0,x,y, Time.time,0.005f)+1f)/1.25f*360f;
            Building next= Vars.world.build(tileX()+2,tileY());
            if(next instanceof WindGeneratorBuild){
                return ((WindGeneratorBuild)next).baseRotation()+90f/4f;
            }
            return rotation*45.0f/2f;
        }

        @Override
        public void write(Writes write){
            super.write(write);
            write.f(boost);
        }

        @Override
        public void read(Reads read, byte revision){
            super.read(read, revision);
            boost = read.f();
        }
    }
}