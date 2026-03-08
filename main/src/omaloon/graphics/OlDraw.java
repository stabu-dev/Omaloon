package omaloon.graphics;

import arc.graphics.g2d.*;
import arc.math.*;

public class OlDraw {

    /**
     * Draws an elliptical donut sector using native Fill.tri.
     * @param x Center X
     * @param y Center Y
     * @param radiusInnerX Inner radius on X axis
     * @param radiusInnerY Inner radius on Y axis
     * @param radiusOuterX Outer radius on X axis
     * @param radiusOuterY Outer radius on Y axis
     * @param percent Coverage percentage (0-1)
     * @param angleOffset Starting angle offset in degrees
     * @param rotation Overall rotation of the ellipse in degrees
     */
    public static void donutEllipse(float x, float y, float radiusInnerX, float radiusInnerY, float radiusOuterX, float radiusOuterY, float percent, float angleOffset, float rotation) {
        int segments = (int)(40 * percent);
        if (segments < 3) segments = 3;

        float step = 360f * percent / segments;

        for (int i = 0; i < segments; i++) {
            float angle1 = i * step + angleOffset;
            float angle2 = (i + 1) * step + angleOffset;

            float cos1 = Mathf.cosDeg(angle1);
            float sin1 = Mathf.sinDeg(angle1);
            float cos2 = Mathf.cosDeg(angle2);
            float sin2 = Mathf.sinDeg(angle2);

            // Points before rotation
            float x1i = cos1 * radiusInnerX;
            float y1i = sin1 * radiusInnerY;
            float x1o = cos1 * radiusOuterX;
            float y1o = sin1 * radiusOuterY;

            float x2i = cos2 * radiusInnerX;
            float y2i = sin2 * radiusInnerY;
            float x2o = cos2 * radiusOuterX;
            float y2o = sin2 * radiusOuterY;

            // Apply rotation and center
            float cosR = Mathf.cosDeg(rotation);
            float sinR = Mathf.sinDeg(rotation);

            // Rotate and translate function
            // x' = x*cos - y*sin + cx
            // y' = x*sin + y*cos + cy

            float px1i = x1i * cosR - y1i * sinR + x;
            float py1i = x1i * sinR + y1i * cosR + y;
            float px1o = x1o * cosR - y1o * sinR + x;
            float py1o = x1o * sinR + y1o * cosR + y;

            float px2i = x2i * cosR - y2i * sinR + x;
            float py2i = x2i * sinR + y2i * cosR + y;
            float px2o = x2o * cosR - y2o * sinR + x;
            float py2o = x2o * sinR + y2o * cosR + y;

            // Draw two triangles to form a trapezoid segment
            Fill.tri(px1i, py1i, px1o, py1o, px2o, py2o);
            Fill.tri(px1i, py1i, px2o, py2o, px2i, py2i);
        }
    }
}
