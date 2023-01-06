package ol.tools.shapes;

import arc.math.geom.Vec2;
import arc.struct.Seq;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class Tri extends Poly<Tri> {
    public Tri() {
        super(3);
    }
    public Tri(Vec2... points) {
        this();
        if (points.length!=3){
            throw new IllegalArgumentException("points must be tree for triangle");
        }
        setAll(points);
    }

    @Override
    public Shape toShape() {
        Vec2[] points = getAll();
        Seq<Vec2> pointsSeq = Seq.with(points);
        pointsSeq.add(pointsSeq.peek());
        Seq<Integer> xmap = pointsSeq.map(v -> (int) v.x);
        Seq<Integer> ymap = pointsSeq.map(v -> (int) v.y);
        int[] x = new int[xmap.size];
        int[] y = new int[ymap.size];
        for (int i = 0; i < 4; i++) {
            x[i]= xmap.get(i);
        }
        for (int i = 0; i < 4; i++) {
            y[i]= ymap.get(i);
        }
        Polygon polygon = new Polygon(x,y,4);
        return polygon;
    }

    @Override
    public Rectangle2D getBounds2D() {
        return super.getBounds2D();
    }

    @Override
    protected Tri getThis() {
        return this;
    }

}
