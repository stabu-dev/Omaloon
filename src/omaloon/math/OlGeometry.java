package omaloon.math;

import arc.math.*;
import arc.math.geom.*;

public class OlGeometry{
    public static final float oneFourth = 1 / 4f;
    private static final Vec2
        tmp1 = new Vec2(),
        tmp2 = new Vec2(),
        tmp3 = new Vec2();
    private static final Vec2 two = new Vec2(2, 2);
    private static final float[] tmpFloats6 = new float[6];
    private static final Circle circle1 = new Circle(), circle2 = new Circle();
//    public static boolean intersectionPointCanBeBetween = false;

    /**
     * @author Zelaux
     * Demo
     * <a href="https://www.desmos.com/calculator/qdx5zo4yem?lang=ru">Desmos</a>
     */
    public static boolean calculateIntersectionPointOfCircles(Vec2 a, Vec2 b, float radius, Vec2 out, Vec2 directionFromA){
        float radius2 = radius * radius;
        float dst2 = a.dst2(b);
        if(dst2 > 4 * radius2 || dst2 <= Float.MIN_NORMAL) return false;

        out.set(a).add(b).div(two);//midpoint
        if(dst2 == 4 * radius2){
            return true;
        }
        float dy = a.x - b.x;
        float dx = -(a.y - b.y);


        float offsetScale = Mathf.sqrt(radius2 / dst2 - oneFourth);

        float scale =
            Math.signum(dx * directionFromA.x + dy * directionFromA.y) * offsetScale;

        out.add(dx * scale, dy * scale);


        return true;
    }

    private static void setMatrix(Vec2 a, Vec2 b, float[] matrix, int i){
        float ax = a.x;
        float ay = a.y;
        float bx = b.x;
        float by = b.y;

        matrix[i] = ax - bx;
        matrix[i + 1] = ay - by;
//        matrix[i++] = ((b.y + a.y) * (b.y - a.y) - (a.x + b.x) * (a.x - b.x)) / 2;
//        matrix[i + 2] = ((b.y * b.y - a.y * a.y) - (a.x * a.x - b.x * b.x)) / 2;
        matrix[i + 2] = (by * by + bx * bx - ax * ax - ay * ay) / 2;
//        matrix[i + 2] = (b.len2() - a.len2()) / 2;
    }

    public static boolean intersectionPoint(Circle a, Circle b, Vec2 directionPoint, Vec2 out){
        return intersectionPoint(a.x, a.y, a.radius, b.x, b.y, b.radius, directionPoint, out);
    }

    public static boolean intersectionPoint(Vec2 p1, float r1, Vec2 p2, float r2, Vec2 directionPoint, Vec2 out){
        return intersectionPoint(p1.x, p1.y, r1, p2.x, p2.y, r2, directionPoint, out);
    }

    public static boolean intersectionPoint(Position p1, float r1, Position p2, float r2, Vec2 directionPoint, Vec2 out){
        return intersectionPoint(p1.getX(), p1.getY(), r1, p2.getX(), p2.getY(), r2, directionPoint, out);
    }

    /**
     * @author Zeluax
     * Demo
     * <a href="https://www.desmos.com/calculator/skgkb55w18?lang=ru">Desmos</a>
     */
    public static boolean intersectionPoint(float x1, float y1, float r1, float x2, float y2, float r2, Vec2 directionPoint, Vec2 out){
        float dx0 = x1 - x2;
        float dy0 = y1 - y2;
        float dst2 = Mathf.len2(dx0, dy0);
        if(dst2 == 0) return false;
        float sumRadius = r1 + r2;
        if(dst2 > sumRadius * sumRadius) return false;
        float dst = Mathf.sqrt(dst2);

        float r1_2 = r1 * r1;
        float r2_2 = r2 * r2;
        float cos = r1 * (dst2 + r1_2 - r2_2) / (2 * dst * r1);
        if(cos > r1 || cos < -r1) return false;

        float sin = Mathf.sqrt(r1_2 - cos * cos);

        float dx = -dx0 / dst;
        float dy = -dy0 / dst;

        float dx2 = dy * sin;
        float dy2 = -dx * sin;

        /*if(directionPoint.isZero()){
            float cx=x1+dx*cos;
            float cy=y1+dy*cos;

            directionPoint
                .set(cx,cy)
                .add(dx2,dy2);
            out
                .set(cx,cy)
                .sub(dx2,dy2);

        }else*/
        {
            float scale =/*
                intersectionPointCanBeBetween ?
                    Math.signum(directionPoint.dot(dx2, dy2)) :*/
                    Mathf.sign(directionPoint.dot(dx2, dy2));
            out.set(x1 + dx * cos + dx2 * scale, y1 + dy * cos + dy2 * scale);
        }
        return true;
    }

    /**
     * @author Zelaux
     */
    public static boolean calculateCircle(Vec2 a, Vec2 b, Vec2 c, Circle circle){
        float[] fls = tmpFloats6;
        setMatrix(a, b, fls, 0);
        setMatrix(b, c, fls, 3);
        //(0 1 | 2)
        //(3 4 | 5)
        float mainDet = calculateDet(fls, 0, 1);
        if(mainDet == 0) return false;

        float x = calculateDet(fls, 1, 2) / mainDet;
        float y = -calculateDet(fls, 0, 2) / mainDet;

        circle.x = x;
        circle.y = y;
        circle.radius = a.dst(x, y);


        return true;
    }

    private static float calculateDet(float[] matrix, int column1, int column2){
        return matrix[column1] * matrix[column2 + 3] - matrix[column2] * matrix[column1 + 3];
    }
}
