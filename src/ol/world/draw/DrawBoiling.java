package ol.world.draw;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;

import mindustry.gen.*;
import mindustry.world.draw.*;

import ol.graphics.*;

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
        OlGraphics.bubbles(3040, build.x, build.y, bubblesAmount, bubblesSize, 330, 0.9f);
        Draw.reset();
    }
}
