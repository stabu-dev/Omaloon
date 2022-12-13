package ol.world.blocks;

import arc.graphics.Color;
import mindustry.gen.Teamc;
import mindustry.graphics.Drawf;
import mindustry.graphics.Pal;

//NOT PRESSURE PART
public interface Ranged extends Teamc {
    float range();

    default Color color() {
        return Pal.accent;
    }

    default void drawRange(float x, float y) {
        Drawf.dashCircle(x, y, range(), color());
    }

    default void drawRange() {
        drawRange(getX(), getY());
    }
}