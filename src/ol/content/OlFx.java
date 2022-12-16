package ol.content;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Interp;
import arc.math.geom.Vec2;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import ol.graphics.OlGraphics;
import ol.graphics.OlPal;
import arc.graphics.g2d.Fill;
import arc.math.Mathf;
import arc.math.Rand;
import mindustry.entities.Effect;
import mindustry.graphics.Drawf;

import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.randLenVectors;

public class OlFx {
    private static final Rand rand = new Rand();
    public static final Vec2 vec = new Vec2();

    public static final Effect
            blueSphere = new Effect(65f, ef -> {
                color(OlPal.oLBlue);
                stroke(ef.fout() * 2f);

                Fill.circle (ef.x, ef.y, ef.fin() * 4f);
                Lines.arc   (ef.x, ef.y, ef.fin() * 8f, 3f);
            }).followParent(true).rotWithParent(true),

            blueShot = new Effect(55f, ef -> {
                color(OlPal.oLBlue);
                float w = 1f + 10 * ef.fout();

                Drawf.tri(ef.x, ef.y, w, 15f * ef.fout(), ef.rotation);
                Drawf.tri(ef.x, ef.y, w, 3f  * ef.fout(), ef.rotation + 180f);
            }).followParent(true).rotWithParent(true),

            olCentryfugeExplosion = new Effect(30, 500f, ef -> {
                float intensity = 8f;
                float baseLifetime = 25f + intensity * 15f;
                ef.lifetime = 50f + intensity * 64f;

                color(OlPal.oLBlue);
                alpha(0.8f);

                //if you need convert to atomic when best variant is just make a one element array
                for(int fi[] = { 0 }; fi[0] < 5; fi[0]++) {
                    rand.setSeed(ef.id * 2L + fi[0]);
                    float lenScl = rand.random(0.25f, 1f);

                    ef.scaled(ef.lifetime * lenScl, ef2 -> {
                        //to long line of code
                        randLenVectors(
                                //seed
                                ef2.id + fi[0] - 1,

                                //fin or progress from 0 to 1
                                ef2.fin(Interp.pow10Out),

                                //amount
                                (int) (2.8f * intensity),

                                //length
                                25f * intensity,

                                //consumer
                                (x, y, in, out) -> {
                                    float fout = ef2.fout(Interp.pow5Out) * rand.random(0.5f, 1f);
                                    float rad = fout * ((2f + intensity) * 2.35f);

                                    Fill.circle(ef2.x + x, ef2.y + y, rad);
                                    Drawf.light(ef2.x + x, ef2.y + y, rad * 2.6f, OlPal.oLDarkBlue, 0.7f);
                                }
                        );
                    });
                }

                ef.scaled(baseLifetime, ef2 -> {
                    Draw.color();

                    ef2.scaled(5 + intensity * 2f, ef3 -> {
                        stroke((3.1f + intensity / 5f) * ef3.fout());
                        Lines.circle(ef2.x, ef2.y, (3f + ef3.fin() * 14f) * intensity);

                        Drawf.light (
                                //x and y
                                ef2.x, ef2.y,

                                //radius
                                ef3.fin() * 14f * 2f * intensity,

                                //color
                                Color.white,

                                //alpha channel
                                0.9f * ef2.fout()
                        );
                    });

                    color(Color.white, Pal.lighterOrange, ef2.fin());
                    stroke((2f * ef2.fout()));

                    OlGraphics.l(Layer.effect + 0.001f);
                    randLenVectors(ef2.id + 1, ef2.finpow() + 0.001f, (int) (8 * intensity), 30f * intensity, (x, y, in, out) -> {
                        lineAngle   (ef2.x + x, ef2.y + y, Mathf.angle(x, y), 1f + out * 4 * (4f + intensity));
                        Drawf.light (ef2.x + x, ef2.y + y, (out * 4 * (3f + intensity)) * 3.5f, Draw.getColor(), 0.8f);
                    });
                });
            }),

            sticky = new Effect(80f, ef -> {
                color(OlPal.oLDalanite);
                alpha(Mathf.clamp(ef.fin() * 2f));

                Fill.circle(ef.x, ef.y, ef.fout());
            }).layer(Layer.debris),

            psh = new Effect(150f, ef -> {
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
            }),

            pressureDamage = new Effect(150f, ef -> {
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
