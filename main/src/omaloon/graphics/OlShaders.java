package omaloon.graphics;

import arc.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.*;
import mindustry.graphics.*;
import mindustry.type.*;
import omaloon.graphics.shaders.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class OlShaders{

    public static @Nullable ShelterShader shelter;

    public static DepthShader depth;
    public static DepthAtmosphereShader depthAtmosphere;

    public static @Nullable SurfaceShader glacium;
    public static CacheLayer.ShaderLayer glaciumLayer;

    public static PlanetTextureShader planetTextureShader;
    public static RingShader rings;
    public static PlanetShieldShader planetShield;
    public static PlanetShieldFieldShader planetShieldField;

    public static Shader alphaCut, harshShadow, impactMask, imprintCut, shockwave, sharedShockwave;

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
        planetShield = new PlanetShieldShader();
        planetShieldField = new PlanetShieldFieldShader();
        try{
            shelter = new ShelterShader();
        }catch(Throwable e){
            Log.err("Shelter field shaders failed to load", e);
        }

        Shader.prependVertexCode = prevVert;
        Shader.prependFragmentCode = prevFrag;
    }

    public static boolean loadBlasts(){
        if(alphaCut != null) return true;
        try{
            String screen = Shaders.getShaderFi("screenspace.vert").readString(), vertex = Shaders.getShaderFi("default.vert").readString(), wave = file("shockwave.frag").readString();
            alphaCut = new Shader(vertex, impactSource("CUT"));
            harshShadow = new Shader(screen, impactSource("SHADOW"));
            impactMask = new Shader(vertex, impactSource("MASK"));
            imprintCut = new Shader(screen, impactSource("UNSATURATED"));
            shockwave = new Shader(screen, wave);
            sharedShockwave = new Shader(screen, "#define SHARED_NOISE true\n" + wave);
            return true;
        }catch(RuntimeException e){
            disposeBlasts();
            Log.err("Nuclear blast shaders failed to load; using reduced effects", e);
            return false;
        }
    }

    public static void disposeBlasts(){
        if(alphaCut != null) alphaCut.dispose();
        if(harshShadow != null) harshShadow.dispose();
        if(impactMask != null) impactMask.dispose();
        if(imprintCut != null) imprintCut.dispose();
        if(shockwave != null) shockwave.dispose();
        if(sharedShockwave != null) sharedShockwave.dispose();
        alphaCut = harshShadow = impactMask = imprintCut = shockwave = sharedShockwave = null;
    }

    public static void dispose(){
        if(!headless){
            glacium.dispose();
            planetShield.dispose();
            planetShieldField.dispose();
            if(shelter != null) shelter.dispose();
            disposeBlasts();
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

    public static class PlanetShieldShader extends Shader{
        public Vec3 lightDir = new Vec3(1, 1, 1).nor();
        public Color ambientColor = Color.white.cpy(), color = Color.white.cpy();

        public PlanetShieldShader(){
            super(file("planet-shield.vert"), file("planet-shield.frag"));
        }

        @Override
        public void apply(){
            setUniformf("u_color", color);
            setUniformf("u_lightdir", lightDir);
            setUniformf("u_ambientColor", ambientColor.r, ambientColor.g, ambientColor.b);
        }
    }

    public static class PlanetShieldFieldShader extends Shader{
        public float pixelScale = 2.5f;
        public float speed = 0.35f;
        public float width = 1f, height = 1f;
        public float outline = 2f;

        public PlanetShieldFieldShader(){
            super(file("planet-shield.vert").readString(), "#define FIELD\n" + file("planet-shield.frag").readString());
        }

        @Override
        public void apply(){
            float scale = Scl.scl(pixelScale), sizeX = width / scale, sizeY = height / scale;

            setUniformi("u_texture", 0);
            setUniformf("u_resolution", width, height);
            setUniformf("u_dp", Scl.scl(1f));
            setUniformf("u_time", Time.globalTime * speed / Scl.scl(1f));
            setUniformf("u_texsize", sizeX, sizeY);
            setUniformf("u_invsize", 1f / sizeX, 1f / sizeY);
            setUniformf("u_outline", outline);
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

    private static String impactSource(String mode){
        return "#define " + mode + "\n" + file("impact.frag").readString();
    }
}