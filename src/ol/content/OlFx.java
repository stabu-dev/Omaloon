package ol.content;

import arc.files.Fi;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Interp;
import arc.util.Tmp;
import mindustry.content.Liquids;
import mindustry.entities.effect.ParticleEffect;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.graphics.Trail;
import ol.graphics.OlPal;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.math.Rand;
import mindustry.entities.Effect;
import mindustry.graphics.Drawf;

import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.randLenVectors;
import static arc.util.Tmp.v1;
import static arc.util.Tmp.v2;
import static mindustry.Vars.state;

public class OlFx {
    private static final Rand rand = new Rand();

    public static final Effect
            blueSphere = new Effect(65f, e -> {
        color(OlPal.OLBlue);
        stroke(e.fout() * 2f);
        Fill.circle(e.x, e.y, e.fin() * 4f);
        Lines.arc(e.x, e.y, e.fin() * 8f, 3f);
    }).followParent(true).rotWithParent(true),

            blueShot = new Effect(55f, e -> {
                color(OlPal.OLBlue);
                float w = 1f + 10 * e.fout();
                Drawf.tri(e.x, e.y, w, 15f * e.fout(), e.rotation);
                Drawf.tri(e.x, e.y, w, 3f * e.fout(), e.rotation + 180f);
            }).followParent(true).rotWithParent(true),

    olCentryfugeExplosion = new Effect(30, 500f, b -> {
        float intensity = 8f;
        float baseLifetime = 25f + intensity * 15f;
        b.lifetime = 50f + intensity * 64f;

        color(OlPal.OLBlue);
        alpha(0.8f);
        for(int i = 0; i < 5; i++){
            rand.setSeed(b.id* 2L + i);
            float lenScl = rand.random(0.25f, 1f);
            int fi = i;
            b.scaled(b.lifetime * lenScl, e -> {
                randLenVectors(e.id + fi - 1, e.fin(Interp.pow10Out), (int)(2.8f * intensity), 25f * intensity, (x, y, in, out) -> {
                    float fout = e.fout(Interp.pow5Out) * rand.random(0.5f, 1f);
                    float rad = fout * ((2f + intensity) * 2.35f);

                    Fill.circle(e.x + x, e.y + y, rad);
                    Drawf.light(e.x + x, e.y + y, rad * 2.6f, OlPal.OLDarkBlue, 0.7f);
                });
            });
        }

        b.scaled(baseLifetime, e -> {
            Draw.color();
            e.scaled(5 + intensity * 2f, i -> {
                stroke((3.1f + intensity/5f) * i.fout());
                Lines.circle(e.x, e.y, (3f + i.fin() * 14f) * intensity);
                Drawf.light(e.x, e.y, i.fin() * 14f * 2f * intensity, Color.white, 0.9f * e.fout());
            });

            color(Color.white, Pal.lighterOrange, e.fin());
            stroke((2f * e.fout()));

            Draw.z(Layer.effect + 0.001f);
            randLenVectors(e.id + 1, e.finpow() + 0.001f, (int)(8 * intensity), 30f * intensity, (x, y, in, out) -> {
                lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), 1f + out * 4 * (4f + intensity));
                Drawf.light(e.x + x, e.y + y, (out * 4 * (3f + intensity)) * 3.5f, Draw.getColor(), 0.8f);
            });
        });
    }),
            sticky = new Effect(80f, e -> {
                color(OlPal.OLDalanite);
                alpha(Mathf.clamp(e.fin() * 2f));

                Fill.circle(e.x, e.y, e.fout());
            }).layer(Layer.debris),

    //TODO change
    pressureDamage = new ParticleEffect() {{
        colorFrom = OlPal.OLPressureMin;
        colorTo = OlPal.OLPressure;

        particles = 3;
        sizeFrom = 4;
        sizeTo = 6;

        lifetime = 15;
        lenFrom = 0;
        lenTo = 8;
    }};
}
