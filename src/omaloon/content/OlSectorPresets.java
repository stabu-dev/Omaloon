package omaloon.content;

import arc.util.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.type.Weather.*;
import omaloon.type.*;

import static arc.util.Time.*;
import static omaloon.type.ExtraSectorPreset.getFlag;

public class OlSectorPresets{
    public static SectorPreset theCrater, redeploymentPath, deadValley;

    public static void load(){
        theCrater = new ExtraSectorPreset("crater", OlPlanets.glasmore, 492, () -> {
            if(getFlag("hail", true)){
                Vars.state.rules.weather.add(new WeatherEntry(OlWeathers.hailStone,
                2.5f * toMinutes, 5f * toMinutes,
                30f * Time.toSeconds, 1.5f * toMinutes
                ){{
                    always = true;
                }});
            }
            if(getFlag("final", true) && !Vars.state.rules.weather.isEmpty()){
                Vars.state.rules.weather.clear();
                Vars.state.rules.weather.add(new WeatherEntry(OlWeathers.hailStone,
                2.5f * toMinutes, 5f * toMinutes,
                30f * toSeconds, 1.5f * toMinutes
                ));
                Groups.weather.each(weather -> weather.life = 300f);
            }
            if(getFlag("haildemo", true)){
                Call.createWeather(OlWeathers.hailStone, 0.3f, 8f * 60f, 1f, 1f);
            }
        }){{
            alwaysUnlocked = true;
            difficulty = 1;
        }};
        redeploymentPath = new ExtraSectorPreset("redeployment_path", OlPlanets.glasmore, 607, () -> {
            if(getFlag("addweather", true)){
                Vars.state.rules.weather.clear();
                Vars.state.rules.weather.add(
                new WeatherEntry(OlWeathers.wind, toMinutes, 12f * toMinutes, 2f * toMinutes, 3f * toMinutes),
                new WeatherEntry(OlWeathers.aghaniteStorm, 1.5f * toMinutes, 5f * toMinutes, 5f * toMinutes, 8f * toMinutes)
                );
            }
        }){{
            captureWave = 15;
        }};
        deadValley = new ExtraSectorPreset("dead_valley", OlPlanets.glasmore, 660){{
            captureWave = 20;
            difficulty = 3;
        }};
    }
}
