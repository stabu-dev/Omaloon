package omaloon.type.weather;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import mindustry.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;
import omaloon.content.*;

import java.util.*;

public class HailStormWeather extends SpawnerWeather{
    public float yspeed = 5f, xspeed = 1.5f, density = 900f, stroke = 0.75f, sizeMin = 8f, sizeMax = 40f, splashTimeScale = 22f;
    public Liquid liquid = OlLiquids.glacium;
    public TextureRegion[] splashes = new TextureRegion[12];
    public Color color = Color.valueOf("5e929d");

    public boolean drawRain = true;

    public BulletStack[] bullets;
    public float bulletChange = 0.2f;
    public Team bulletTeam = Team.derelict;

    public HailStormWeather(String name){
        super(name);
    }

    @Override
    public void load(){
        super.load();

        for(int i = 0; i < splashes.length; i++){
            splashes[i] = Core.atlas.find("splash-" + i);
        }
    }

    @Override
    public void spawnAt(WeatherState state, float x, float y){
        BulletType b = getBullet();

        if(!Vars.net.client()){
            b.createNet(bulletTeam, x, y, useWindVector ? state.windVector.angle() : 0, b.damage, 1f, 1f);
        }
    }

    @Override
    public boolean canSpawn(WeatherState state){
        return Mathf.randomBoolean(bulletChange * state.intensity);
    }

    public BulletType getBullet(){
        for(int i = 0; i < bullets.length; i++){
            var item = (BulletStack)bullets[i];

            if(Mathf.random() < item.change){
                return item.bullet;
            }
        }

        return bullets[bullets.length - 1].bullet;
    }

    public void setBullets(Object... items){
        var stack = new BulletStack[items.length / 2];

        for(int i = 0; i < items.length; i += 2){
            stack[i / 2] = new BulletStack((BulletType)items[i], (float)items[i + 1]);
        }

        Arrays.sort(stack, new Comparator<BulletStack>(){
            @Override
            public int compare(BulletStack o1, BulletStack o2){
                if(o1.change == o2.change) return 0;
                return o1.change > o2.change ? 1 : -1;
            }
        });

        bullets = stack;
    }

    @Override
    public void drawOver(WeatherState state){
        super.drawOver(state);
        if(drawRain)
            drawRain(sizeMin, sizeMax, xspeed, yspeed, density, state.intensity, stroke, color);
    }

    @Override
    public void drawUnder(WeatherState state){
        if(drawRain)
            drawSplashes(splashes, sizeMax, density, state.intensity, state.opacity, splashTimeScale, stroke, color, liquid);
    }

    public static class BulletStack{
        public BulletType bullet;
        public float change;

        public BulletStack(BulletType bullet, float change){
            this.bullet = bullet;
            this.change = change;
        }
    }
}
