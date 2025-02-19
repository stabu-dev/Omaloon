package omaloon.math;

import arc.math.geom.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class CircleStuffTest{
    static final Vec2 tmp1 = new Vec2(), tmp2 = new Vec2();

    static final float FLT_DELTA=0.0001f;
    @Test
    void intersection(){
        assertTrue(OlGeometry.intersectionPoint(
            1, 1, 1.5f,
            0, 0, 2,
            tmp1.set(2, 1).sub(1, 1),
            tmp2
        ), "Cannot compute intersection");
        Assertions.assertEquals(1.99632,tmp2.x,FLT_DELTA);
        Assertions.assertEquals(-0.12132,tmp2.y,FLT_DELTA);
    }
}
