package omaloon.content;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.util.Tmp;
import mindustry.entities.Effect;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import omaloon.entities.bullet.HailStoneBulletType;

import static arc.graphics.Color.valueOf;
import static arc.graphics.g2d.Draw.alpha;
import static arc.graphics.g2d.Draw.color;
import static arc.graphics.g2d.Lines.lineAngle;
import static arc.graphics.g2d.Lines.stroke;
import static arc.math.Angles.randLenVectors;

public class OIFx {
    public static final Rand rand = new Rand();
    public static Effect
    fallenStone = new Effect(120f, e -> {
        if(!(e.data instanceof HailStoneBulletType.HailStoneData data)) return;

        Tmp.v2.trns(Mathf.randomSeed(e.id) * 360, data.fallTime/2 + Mathf.randomSeed(e.id + 1) * data.fallTime);
        float scl = Interp.bounceIn.apply(e.fout() - 0.3f);
        float rot = Tmp.v2.angle();
        float x = e.x + (Tmp.v2.x * e.finpow()), y = e.y + (Tmp.v2.y * e.finpow());

        Draw.z(Layer.power + 0.1f);
        Draw.scl();
        Drawf.shadow(data.region, x, y, rot);

        Draw.scl();
        Draw.z(Layer.power + 0.2f);
        Draw.rect(data.region, x, y + (scl * data.fallTime/2), rot);
    });
}
