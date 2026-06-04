package omaloon.graphics.g3d;

import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.graphics.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;

public class PillarMesh extends PlanetMesh {
    public float baseRadius, tipRadius;
    public float height;
    public int sides;
    public Color color = Color.white;
    public Vec3 normal = Vec3.Y;

    public PillarMesh(Planet planet){
        super(planet, null, Shaders.unlit);
    }

    public PillarMesh build(){

        FloatSeq vertices = new FloatSeq();
        ShortSeq indexes = new ShortSeq();

        Vec3 base = normal.cpy().crs(normal.equals(Vec3.Y) ? Vec3.X : Vec3.Y).nor();

        for(int i = 0; i < sides; i++){
            float angle = 360f / sides * i;

            Tmp.v31.set(base).rotate(normal, angle).setLength(tipRadius);
            Tmp.v31.add(normal.cpy().setLength(height));

            OlMeshBuilder.vertex(vertices, Tmp.v31.cpy(), color);
            if (i > 1) {
                indexes.add((short) (i - 1), (short) 0, (short) i);
            }
        }

        for(int i = 0; i < sides; i++){
            float angle = 360f / sides * i;

            Tmp.v31.set(base).rotate(normal, angle).setLength(baseRadius);

            OlMeshBuilder.vertex(vertices, Tmp.v31.cpy(), color);
            indexes.add((short) (sides + i), (short) (i), (short) ((i + 1) % sides));
            indexes.add((short) (sides + i), (short) ((i + 1) % sides), (short) (sides + (i + 1) % sides));
        }

//        OlMeshBuilder.vertex(vertices, tip, tip.cpy().nor(), color);
//        for(int i = 0; i < sides; i++){
//            OlMeshBuilder.vertex(vertices, base[i], nor[i], color);
//            indexes.add((short) (i + verticeCount + 1), (short) verticeCount, (short) ((i + 1) % sides + verticeCount + 1));
//        }
//        verticeCount += sides + 1;

        mesh = new Mesh(true, vertices.size, indexes.size, VertexAttribute.position3, VertexAttribute.color);
        mesh.getVerticesBuffer().limit(vertices.size);
        mesh.getVerticesBuffer().position(0);
        mesh.getIndicesBuffer().limit(indexes.size);
        mesh.getIndicesBuffer().position(0);
        mesh.setVertices(vertices.toArray());
        mesh.setIndices(indexes.toArray());

        return this;
    }

    @Override
    public void preRender(PlanetParams params) {
        Shaders.planet.planet = planet;
        Shaders.planet.lightDir.set(planet.solarSystem.position).sub(planet.position).rotate(Vec3.Y, planet.getRotation()).nor();
        Shaders.planet.ambientColor.set(planet.solarSystem.lightColor);
    }
}
