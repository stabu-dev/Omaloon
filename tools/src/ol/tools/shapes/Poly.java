package ol.tools.shapes;

import arc.math.geom.Polygon;
import arc.math.geom.Shape2D;
import arc.math.geom.Vec2;
import arc.struct.FloatSeq;
import arc.struct.Seq;

import java.awt.*;
import java.awt.geom.*;

public abstract class Poly<T extends Poly> implements Cloneable, Shape2D, Shape {
    static final FloatSeq floats = new FloatSeq();
    private static Vec2 tmp = new Vec2();
    private final Vec2[] points;
    public Poly(int size) {
        points = new Vec2[size];
    }

    @Override
    public Rectangle getBounds() {
        return getBounds2D().getBounds();
    }

    @Override
    public Rectangle2D getBounds2D() {
        Seq<Vec2> points=Seq.with(this.points);
        Seq<Float> xmap = points.map(v ->  v.x);
        Seq<Float> ymap = points.map(v ->  v.y);
        float x=  xmap.min(Float::floatValue);
        float y=  ymap.min(Float::floatValue);
        float maxX=  xmap.max(Float::floatValue);
        float maxY=  ymap.max(Float::floatValue);
        return new Rectangle2D.Float(x, y, maxX - x, maxY - y);
    }

    @Override
    public boolean contains(double x, double y) {
        return contains((float) x, (float) y);
    }

    @Override
    public boolean contains(Point2D p) {
        return contains(p.getX(), p.getY());
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
        return getBounds().intersects(x, y, w, h);
    }

    private Polygon toPolygon() {
        floats.clear();
        for (Vec2 vec2 : points) {
            floats.add(vec2.x, vec2.y);
        }
        Polygon polygon = new Polygon(floats.toArray());
        floats.clear();
        return polygon;
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        return intersects(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {

        return contains(x, y) && contains(x + w, y) && contains(x + w, y + h) && contains(x, y + h);
    }

    @Override
    public boolean contains(Rectangle2D r) {
        return contains(r.getX(), r.getY(), r.getWidth(), r.getHeight());
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at) {
        return new PolyPathIterator(points, at);
    }

    @Override
    public PathIterator getPathIterator(AffineTransform at, double flatness) {
        return new FlatteningPathIterator(getPathIterator(at), flatness);
    }
    public Shape toShape(){
        Seq<Integer> xmap = Seq.with(points).map(v -> (int) v.x);
        Seq<Integer> ymap = Seq.with(points).map(v -> (int) v.y);
        int[] x = new int[xmap.size];
        int[] y = new int[ymap.size];
        for (int i = 0; i < x.length; i++) {
            x[i]= xmap.get(i);
        }
        for (int i = 0; i < y.length; i++) {
            y[i]= ymap.get(i);
        }
        java.awt.Polygon polygon = new java.awt.Polygon(x,y,points.length);

        return polygon;
    }

    @Override
    public boolean contains(float x, float y) {
        return contains(tmp.set(x, y));
    }

    public T set(int index, Vec2 npoint) {
        points[index] = npoint;
        return getThis();
    }

    public T scl(Vec2 vec2) {
        for (Vec2 point : points) {
            point.scl(vec2);
        }
        return getThis();
    }

    public T scl(float scl) {
        return scl(scl, scl);
    }

    public T scl(float sclX, float sclY) {
        return scl(tmp.set(sclX, sclY));
    }

    public Vec2 get(int index) {
        return points[index];
    }

    protected abstract T getThis();

    @Override
    public T clone() {
        try {
            return (T) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean corner(Vec2 other) {
        if (other == null) return false;
        for (Vec2 point : points) {
            if (other.equals(point)) return true;
        }
        return false;
    }

    public boolean contains(Vec2 point) {
        Polygon polygon = toPolygon();
        return polygon.contains(point);
    }


    public Vec2[] getAll() {
        return points.clone();
    }

    public T setAll(Vec2... points) {
        for (int i = 0; i < points.length; i++) {
            set(i, points[i]);
        }
        return getThis();
    }

    public T translate(float dx,float dy) {
        for (Vec2 point : points) {
            point.add(dx,dy);
        }
        return getThis();
    }

    public T cpy() {
        Poly clone =(Poly) clone();
        for (int i = 0; i < points.length; i++) {
            clone.points[i]=points[i].cpy();
        }
        return (T) clone;
    }
}
