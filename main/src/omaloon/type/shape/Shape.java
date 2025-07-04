package omaloon.type.shape;

import arc.func.*;

/**
 * Represents a shape for various content types.
 * Shapes are defined by a set of relative coordinates from an origin (0,0).
 * @author Gemini
 */
public abstract class Shape{
    /** The width of the shape's bounding box. */
    public abstract int width();

    /** The height of the shape's bounding box. */
    public abstract int height();

    /** Returns true if the tile at (x, y) relative to the shape's origin is part of the shape. */
    public abstract boolean get(int x, int y);

    /** Iterates over each point in the shape, providing relative coordinates from the origin. */
    public abstract void each(Intc2 consumer);
}
