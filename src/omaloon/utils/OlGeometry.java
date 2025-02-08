package omaloon.utils;

import arc.math.*;
import arc.math.geom.*;

public class OlGeometry{
    private static final Vec2
        tmp1 = new Vec2(),
        tmp2 = new Vec2(),
        tmp3 = new Vec2();
    private static final Vec2 two = new Vec2(2, 2);
    public static final float oneFourth = 1 / 4f;

    /**
     * @author Zelaux
     * Demo
     * <a href="https://www.desmos.com/calculator/qdx5zo4yem?lang=ru">Desmos</a>
     * */
    public static boolean calculateIntersectionPointOfCircles(Vec2 a, Vec2 b, float radius, Vec2 out, Vec2 directionFromA){
        float radius2 = radius * radius;
        float dst2 = a.dst2(b);
        if(dst2 > 4 * radius2 || dst2<=Float.MIN_NORMAL) return false;

        out.set(a).add(b).div(two);//midpoint
        if(dst2 == 4 * radius2){
            return true;
        }
        float dy = a.x - b.x;
        float dx = -(a.y - b.y);



        float offsetScale = Mathf.sqrt(radius2/dst2 - oneFourth);

        float scale =
            Math.signum(dx * directionFromA.x + dy * directionFromA.y) * offsetScale;

        out.add(dx * scale, dy * scale);


        return true;
    }
}
