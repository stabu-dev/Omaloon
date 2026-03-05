package omaloon.graphics.g3d;

import arc.graphics.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.graphics.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;

public class PyramidMesh extends PlanetMesh {
    public int sides = 3;
    public float tipHeightMin = 1f, tipHeightMax = 2f;
    public float baseHeightMin = 0f, baseHeightMax = 1f;
    public float baseRadiusMin = 0.5f, baseRadiusMax = 1f;
    public int seed = 1;
    //ugly, but I didn't find a better way to do this.
    //rotates the iceberg along the axis, by angle.
    //by default, it is made pointing at Vec3.y, and so rotating by it won't work, you'll need a different vector.
    public Vec3 axis = Vec3.X;
    public float angle = 0f;
    public Color color = Color.white;

    public PyramidMesh(Planet planet){
        super(planet, null, Shaders.planet);
    }

    public PyramidMesh build() {
        Mathf.rand.setSeed(seed);

        FloatSeq vertices = new FloatSeq();
        ShortSeq indexes = new ShortSeq();

        Vec3 tip = Vec3.Y.cpy().setLength(Mathf.random(tipHeightMin, tipHeightMax)).rotate(axis, angle);

        Vec3[] base = new Vec3[sides];
        Vec3[] nor = new Vec3[sides];

        for (int i = 0; i < sides; i++) {
            float angle = 360f/base.length * i;

            Tmp.v31.set(Vec3.X).rotate(Vec3.Y, angle).setLength(Mathf.random(baseRadiusMin, baseRadiusMax));

            nor[i] = Tmp.v31.cpy().rotate(axis, PyramidMesh.this.angle).nor();

            Tmp.v31.y += Mathf.random(baseHeightMin, baseHeightMax);
            base[i] = Tmp.v31.cpy().rotate(axis, PyramidMesh.this.angle);
        }

        OlMeshBuilder.vertex(vertices, tip, tip.cpy().nor(), color);
        for (int i = 0; i < sides; i++) {
            OlMeshBuilder.vertex(vertices, base[i], nor[i], color);
            indexes.add((short) (i + 1), (short) 0, (short) ((i + 1) % base.length + 1));
        }

        mesh = new Mesh(true, sides + 1, indexes.size * 6, VertexAttribute.position3, VertexAttribute.normal, VertexAttribute.color);
        mesh.getVerticesBuffer().limit(sides + 1);
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
