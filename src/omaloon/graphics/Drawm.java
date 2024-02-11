package omaloon.graphics;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arclibrary.graphics.*;
import mindustry.graphics.*;

public class Drawm{

    public static void dashPoly(float partSize, Color color, float... cords){
        Lines.stroke(3f, Pal.gray);
        DashLine.dashPolyWithLength(partSize, cords);
        Lines.stroke(1f, color);
        DashLine.dashPolyWithLength(partSize, cords);
        Draw.reset();
    }

    public static void dashPoly(Color color, float... cords){
        dashPoly(10, color, cords);
    }

    public static void shadow(TextureRegion region, float x, float y, float rotation, float alpha){
        Draw.color(Pal.shadow, alpha);
        Draw.rect(region, x, y, rotation);
        Draw.color();
    }
}
