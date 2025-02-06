package omaloon.type.weather;

import arc.math.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;

public class EffectWeather extends SpawnerWeather{
    public Effect weatherFx = Fx.none;

    public EffectWeather(String name){
        super(name);
        useWindVector = true;
    }

    @Override
    public void spawnAt(WeatherState state, float x, float y){
        weatherFx.at(x, y, Mathf.angle(state.windVector.x, state.windVector.y));
    }
}
