package omaloon.content;

import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.graphics.*;
import mindustry.world.*;
import omaloon.world.blocks.environment.customsshapeproop.*;

import static arc.graphics.g2d.Draw.*;
import static arc.math.Angles.randLenVectors;

public class OlFx{
    public static final Effect
    glacied = new Effect(80f, e -> {
        color(OlStatusEffects.glacied.color);
        alpha(Mathf.clamp(e.fin() * 2f));

        Fill.circle(e.x, e.y, e.fout());
    }),

    breakShapedProp = new Effect(23, e -> {
        if(!(e.data instanceof MultiPropGroup group)) return;

        float scl = Math.max(e.rotation, 1);
        color(Tmp.c1.set(e.color).mul(1.1f));

        for(Tile tile : group.group){
            randLenVectors(e.id + tile.pos(), 2, 19f * e.finpow() * scl, (x, y) -> {
                float wx = tile.worldx() + x;
                float wy = tile.worldy() + y;
                Fill.circle(wx, wy, e.fout() * 3.5f * scl + 0.3f);
            });
        }
    }).layer(Layer.debris);
}
