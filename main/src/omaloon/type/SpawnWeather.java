package omaloon.type;

import arc.math.*;
import arc.util.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.type.*;

public class SpawnWeather extends Weather{
    public int spawns = 0;

    public SpawnWeather(String name){
        super(name);
    }

    @Override
    public boolean isHidden(){
        return localizedName.equals(name) || hidden;
    }

    public boolean shouldSpawn(WeatherState state){
        return true;
    }

    public void spawn(WeatherState state, float x, float y){
    }

    @Override
    public void update(WeatherState state){
        if(Vars.net.client()) return;

        float count = spawns * Time.delta;
        int attempts = (int)count + (Mathf.chance(count % 1f) ? 1 : 0);
        for(int spawn = 0; spawn < attempts; spawn++){
            if(shouldSpawn(state)){
                spawn(state, Mathf.random(Vars.world.unitWidth()), Mathf.random(Vars.world.unitHeight()));
            }
        }
    }
}
