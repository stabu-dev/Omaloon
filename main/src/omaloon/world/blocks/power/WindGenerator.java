package omaloon.world.blocks.power;

import arc.math.*;
import arc.struct.*;
import arc.util.*;
import arc.util.io.*;
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
            float time = Time.time / 4.0f;
            float offset = this.id() * 10.0f;
            return offset + time + Mathf.lerp(0, 360, rotateSpeed);
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