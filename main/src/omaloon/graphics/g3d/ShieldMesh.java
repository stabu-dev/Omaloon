package omaloon.graphics.g3d;

import arc.*;
import arc.graphics.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.graphics.g3d.*;
import mindustry.graphics.g3d.PlanetGrid.*;
import mindustry.type.*;
import omaloon.graphics.*;
import omaloon.graphics.OlShaders.*;

import java.nio.*;

import static mindustry.Vars.*;

public class ShieldMesh extends PlanetMesh{
    private static final IntBuffer viewport = Buffers.newIntBuffer(16);
    private static @Nullable FrameBuffer buffer;

    public Color color = Color.white;
    /** Wall height, relative to the planet radius. */
    public float height = 0.16f;
    /** Radius of the sphere the walls stand on, relative to the planet radius. Slightly below 1 to sink them into the terrain. */
    public float elevation = 0.98f;

    protected IntSet tiles = new IntSet();
    /** Terrain rendered with {@link OlShaders#depth}, used to hide the shields behind the planet surface. */
    protected GenericMesh occluder;

    public ShieldMesh(Planet planet, GenericMesh occluder){
        super(planet, null, null);
        this.occluder = occluder;
    }

    /** Covers the sector closest to the given coordinates in degrees, along with {@code range} rings of its neighbours. */
    public ShieldMesh shield(float latitude, float longitude, int range){
        Vec3 dir = new Vec3(Mathf.cosDeg(latitude) * Mathf.cosDeg(longitude), Mathf.sinDeg(latitude), Mathf.cosDeg(latitude) * Mathf.sinDeg(longitude));
        Ptile center = planet.grid.tiles[0];
        for(Ptile tile : planet.grid.tiles){
            if(tile.v.dot(dir) > center.v.dot(dir)) center = tile;
        }

        Seq<Ptile> front = Seq.with(center), next = new Seq<>();
        tiles.add(center.id);
        for(int i = 0; i < range; i++){
            for(Ptile tile : front){
                for(Ptile other : tile.tiles){
                    if(tiles.add(other.id)) next.add(other);
                }
            }
            front.set(next);
            next.clear();
        }
        return this;
    }

    public ShieldMesh build(){
        shader = OlShaders.planetShield;

        FloatSeq vertices = new FloatSeq();
        ShortSeq indices = new ShortSeq();
        float top = (elevation + height) * planet.radius, bottom = elevation * planet.radius;
        Vec3 normal = new Vec3(), outward = new Vec3();

        for(Ptile tile : planet.grid.tiles){
            if(!tiles.contains(tile.id)) continue;

            for(int k = 0; k < tile.edgeCount; k++){
                Corner ca = tile.corners[k], cb = tile.corners[(k + 1) % tile.edgeCount];
                if(Tmp.v31.set(ca.v).sub(tile.v).crs(Tmp.v32.set(cb.v).sub(tile.v)).dot(tile.v) < 0f){
                    Corner swap = ca;
                    ca = cb;
                    cb = swap;
                }

                int start = vertices.size / 6;

                vertex(vertices, Tmp.v31.set(tile.v).setLength(top), tile.v);
                vertex(vertices, Tmp.v31.set(ca.v).setLength(top), ca.v);
                vertex(vertices, Tmp.v31.set(cb.v).setLength(top), cb.v);
                indices.add((short)start, (short)(start + 1), (short)(start + 2));

                if(!isBorder(tile, ca, cb)) continue;

                outward.set(ca.v).add(cb.v).scl(0.5f).sub(tile.v);
                normal.set(outward).sub(Tmp.v31.set(tile.v).scl(outward.dot(tile.v))).nor();

                start += 3;
                vertex(vertices, Tmp.v31.set(ca.v).setLength(bottom), normal);
                vertex(vertices, Tmp.v31.set(cb.v).setLength(bottom), normal);
                vertex(vertices, Tmp.v31.set(cb.v).setLength(top), normal);
                vertex(vertices, Tmp.v31.set(ca.v).setLength(top), normal);
                indices.add((short)start, (short)(start + 1), (short)(start + 2));
                indices.add((short)start, (short)(start + 2), (short)(start + 3));
            }
        }

        mesh = new Mesh(true, vertices.size / 6, indices.size, VertexAttribute.position3, VertexAttribute.normal);
        mesh.setVertices(vertices.toArray());
        mesh.setIndices(indices.toArray());
        return this;
    }

    protected boolean isBorder(Ptile tile, Corner a, Corner b){
        for(Edge edge : tile.edges){
            if((edge.corners[0] == a && edge.corners[1] == b) || (edge.corners[0] == b && edge.corners[1] == a)){
                Ptile other = edge.tiles[0] == tile ? edge.tiles[1] : edge.tiles[0];
                return !tiles.contains(other.id);
            }
        }
        return true;
    }

    protected void vertex(FloatSeq vertices, Vec3 pos, Vec3 normal){
        vertices.add(pos.x, pos.y, pos.z);
        vertices.add(normal.x, normal.y, normal.z);
    }

    @Override
    public void preRender(PlanetParams params){
        OlShaders.planetShield.color.set(color);
        OlShaders.planetShield.lightDir.set(planet.solarSystem.position).sub(planet.position).nor();
        OlShaders.planetShield.ambientColor.set(planet.solarSystem.lightColor);
    }

    @Override
    public void render(PlanetParams params, Mat3D projection, Mat3D transform){
        if(mesh.isDisposed()) return;

        Gl.getIntegerv(Gl.viewport, viewport);
        int width = Math.max(viewport.get(2), 2), height = Math.max(viewport.get(3), 2);
        if(buffer == null) buffer = new FrameBuffer(width, height, true);
        buffer.resize(width, height);
        buffer.begin(Color.clear);
        Blending.disabled.apply();

        Gl.colorMask(false, false, false, false);
        OlShaders.depth.camera = renderer.planets.cam;
        occluder.render(params, projection, transform);
        Gl.colorMask(true, true, true, true);

        Gl.disable(Gl.cullFace);
        preRender(params);
        shader.bind();
        shader.setUniformMatrix4("u_proj", projection.val);
        shader.setUniformMatrix4("u_trans", transform.val);
        shader.apply();
        mesh.render(shader, Gl.triangles);

        buffer.end();
        Gl.enable(Gl.cullFace);

        PlanetShieldFieldShader field = OlShaders.planetShieldField;
        field.width = width;
        field.height = height;
        buffer.getTexture().bind(0);
        field.bind();
        field.setUniformMatrix4("u_proj", projection.val);
        field.setUniformMatrix4("u_trans", transform.val);
        field.apply();

        Blending.normal.apply();
        Gl.depthMask(false);
        mesh.render(field, Gl.triangles);
        Gl.depthMask(true);
    }
}
