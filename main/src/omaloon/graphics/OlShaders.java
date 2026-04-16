package omaloon.graphics;

import arc.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.*;
import mindustry.graphics.*;
import mindustry.type.*;
import omaloon.graphics.shaders.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class OlShaders{

//    public static @Nullable ShelterShader shelter;
//    public static final float shelterShieldLayer = 128f;

    public static DepthShader depth;
    public static DepthAtmosphereShader depthAtmosphere;

    public static @Nullable SurfaceShader glacium;
    public static CacheLayer.ShaderLayer glaciumLayer;

    public static PlanetTextureShader planetTextureShader;
    public static RingShader rings;

    public static void load(){
        String prevVert = Shader.prependVertexCode, prevFrag = Shader.prependFragmentCode;
        Shader.prependVertexCode = Shader.prependFragmentCode = "";

        if(graphics.getGLVersion().type == GLVersion.GlType.OpenGL){
            Shader.prependFragmentCode = "#define HAS_GL_FRAGDEPTH\n";
        }

        depth = new DepthShader();
        depthAtmosphere = new DepthAtmosphereShader();

        glacium = new SurfaceShader("glacium");
        glaciumLayer = new CacheLayer.ShaderLayer(glacium);
        CacheLayer.add(glaciumLayer);

        rings = new RingShader("rings", "rings");
        planetTextureShader = new PlanetTextureShader();

//        shelter = new ShelterShader();
//
//        Events.run(EventType.Trigger.draw, () -> {
//            if(renderer.animateShields && shelter != null){
//                Draw.drawRange(shelterShieldLayer, 1f, () -> renderer.effectBuffer.begin(Color.clear), () -> {
//                    renderer.effectBuffer.end();
//                    renderer.effectBuffer.blit(shelter);
//                });
//            }
//        });

        Shader.prependVertexCode = prevVert;
        Shader.prependFragmentCode = prevFrag;
    }

    public static void dispose(){
        if(!headless){
            glacium.dispose();
        }
    }

    /**
     * Resolves shader files from this mod via {@link Vars#tree}.
     * @param name The shader file name, e.g. {@code my-shader.frag}.
     * @return The shader file, located inside {@code shaders/confictura/}.
     */
    public static Fi file(String name){
        return tree.get("shaders/" + name);
    }

    public static class PlanetTextureShader extends OlLoadShader{
        public Vec3 lightDir = new Vec3(1, 1, 1).nor();
        public Color ambientColor = Color.white.cpy();
        public Vec3 camDir = new Vec3();
        public float alpha = 1f;
        public Planet planet;

        public PlanetTextureShader(){
            super("circle-mesh", "circle-mesh");
        }

        @Override
        public void apply(){
            camDir.set(renderer.planets.cam.direction).rotate(Vec3.Y, planet.getRotation());

            setUniformf("u_alpha", alpha);
            setUniformf("u_lightdir", lightDir);
            setUniformf("u_ambientColor", ambientColor.r, ambientColor.g, ambientColor.b);
            setPlanetInfo("u_sun_info", planet.solarSystem);
            setPlanetInfo("u_planet_info", planet);
            setUniformf("u_camdir", camDir);
            setUniformf("u_campos", renderer.planets.cam.position);
        }

        private void setPlanetInfo(String name, Planet planet){
            Vec3 position = planet.position;
            Shader shader = this;
            shader.setUniformf(name, position.x, position.y, position.z, planet.radius);
        }
    }

    public static class OlLoadShader extends Shader{

        public OlLoadShader(String fragment, String vertex){
            super(
            file(vertex + ".vert"),
            file(fragment + ".frag")
            );
        }

        public void set(){
            Draw.shader(this);
        }

        @Override
        public void apply(){
            super.apply();

            setUniformf("u_time_millis", System.currentTimeMillis() / 1000f * 60f);
        }
    }

    public static class RingShader extends Shader{
        public TextureRegion baseRegion;
        public float alpha;
        public float inRadius = 0.65f, outRadius = 1f;
        public float planetRadius = 1f;
        public Vec3 planetPos = new Vec3(), sunPos = new Vec3();

        public RingShader(String vertexShader, String fragmentShader){
            super(
                file(vertexShader + ".vert"),
                file(fragmentShader + ".frag")
            );
        }

        @Override
        public void apply(){
            if (baseRegion == null) baseRegion = Core.atlas.find("router");

            baseRegion.texture.bind(0);
            setUniformi("u_texture", 0);
            setUniformf("u_textureUV", baseRegion.u, baseRegion.v, baseRegion.u2, baseRegion.v2);

            setUniformf("u_opacity", 1f - alpha);

            setUniformf("u_planet_pos", planetPos.x, planetPos.y, planetPos.z);
            setUniformf("u_sun_pos", sunPos.x, sunPos.y, sunPos.z);

            setUniformf("u_stroke", inRadius, outRadius);
            setUniformf("u_planet_radius", planetRadius);
        }
    }

    public static class SurfaceShader extends Shader{
        Texture noiseTex;
        float seed = (float)Math.random();

        public SurfaceShader(String frag){
            super(Shaders.getShaderFi("screenspace.vert"), tree.get("shaders/" + frag + ".frag"));
            loadNoise();
        }

        public String textureName(){
            return "noise";
        }

        public void loadNoise(){
            assets.load("sprites/" + textureName() + ".png", Texture.class).loaded = t -> {
                t.setFilter(Texture.TextureFilter.linear);
                t.setWrap(Texture.TextureWrap.repeat);
            };
        }

        @Override
        public void apply(){
            setUniformf("u_campos",
            camera.position.x - camera.width / 2,
            camera.position.y - camera.height / 2
            );
            setUniformf("u_ccampos", camera.position);
            setUniformf("u_resolution", camera.width, camera.height);
            setUniformf("u_rresolution", graphics.getWidth(), graphics.getHeight());
            setUniformf("u_time", Time.time);
            setUniformf("u_seed", seed);

            if(hasUniform("u_noise")){
                if(noiseTex == null){
                    noiseTex = assets.get("sprites/" + textureName() + ".png", Texture.class);
                }

                noiseTex.bind(1);
                renderer.effectBuffer.getTexture().bind(0);

                setUniformi("u_noise", 1);
            }
        }
    }
}