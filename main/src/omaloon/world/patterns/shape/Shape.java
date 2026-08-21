package omaloon.world.patterns.shape;

import arc.func.*;

/**
 * Represents a geometric tile shape defined by a set of relative coordinates and an anchor point.
 * @author stabu_
 */
public abstract class Shape{
    /** Position of the anchor tile relative to the shape's bounding-box origin (0,0). */
    public int anchorX = 0, anchorY = 0;

    public abstract int width();
    public abstract int height();
    public abstract boolean get(int x, int y);
    public abstract void load();

    /** Iterates over each point in the shape, providing relative coordinates from the origin. */
    public void each(Intc2 consumer){
        int w = width(), h = height();
        for(int x = 0; x < w; x++){
            for(int y = 0; y < h; y++){
                if(get(x, y)){
                    consumer.get(x, y);
                }
            }
        }
    }
}
