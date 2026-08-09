package omaloon.type.shape;

import arc.func.*;

/**
 * Represents a shape for various tile types.
 * Shapes are defined by a set of relative coordinates.
 * @author stabu_
 */
public abstract class Shape{
    /** The position of the anchor tile, relative to the shape's bounding-box origin (0,0). */
    public int anchorX = 0, anchorY = 0;

    /** The width of the shape's bounding box. */
    public abstract int width();

    /** The height of the shape's bounding box. */
    public abstract int height();

    /** Returns true if the tile at (x, y) relative to the shape's origin is part of the shape. */
    public abstract boolean get(int x, int y);

    /** Iterates over each point in the shape, providing relative coordinates from the origin. */
    public abstract void each(Intc2 consumer);

    /** Loads any resources required for this shape. */
    public abstract void load();
}
