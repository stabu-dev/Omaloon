package omaloon.type.weather;

import arc.*;
import arc.math.*;
import arc.util.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.type.weather.*;

import static mindustry.Vars.*;

public class SpawnerWeather extends ParticleWeather{
    public int maxSpawn = 6;
    public boolean inBounceCam = true;

    public SpawnerWeather(String name){
        super(name);
    }

    @Override
    public void update(WeatherState state){
        super.update(state);

        //I don't know how to make it more simplified --random
        if(inBounceCam){
            spawnByBounceCam(state);
        }else{
            spawn(state);
        }
    }

    //Spawn only visible area in area camera
    public void spawnByBounceCam(WeatherState state){
        //on based draw weather draw particle
        Tmp.r1.setCentered(Core.camera.position.x, Core.camera.position.y, Core.graphics.getWidth() / renderer.minScale(), Core.graphics.getHeight() / renderer.minScale());
        Tmp.r1.grow(sizeMax * 1.5f);
        Core.camera.bounds(Tmp.r2);
        rand.setSeed((long)Time.time);

        float speed = force * state.intensity * Time.delta;
        float windx = state.windVector.x * speed, windy = state.windVector.y * speed;
        int total = (int)Mathf.clamp(Tmp.r1.area() / density * state.intensity, 0f, maxSpawn);

        for(int i = 0; i < total; i++){
            float scl = rand.random(0.5f, 1f);
            float scl2 = rand.random(0.5f, 1f);
            float size = rand.random(sizeMin, sizeMax);
            float x = (rand.random(0f, world.unitWidth()) + Time.time * windx * scl2);
            float y = (rand.random(0f, world.unitHeight()) + Time.time * windy * scl);

            x += Mathf.sin(y, rand.random(sinSclMin, sinSclMax), rand.random(sinMagMin, sinMagMax));

            x -= Tmp.r1.x;
            y -= Tmp.r1.y;
            x = Mathf.mod(x, Tmp.r1.width);
            y = Mathf.mod(y, Tmp.r1.height);
            x += Tmp.r1.x;
            y += Tmp.r1.y;

            if(Tmp.r3.setCentered(x, y, size).overlaps(Tmp.r2) && canSpawn(state)){
                float x1 = Mathf.random(1, world.tiles.width - 1) * Vars.tilesize;
                float y1 = Mathf.random(1, world.tiles.height - 1) * Vars.tilesize;

                spawnAt(state, x1, y1);
            }
        }
    }

    //Spawn on the world
    public void spawn(WeatherState state){
        Tmp.r2.set(0f, 0f, world.unitWidth(), world.unitHeight());
        rand.setSeed((long)Time.time);

        int total = (int)Mathf.clamp(Tmp.r2.area() / density * state.intensity, 0f, maxSpawn);

        for(int i = 0; i < total; i++){
            if(canSpawn(state)){
                float x1 = Mathf.random(1, world.tiles.width - 1) * Vars.tilesize;
                float y1 = Mathf.random(1, world.tiles.height - 1) * Vars.tilesize;

                spawnAt(state, x1, y1);
            }
        }
    }


    public void spawnAt(WeatherState state, float x, float y){
        //TODO
    }

    public boolean canSpawn(WeatherState state){
        return true; //TODO
    }

}
