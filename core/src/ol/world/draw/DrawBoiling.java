package ol.world.draw;

import arc.graphics.*;
import arc.graphics.g2d.*;

import arc.math.*;
import arc.util.*;

import mindustry.gen.*;
import mindustry.world.draw.*;

public class DrawBoiling extends DrawBlock {
    /**sets color for bubbles*/
    public Color bubblesColor;
    /**sets alpha for bubbles*/
    public float alpha = 0.9f;
    /**sets bubbles size*/
    public float bubblesSize = 0f;
    /**sets bubbles amount*/
    public int bubblesAmount = 0;

    @Override
    public void draw(Building build) {
        if(build.warmup() <= 0.001f) {
            return;
        }

        Draw.color(bubblesColor);
        Draw.alpha((Mathf.absin(build.totalProgress(), 10f, alpha) * 0.5f + 1f - 0.5f) * build.warmup() * alpha);
        Draw.color(bubblesColor.cpy().mul(1.2f).a(build.warmup() / 2.5f));
        bubbles(3040, build.x, build.y, bubblesAmount, bubblesSize, 330, 0.9f);
        Draw.reset();
    }

    public static void bubbles(int seed, float x, float y, int bubblesAmount, float bubblesSize, float baseLife, float baseSize) {
        rand.setSeed(seed);
        float quarterBaseSize = baseSize / 4f;
        for (int i = 0; i < bubblesAmount; i++) {
            float angle = rand.random(360f),
                    fin = (rand.random(0.8f) * (Time.time / baseLife)) % rand.random(0.1f, 0.6f),
                    len = rand.random(quarterBaseSize, baseSize) / fin,
                    trnsx = x + Angles.trnsx(angle, len, rand.random(quarterBaseSize, quarterBaseSize)),
                    trnsy = y + Angles.trnsy(angle, len, rand.random(quarterBaseSize, quarterBaseSize));
            Fill.poly(trnsx, trnsy, 18, Interp.sine.apply(fin * 3.5f) * bubblesSize);
        }
    }
}
