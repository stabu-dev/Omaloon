package ol.utils;

import arc.math.*;

public class OlGeomerty{
    public static float squareEdgeDistance(float radius,float angle){
        return rhombusEdgeDistance(radius,angle+45);
    }
    public static float rhombusEdgeDistance(float radius,float angle){
        var part1 = Math.abs(Mathf.cosDeg(angle));

        var part2 = Math.abs(Mathf.sinDeg(angle));


        return (radius / (part1 + part2));
    }
}
