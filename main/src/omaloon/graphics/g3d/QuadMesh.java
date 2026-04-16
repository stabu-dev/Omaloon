package omaloon.graphics.g3d;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;
import omaloon.graphics.*;

import static arc.Core.*;

public class QuadMesh extends PlanetMesh{
    private static final Mat3D trans = new Mat3D();

    Vec3 normal;
    float meshRadius;

    public float mag = 1f;
    public float scl = 1f;
    public @Nullable TextureRegion baseRegion;

    public QuadMesh(Planet planet, String regionName, float mag){
        this.planet = planet;
        this.shader = OlShaders.rings;
        this.baseRegion = atlas.find(regionName);
        this.mag = mag;
        this.normal = new Vec3(Vec3.Y).rotate(Vec3.X, 22f);
        
        updateMesh();
    }

    public void updateMesh(){
        float thickness = (baseRegion != null ? (float)baseRegion.height / baseRegion.width : 0.1f) * scl;
        this.meshRadius = mag + thickness/2f + 0.05f;
        
        if(mesh != null) mesh.dispose();
        
        FloatSeq vertices = new FloatSeq();
        Vec3 base = normal.cpy().crs(normal.equals(Vec3.Y) ? Vec3.X : Vec3.Y).nor();

        for(int i = 0; i < 4; i++){
            base.rotate(normal, 90f);
            vertices.add(base.x * meshRadius * Mathf.sqrt2, base.y * meshRadius * Mathf.sqrt2, base.z * meshRadius * Mathf.sqrt2);
            vertices.add(Mathf.num(i == 1 || i == 2), Mathf.num(i > 1f));
        }

        mesh = new Mesh(true, 4, 12, VertexAttribute.position3, VertexAttribute.texCoords);
        mesh.setVertices(vertices.toArray());
        mesh.setIndices(new short[] {0, 1, 2, 0, 2, 3,  0, 2, 1, 0, 3, 2});
    }

    @Override
    public void preRender(PlanetParams params){
        OlShaders.rings.baseRegion = baseRegion;
        OlShaders.rings.alpha = params.planet == planet ? params.uiAlpha : 1f;
        OlShaders.rings.planetPos = planet.position;
        OlShaders.rings.sunPos = planet.solarSystem.position;
        OlShaders.rings.planetRadius = planet.radius;

        float thickness = (baseRegion != null ? (float)baseRegion.height / baseRegion.width : 0.1f) * scl;
        OlShaders.rings.inRadius = (mag - thickness/2f) / meshRadius;
        OlShaders.rings.outRadius = (mag + thickness/2f) / meshRadius;
    }

    @Override
    public void render(PlanetParams params, Mat3D projection, Mat3D transform){
        if (Mathf.zero(1f - params.uiAlpha, 0.1f)) return;
        super.render(params, projection, trans.setTranslation(transform.getTranslation(Tmp.v31)));
    }
}
