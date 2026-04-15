package omaloon.graphics.g3d;

import arc.graphics.*;
import arc.graphics.gl.*;
import arc.math.geom.*;
import arc.struct.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;

public class QuadMesh extends PlanetMesh{
    float radius;

    public QuadMesh(Planet planet, Shader shader, float radius){
        this.planet = planet;
        this.shader = shader;

        this.radius = radius;
        this.mesh = buildMesh();
    }

    public Mesh buildMesh(){
        FloatSeq vertices = new FloatSeq();

        for(Point2 offset : Geometry.d8edge) {
            vertices.add(offset.x * radius, 0f, offset.y * radius);
            vertices.add((offset.x + 1f) / 2f, (offset.y + 1f) / 2f);
        }

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
