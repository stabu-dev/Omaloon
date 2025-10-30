package omaloon.world.patterns;

import mindustry.world.*;
import omaloon.type.shape.*;

public interface Patterned{
    Shape getShape();

    void drawPattern(Tile anchor);
}
