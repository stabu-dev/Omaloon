package omaloon.utils;

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
        if(mainDet==0)return false;

        float x = calculateDet(fls, 1, 2)/mainDet;
        float y = -calculateDet(fls, 0, 2)/mainDet;

        circle.x=x;
        circle.y=y;
        circle.radius=a.dst(x,y);


        return true;
    }

    private static float calculateDet(float[] matrix, int column1, int column2){
        return matrix[column1] * matrix[column2 + 3] - matrix[column2] * matrix[column1 + 3];
    }
}
