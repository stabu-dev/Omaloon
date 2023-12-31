package omaloon.content;

import arc.graphics.g2d.Draw;
import arc.math.Interp;
import arc.math.Mathf;
import arc.util.Tmp;
import mindustry.entities.Effect;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import omaloon.entities.bullet.HailStoneBulletType;

public class OIFx {
    public static Effect

    falledStone = new Effect(120f, e -> {
        if(!(e.data instanceof HailStoneBulletType.HailStoneData data)) return;

        Tmp.v2.trns(Mathf.randomSeed(e.id) * 360, data.fallTime/2 + Mathf.randomSeed(e.id + 1) * data.fallTime);
        float scl = Interp.bounceIn.apply(e.fout() - 0.3f);
        float rot = Tmp.v2.angle();
        float x = e.x + (Tmp.v2.x * e.finpow()), y = e.y + (Tmp.v2.y * e.finpow());

        Draw.z(Layer.darkness);
        Draw.scl();
        Drawf.shadow(data.region, x, y, rot);

        Draw.scl();
        Draw.z(Layer.flyingUnitLow);
        Draw.rect(data.region, x, y + (scl * data.fallTime/2), rot);
    });

}
