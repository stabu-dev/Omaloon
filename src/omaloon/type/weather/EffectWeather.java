package omaloon.type.weather;

import arc.math.Mathf;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.WeatherState;

public class EffectWeather extends SpawnerWeather{
    public Effect weatherFx = Fx.none;
    public EffectWeather(String name) {
        super(name);
        useWindVector = true;
    }

    @Override
    public void spawnAt(WeatherState state, float x, float y) {
        weatherFx.at(x, y, Mathf.angle(state.windVector.x, state.windVector.y));
    }
}
