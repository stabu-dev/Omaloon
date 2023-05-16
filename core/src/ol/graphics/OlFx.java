package ol.graphics;

import arc.graphics.g2d.*;

import arc.math.Rand;
import arc.math.geom.Vec2;
import mindustry.entities.*;
import mindustry.graphics.*;
import ol.graphics.OlPal;

import static arc.graphics.g2d.Draw.*;
import static arc.graphics.g2d.Lines.*;
import static arc.math.Angles.*;

public class OlFx {
    private static final Rand rand = new Rand();
    public static final Vec2 vec = new Vec2();

    public static final Effect tauTrail = new Effect(30, e -> {
        rand.setSeed(e.id);
        float alpha = e.fin();
        float stroke = 0.5f + e.fout() * 1.5f;
        float radius1 = 12f + e.finpow();
        float radius2 = 14f + e.finpow();

        for(int i = 0; i < 2; i++){
            float rot = e.rotation + rand.range(10f) + 180f;
            vec.trns(rot, rand.random(e.fin() * 10f));
            Draw.color(Pal.heal, Pal.heal, alpha);
            lineAngle(e.x + vec.x, e.y + vec.y, rot, stroke * rand.random(2f, 5f) + 1.3f);
        }

        randLenVectors(e.id, 2, radius1, e.rotation + 180, 7f, (x, y) -> {
            Draw.color(Pal.heal, Pal.heal, alpha);
            Fill.circle(e.x + x, e.y + y, e.fout());
        });

        randLenVectors(e.id, 3, radius2, e.rotation + 180, 9f, (x, y) -> {
            Draw.color(Pal.heal, Pal.heal, alpha);
            Fill.circle(e.x + x, e.y + y, e.fout());
        });

        color();
    });

    public static final Effect omaloonBlueShot = new Effect(55f, ef -> {
        color(OlPal.omaloonBlue);
        float w = 1f + 10 * ef.fout();

        Drawf.tri(ef.x, ef.y, w, 15f * ef.fout(), ef.rotation);
        Drawf.tri(ef.x, ef.y, w, 3f  * ef.fout(), ef.rotation + 180f);
    }).followParent(true).rotWithParent(true);

    public static final Effect omaloonBlueCharge = new Effect(65f, ef -> {
        color(OlPal.omaloonBlue);
        stroke(ef.fout() * 2f);

        Fill.circle (ef.x, ef.y, ef.fin() * 4f);
        Lines.arc(ef.x, ef.y, ef.fin() * 8f, 3f);
    }).followParent(true).rotWithParent(true);
}
