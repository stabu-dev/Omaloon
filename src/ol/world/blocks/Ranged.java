package ol.world.blocks;

import arc.graphics.*;

import mindustry.gen.*;
import mindustry.graphics.*;

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