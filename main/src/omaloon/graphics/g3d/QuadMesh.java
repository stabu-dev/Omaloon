package omaloon.graphics.g3d;

import arc.graphics.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;

public class QuadMesh extends PlanetMesh{
    Vec3 normal;
    float radius;

    public QuadMesh(Planet planet, Shader shader, Vec3 normal, float radius){
        this.planet = planet;
        this.shader = shader;

        this.radius = radius;
        this.normal = normal;
        this.mesh = buildMesh();
    }

    public Mesh buildMesh(){
        FloatSeq vertices = new FloatSeq();

        Vec3 base = normal.cpy().crs(normal.equals(Vec3.Y) ? Vec3.X : Vec3.Y).nor();

        for(int i = 0; i < 4; i++){
            base.rotate(normal, 90f);
            vertices.add(base.x * radius * Mathf.sqrt2, base.y * radius * Mathf.sqrt2, base.z * radius * Mathf.sqrt2);
            vertices.add(Mathf.num(i == 1 || i == 2), Mathf.num(i > 1f));
        }

//        for(Point2 offset : Geometry.d8edge) {
//            vertices.add(offset.x * radius, 0f, offset.y * radius);
//            vertices.add((offset.x + 1f) / 2f, (offset.y + 1f) / 2f);
//        }

        Mesh out = new Mesh(true, 4, 12, VertexAttribute.position3, VertexAttribute.texCoords);
        out.getVerticesBuffer().limit(vertices.size);
        out.getVerticesBuffer().position(0);
        out.getIndicesBuffer().limit(12);
        out.getIndicesBuffer().position(0);
        out.setVertices(vertices.toArray());
        out.setIndices(new short[] {0, 1, 2, 0, 2, 3,  0, 2, 1, 0, 3, 2});

        return out;
    }
}
