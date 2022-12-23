package ol.content;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.math.geom.*;

import mindustry.graphics.*;
import mindustry.entities.*;

import ol.graphics.*;

import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;

public class OlFx {
    private static final Rand rand = new Rand();
    public static final Vec2 vec = new Vec2();

    public static final Effect
            blueSphere = new Effect(65f, ef -> {
                color(OlPal.oLBlue);
                stroke(ef.fout() * 2f);

                Fill.circle (ef.x, ef.y, ef.fin() * 4f);
                Lines.arc   (ef.x, ef.y, ef.fin() * 8f, 3f);
            }).followParent(true).rotWithParent(true);

    public static final Effect blueShot = new Effect(55f, ef -> {
                color(OlPal.oLBlue);
                float w = 1f + 10 * ef.fout();

                Drawf.tri(ef.x, ef.y, w, 15f * ef.fout(), ef.rotation);
                Drawf.tri(ef.x, ef.y, w, 3f  * ef.fout(), ef.rotation + 180f);
            }).followParent(true).rotWithParent(true);

    public static final Effect sticky = new Effect(80f, ef -> {
                color(OlPal.oLDalanite);
                alpha(Mathf.clamp(ef.fin() * 2f));

                Fill.circle(ef.x, ef.y, ef.fout());
            }).layer(Layer.debris);

    public static final Effect psh = new Effect(150f, ef -> {
                color(Color.white);
                alpha(0.6f);

                rand.setSeed(ef.id);
                for(int i = 0; i < 3; i++) {
                    float len = rand.random(6f), rot = rand.range(40f) + ef.rotation;

                    ef.scaled(ef.lifetime * rand.random(0.3f, 1f), ef2 -> {
                        vec.trns(rot, len * ef2.finpow());

                        Fill.circle(ef2.x + vec.x, ef2.y + vec.y, 1.5f * ef2.fslope() + 0.2f);
                    });
                }
            });

    public static final Effect pressureDamage = new Effect(150f, ef -> {
                color(Color.white);
                alpha(0.6f);

                rand.setSeed(ef.id);
                for(int i = 0; i < 3; i++) {
                    float len = rand.random(6f), rot = rand.range(40f) + ef.rotation;

                    ef.scaled(ef.lifetime * rand.random(0.3f, 1f), b -> {
                        vec.trns(rot, len * b.finpow());
                        Fill.circle(ef.x + vec.x, ef.y + vec.y, 1.2f * b.fslope() + 0.2f);
                    });
                }
            });
}
