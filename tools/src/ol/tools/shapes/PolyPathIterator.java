package ol.tools.shapes;

import arc.math.geom.Vec2;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.util.NoSuchElementException;

public class PolyPathIterator implements PathIterator {
    final Vec2[] points;
    final AffineTransform affine;
    int index = 0;

    public PolyPathIterator(Vec2[] points, AffineTransform affine) {
        this.points = points;
        this.affine = affine;
    }

    @Override
    public int getWindingRule() {
        return WIND_NON_ZERO;
    }

    @Override
    public boolean isDone() {
        return index > points.length;
    }

    @Override
    public void next() {
        index++;
    }

    @Override
    public int currentSegment(float[] coords) {
        if (isDone()) {
            throw new NoSuchElementException("poly iterator out of bounds");
        }
        if (index == points.length) {
            return SEG_CLOSE;
        }
        coords[0] = points[index].x;
        coords[1] = points[index].y;
        if (affine != null) {
            affine.transform(coords, 0, coords, 0, 1);
        }
        return (index == 0 ? SEG_MOVETO : SEG_LINETO);
    }

    @Override
    public int currentSegment(double[] coords) {
        if (isDone()) {
            throw new NoSuchElementException("poly iterator out of bounds");
        }
        if (index == points.length) {
            return SEG_CLOSE;
        }
        coords[0] = points[index].x;
        coords[1] = points[index].y;
        if (affine != null) {
            affine.transform(coords, 0, coords, 0, 1);
        }
        return (index == 0 ? SEG_MOVETO : SEG_LINETO);
    }
}
