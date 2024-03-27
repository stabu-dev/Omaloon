package omaloon.type.weather;

import arc.math.Mathf;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.WeatherState;

public class EffectWeather extends SpawnerWeather{
    public Effect WeatherFx = Fx.none;
    public EffectWeather(String name) {
        super(name);
    }

    @Override
    public void spawnAt(WeatherState state, float x, float y) {
        WeatherFx.at(x, y, Mathf.angle(xspeed, yspeed));
    }
}
