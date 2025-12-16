package omaloon.type;

import arc.math.*;
import arc.util.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.type.*;

public class SpawnWeather extends Weather{
    public int spawns = 0;

    public SpawnWeather(String name) {
        super(name);
    }

    @Override
    public boolean isHidden(){
        return localizedName.equals(name) || hidden;
    }

    public boolean shouldSpawn(WeatherState state) {
        return true;
    }

    public void spawn(WeatherState state, float x, float y) {
    }

    @Override
    public void update(WeatherState state){
        if(Vars.net.client()) return;
        rand.setSeed((long)Time.time * Mathf.random(Vars.world.unitHeight(), Vars.world.unitWidth()));

        for(int spawn = 0; spawn < spawns; spawn++) {
            if (shouldSpawn(state)) {
                float rx = rand.random(0f, Vars.world.unitWidth());
                float ry = rand.random(0f, Vars.world.unitHeight());
                spawn(state, rx, ry);
            }
        }
    }
}
