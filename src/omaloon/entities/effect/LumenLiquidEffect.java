package omaloon.entities.effect;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;
import mindustry.entities.*;

public class LumenLiquidEffect extends Effect{
    public static final Rand rand = new Rand();
    public static final Vec2 rv = new Vec2();

    public Color base, middle, top;

    public LumenLiquidEffect(float lifetime, Color base, Color middle, Color top){
        this.lifetime = lifetime;
        this.base = base;
        this.middle = middle;
        this.top = top;
    }

    @Override
    public void render(EffectContainer e){
        Draw.color(base);
        rand.setSeed(e.id);
        for(int i = 0; i < 4f; i++){
            rv.trns(rand.range(10) + e.rotation, e.fin() * (12 + rand.random(32))).add(e.x, e.y);
            Fill.circle(rv.x, rv.y, 4 * e.foutpow());
        }
        Draw.color(middle);
        for(int i = 0; i < 11f; i++){
            rv.trns(rand.range(15) + e.rotation, e.fin() * (10 + rand.random(29))).add(e.x, e.y);
            Fill.circle(rv.x, rv.y, 3 * e.foutpow());
            rv.trns(rand.range(20) + e.rotation, e.finpow() * (9 + rand.random(26))).add(e.x, e.y);
            Fill.circle(rv.x, rv.y, 2 * e.fout());
        }
        Draw.color(top);
        for(int i = 0; i < 30f; i++){
            rv.trns(rand.range(30) + e.rotation, e.finpow() * (8 + rand.random(24))).add(e.x, e.y);
            Fill.circle(rv.x, rv.y, 1 * e.fout());
        }
    }
}
