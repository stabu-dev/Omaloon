package ol.graphics.g3d;

import arc.graphics.Color;
import arc.graphics.Gl;
import arc.math.Mathf;
import arc.math.geom.Mat3D;
import arc.math.geom.Vec3;
import arc.util.noise.Simplex;
import mindustry.graphics.Shaders;
import mindustry.graphics.g3d.HexMesher;
import mindustry.graphics.g3d.MeshBuilder;
import mindustry.graphics.g3d.PlanetMesh;
import mindustry.graphics.g3d.PlanetParams;
import mindustry.type.Planet;

public class Rings extends PlanetMesh {
    static Mat3D mat = new Mat3D();

    public Rings (Planet planet, int seed, float radius, int divisions, Color color, int octaves, float persistence, float scl, float thresh){
        super(planet, MeshBuilder.buildHex(new HexMesher(){

            @Override
            public float getHeight(Vec3 position) {
                return 1f;
            }

            @Override
            public Color getColor(Vec3 position) {
                return color;
            }

            @Override
            public boolean skip(Vec3 position){
                return Simplex.noise3d(planet.id + seed, octaves, persistence, scl, position.x, position.y * 3f, position.z) >= thresh;
            }
        }, divisions, false, planet.radius, radius), Shaders.clouds);
    }
    public Rings(){
    }

    @Override
    public void render(PlanetParams params, Mat3D projection, Mat3D transform){
        //don't waste performance rendering 0-alpha clouds
        if(params.planet == planet && Mathf.zero(1f - params.uiAlpha, 0.01f)) return;

        preRender(params);
        shader.bind();
        shader.setUniformMatrix4("u_proj", projection.val);
        shader.setUniformMatrix4("u_trans", mat.setToTranslation(planet.position).rotate(Vec3.Y, planet.getRotation()).val);
        shader.apply();
        mesh.render(shader, Gl.triangles);
    }

    @Override
    public void preRender(PlanetParams params){
        Shaders.clouds.planet = planet;
        Shaders.clouds.lightDir.set(planet.solarSystem.position).sub(planet.position).rotate(Vec3.Y, planet.getRotation()).nor();
        Shaders.clouds.ambientColor.set(planet.solarSystem.lightColor);
        Shaders.clouds.alpha = params.planet == planet ? 1f - params.uiAlpha : 1f;
    }
}
