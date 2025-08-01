package omaloon.world.patterns;

import mindustry.world.Block;
import mindustry.world.Tile;
import omaloon.type.shape.Shape;

public interface Patterned{
    Shape getShape();
    void drawPattern(Tile anchor);
    Block getParent();
    boolean drawOnTop();
}
