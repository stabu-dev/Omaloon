package omaloon.graphics.shaders;

import arc.graphics.g3d.*;
import arc.graphics.gl.*;
import arc.math.geom.*;
import arc.util.*;
import omaloon.type.*;

import static arc.Core.graphics;
import static omaloon.graphics.OlShaders.file;

/**
 * An atmosphere shader that incorporates the planet shape in a form of depth texture. Better quality, but at the little
 * cost of performance.
 * @author GlFolker
 */
public class DepthAtmosphereShader extends Shader{
    private static final Mat3D mat = new Mat3D();

    public Camera3D camera;
    public BetterPlanet planet;

    public DepthAtmosphereShader(){
        super(file("depth-atmosphere.vert"), file("depth-atmosphere.frag"));
    }

    @Override
    public void apply(){
        setUniformMatrix4("u_proj", camera.combined.val);
        setUniformMatrix4("u_trans", planet.getTransform(mat).val);

        setUniformf("u_camPos", camera.position);
        setUniformf("u_relCamPos", Tmp.v31.set(camera.position).sub(planet.position));
        setUniformf("u_camRange", camera.near, camera.far - camera.near);
        setUniformf("u_center", planet.position);
        setUniformf("u_light", planet.getLightNormal());
        setUniformf("u_color", planet.atmosphereColor.r, planet.atmosphereColor.g, planet.atmosphereColor.b);

        setUniformf("u_innerRadius", planet.radius + planet.atmosphereRadIn);
        setUniformf("u_outerRadius", planet.radius + planet.atmosphereRadOut);

        planet.depthBuffer.getTexture().bind(0);
        setUniformi("u_topology", 0);
        setUniformf("u_viewport", graphics.getWidth(), graphics.getHeight());
    }
}
