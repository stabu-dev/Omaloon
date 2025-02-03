package omaloon.world.draw;

import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;

public class Drawo {
    public static void asymmetricSpinSprite(TextureRegion region, float x, float y, float r){
        float a = Draw.getColor().a;
        r = Mathf.mod(r, 180f);
        Draw.rect(region, x, y, r);
        Draw.alpha(r / 180f * a);
        Draw.rect(region, x, y, r - 180f);
        Draw.alpha(a);
    }
}
