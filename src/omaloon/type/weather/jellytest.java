package omaloon.type.weather;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.type.weather.ParticleWeather;
import omaloon.math.Math3D;

import static mindustry.Vars.renderer;
import static mindustry.Vars.world;

public class jellytest extends ParticleWeather {
    public jellytest(String name){ super(name);}

    public static void drawParticles(TextureRegion region, Color color,
                                     float sizeMin, float sizeMax,
                                     float density, float intensity, float opacity,
                                     float windx, float windy,
                                     float minAlpha, float maxAlpha,
                                     float sinSclMin, float sinSclMax, float sinMagMin, float sinMagMax,
                                     boolean randomParticleRotation){
        rand.setSeed(0);
        Tmp.r1.setCentered(Core.camera.position.x, Core.camera.position.y, Core.graphics.getWidth() / renderer.minScale(), Core.graphics.getHeight() / renderer.minScale());
        Tmp.r1.grow(sizeMax * 1.5f);
        Core.camera.bounds(Tmp.r2);
        int total = (int)(Tmp.r1.area() / density * intensity);
        Draw.color(color, opacity);

        for(int i = 0; i < total; i++){
            float scl = rand.random(0.5f, 1f);
            float scl2 = rand.random(0.5f, 1f);
            float size = rand.random(sizeMin, sizeMax);
            float alpha = rand.random(minAlpha, maxAlpha);
            float rotation = randomParticleRotation ? rand.random(0f, 360f) : 0f;
            float tx = rand.random(0f, world.unitWidth());
            float ty = rand.random(0f, world.unitHeight());
            float scls = rand.random(sinSclMin, sinSclMax);
            float mag = rand.random(sinMagMin, sinMagMax);

            int amount = rand.random(4, 10);


            for (int j = 0; j < amount; j++) {

                float x = (tx + (Time.time - j * 10) * windx * scl2);
                float y = (ty + (Time.time - j * 10) * windy * scl);


                x += Mathf.sin(y, scls, mag);

                x -= Tmp.r1.x;
                y -= Tmp.r1.y;
                x = Mathf.mod(x, Tmp.r1.width);
                y = Mathf.mod(y, Tmp.r1.height);
                x += Tmp.r1.x;
                y += Tmp.r1.y;

                if(Tmp.r3.setCentered(x, y, size).overlaps(Tmp.r2)){
                    Draw.alpha(alpha * opacity);
                    Draw.rect(region, x, y, size, size, rotation);
                }
            }
        }

        Draw.reset();
    }
}
