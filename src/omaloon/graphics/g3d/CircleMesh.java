package omaloon.graphics.g3d;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.*;
import arc.math.geom.*;
import mindustry.graphics.g3d.*;
import mindustry.type.*;
import omaloon.graphics.*;

import static omaloon.graphics.g3d.MeshPoint.*;

/**
 * @author Zelaux
 */
public class CircleMesh extends PlanetMesh{
    public static final int[] VERTEX_ORDER = new int[]{0, 1, 2, 2, 3, 0};
    public static final MeshPoint[] MESH_POINTS = new MeshPoint[]{
        new MeshPoint(new Vec3(), 0, 0, innerRadius, false),
        new MeshPoint(new Vec3(), 1, 0, outerRadius, false),
        new MeshPoint(new Vec3(), 1, 1, outerRadius, true),
        new MeshPoint(new Vec3(), 0, 1, innerRadius, true),
    };
    public final Mesh mesh;
    public TextureRegion region;
    public Texture texture;
    public Color color = Color.white.cpy();

    public CircleMesh(TextureRegion region, Planet planet, int sides, float radiusIn, float radiusOut, Vec3 axis){
        this.planet = planet;
        this.region = region;

        MeshUtils.begin(sides * 6/*points amount*/ * (3/*pos*/ + 3/*normal*/ + 2/*texCords*/) * 2/*top and bottom normal*/);

        Vec3 plane = new Vec3();
        if(axis.y == 0){
            plane.set(0, 1, 0);
        }else{
            float val = axis.dot(1, 0, 1);
            float calcY = -val / axis.y;

            float len = Mathf.sqrt(2 + calcY * calcY);
            float invLen = 1 / len;

            plane.set(invLen, calcY / len, invLen);
        }

        Vec3 inv = axis.cpy().unaryMinus();

        float angleStep = 360f / sides;
        for(int i = 0; i < sides; i++){
            float angle = i * angleStep;
            float nextAngle = angle + angleStep;
            for(MeshPoint p : MESH_POINTS){
                p.position
                    .set(plane)
                    .rotate(axis, p.nextAngle ? nextAngle : angle)
                    .scl(p.radiusIndex == innerRadius ? radiusIn : radiusOut);
            }

            for(int j : VERTEX_ORDER){
                MeshPoint point = MESH_POINTS[j];
                MeshUtils.vert(point.position, axis, point.textureCordsX, point.textureCordsY);
            }
            for(int j = VERTEX_ORDER.length - 1; j >= 0; j--){
                MeshPoint point = MESH_POINTS[VERTEX_ORDER[j]];
                MeshUtils.vert(point.position, inv, point.textureCordsX, point.textureCordsY);
            }
        }

        mesh = MeshUtils.end();
    }

    private static Shader shader(){
        return OlShaders.planetTextureShader;
    }

    @Override
    public void render(PlanetParams params, Mat3D projection, Mat3D transform){
        //don't waste performance rendering 0-alpha
        if(params.planet == planet && Mathf.zero(1f - params.uiAlpha, 0.01f)) return;

        preRender(params);
        if(texture == null){
            texture = new Texture(Core.atlas.getPixmap(region).crop());
        }

        Shader shader = shader();
        shader.bind();
        shader.setUniformMatrix4("u_proj", projection.val);
        shader.setUniformMatrix4("u_trans", transform.val);
        shader.setUniformf("u_color", color);
        setPlanetInfo("u_sun_info", planet.solarSystem);
        setPlanetInfo("u_planet_info", planet);
        texture.bind(0);
        shader.setUniformi("u_texture", 0);
        shader.apply();

        mesh.render(shader, Gl.triangles);
    }

    @Override
    public void preRender(PlanetParams params){
        OlShaders.planetTextureShader.planet = planet;
        OlShaders.planetTextureShader.lightDir
            .set(planet.solarSystem.position)
            .sub(planet.position)
            .rotate(Vec3.Y, planet.getRotation())
            .nor();
        OlShaders.planetTextureShader.ambientColor
            .set(planet.solarSystem.lightColor);
        //TODO: better disappearing
        OlShaders.planetTextureShader.alpha = params.planet == planet ? 1f - params.uiAlpha : 1f;
    }

    private void setPlanetInfo(String name, Planet planet){
        Vec3 position = planet.position;
        Shader shader = shader();
        shader.setUniformf(name, position.x, position.y, position.z, planet.radius);
    }

}