package omaloon.graphics.g3d;

import arc.graphics.*;
import arc.math.geom.*;

public class MeshUtils{
    static final Vec3
        v1 = new Vec3(),
        v2 = new Vec3(),
        v3 = new Vec3(),
        v4 = new Vec3();

    static final float[] floats = new float[8]; //3 + 3 + 2
    static Mesh mesh;

    static void begin(int count){
        mesh = new Mesh(true, count, 0,
            VertexAttribute.position3,
            VertexAttribute.normal,
            VertexAttribute.texCoords
        );

        mesh.getVerticesBuffer().limit(mesh.getMaxVertices());
        mesh.getVerticesBuffer().position(0);
    }

    static Mesh end(){
        Mesh last = mesh;

        last.getVerticesBuffer().limit(last
            .getVerticesBuffer()
            .position()
        );

        mesh = null;
        return last;
    }

    static Vec3 normal(Vec3 v1, Vec3 v2, Vec3 v3){
        return v4.set(v2).sub(v1).crs(
            v3.x - v1.x,
            v3.y - v1.y,
            v3.z - v1.z
        ).nor();
    }

    static void vert(Vec3 a, Vec3 normal, float texCordsX, float texCordsY){
        floats[0] = a.x;
        floats[1] = a.y;
        floats[2] = a.z;

        floats[3] = normal.x;
        floats[4] = normal.y;
        floats[5] = normal.z;

        floats[6] = texCordsX;
        floats[7] = texCordsY;

        mesh.getVerticesBuffer().put(floats);
    }
}