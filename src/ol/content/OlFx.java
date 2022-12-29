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
import static arc.math.Angles.*;

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
    public static final Effect zoneTrail = new Effect(30, e -> {
        color(Pal.heal, Pal.heal, e.fin() * e.fin());
        stroke(0.5f + e.fout() * 1.5f);
        rand.setSeed(e.id);

        for(int i = 0; i < 2; i++){
            float rot = e.rotation + rand.range(10f) + 180f;
            vec.trns(rot, rand.random(e.fin() * 10f));
            lineAngle(e.x + vec.x, e.y + vec.y, rot, e.fout() * rand.random(2f, 5f) + 1.3f);
        }

        randLenVectors(e.id, 2, 12f + e.finpow(), e.rotation + 180, 7f, (x, y) -> {
            Fill.circle(e.x + x, e.y + y, e.fout());
        });

        randLenVectors(e.id, 3, 14f + e.finpow(), e.rotation + 180, 9f, (x, y) -> {
            Fill.square(e.x + x, e.y + y, e.fout());
        });
    });
}
