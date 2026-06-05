package omaloon.graphics.g3d;

import arc.graphics.*;
import arc.math.geom.*;
import arc.struct.*;

public class OlMeshBuilder{
    public static void vertex(FloatSeq list, Vec3 pos, Vec3 normal, Color color){
        list.add(pos.x, pos.y, pos.z);
        list.add(normal.x, normal.y, normal.z);
        list.add(color.toFloatBits());
    }
}
