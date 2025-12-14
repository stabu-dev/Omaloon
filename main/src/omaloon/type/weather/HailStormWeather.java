package omaloon.type.weather;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.struct.*;
import mindustry.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;
import omaloon.content.*;
import omaloon.type.*;

public class HailStormWeather extends SpawnWeather{
    public ObjectFloatMap<BulletType> bullets = new ObjectFloatMap<>();

    public float spawnChance = 0;

    public boolean windDrag = true;
    public float windDragScaleMin = 1, windDragScaleMax = 1;

    public boolean rain = true;
    public float yspeed = 5f, xspeed = 1.5f, density = 900f, stroke = 0.75f, sizeMin = 8f, sizeMax = 40f, splashTimeScale = 22f;
    public Liquid liquid = OlLiquids.glacium;
    public TextureRegion[] splashes = new TextureRegion[12];
    public Color color = Color.valueOf("5e929d");

    private float minIntensity;

    private static BulletType picked;
    private static float threshold;

    public HailStormWeather(String name) {
        super(name);
    }

    /**
     * Each time a bullet spawns, it'll have a random variable that ranges from 0-1.
     * The bullet with the highest intensity that is still under the intensity will be spawned.
     */
    public void addBullet(BulletType bullet, float intensity) {
        bullets.put(bullet, intensity);
        minIntensity = Math.max(minIntensity, intensity);
    }

    @Override
    public void drawOver(WeatherState state){
        super.drawOver(state);
        if(rain) drawRain(sizeMin, sizeMax, xspeed, yspeed, density, state.intensity, stroke, color);
    }

    @Override
    public void drawUnder(WeatherState state){
        if(rain) drawSplashes(splashes, sizeMax, density, state.intensity, state.opacity, splashTimeScale, stroke, color, liquid);
    }

    @Override
    public void load(){
        super.load();

        for(int i = 0; i < splashes.length; i++){
            splashes[i] = Core.atlas.find("splash-" + i);
        }
    }

    @Override
    public boolean shouldSpawn(WeatherState state){
        return Mathf.chance(spawnChance * state.intensity);
    }

    @Override
    public void spawn(WeatherState state, float x, float y){
        if (Vars.net.client()) return;
        float intensity = Math.max(minIntensity, rand.random(1f));

        threshold = Float.NEGATIVE_INFINITY;
        picked = null;
        bullets.each(b -> {
            if (b.value <= intensity && b.value >= threshold) {
                picked = b.key;
                threshold = b.value;
            }
        });
        if (picked != null) picked.createNet(Team.derelict, x, y, windDrag ? state.windVector.angle() : 0, picked.damage, Mathf.random(windDragScaleMin, windDragScaleMax), 1);
    }
}
