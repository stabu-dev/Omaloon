package omaloon.graphics.g3d;


import arc.graphics.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.graphics.g3d.*;
import omaloon.graphics.*;
import omaloon.type.planet.*;

import static arc.Core.*;
import static mindustry.Vars.*;

/**
 * Modified version of {@link omaloon.type.planet.BetterPlanet.AtmosphereHexMesh} to work using multiple meshes.
 * To use it, have a duplicate of whatever meshes the planet has, but with all of their shaders set to {@link OlShaders#depth}
 */
public class AtmosphereMesh implements GenericMesh{
    protected BetterPlanet planet;
    protected GenericMesh mesh;

    public AtmosphereMesh(BetterPlanet planet, GenericMesh mesh){
        this.planet = planet;
        this.mesh = mesh;
    }

    @Override
    public void render(PlanetParams params, Mat3D projection, Mat3D transform){
        if(params.alwaysDrawAtmosphere || settings.getBool("atmosphere")){
            var depth = OlShaders.depth;
            planet.depthBuffer.resize(graphics.getWidth(), graphics.getHeight());
            planet.depthBuffer.begin(Tmp.c1.set(0xffffff00));
            Blending.disabled.apply();

            depth.camera = renderer.planets.cam;
//            depth.bind();
//            depth.setUniformMatrix4("u_proj", projection.val);
//            depth.setUniformMatrix4("u_trans", transform.val);
//            depth.apply();
            mesh.render(params, projection, transform);

            Blending.normal.apply();
            planet.depthBuffer.end();
        }

//        var shader = Shaders.planet;
//        shader.planet = BetterPlanet.this;
//        shader.lightDir.set(solarSystem.position).sub(position).rotate(Vec3.Y, getRotation()).nor();
//        shader.ambientColor.set(solarSystem.lightColor);
//        shader.bind();
//        shader.setUniformMatrix4("u_proj", projection.val);
//        shader.setUniformMatrix4("u_trans", transform.val);
//        shader.apply();
//        mesh.render(shader, Gl.triangles);
    }

    @Override
    public void dispose(){
        mesh.dispose();
    }
}