package omaloon.graphics;

import arc.files.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.*;
import mindustry.graphics.*;
import mindustry.type.*;
import omaloon.*;
import omaloon.graphics.g3d.CircleMesh;

import static arc.Core.*;
import static mindustry.Vars.*;

public class OlShaders {
    public static @Nullable SurfaceShader dalani;
    public static CacheLayer.ShaderLayer dalaniLayer;

    public static PlanetTextureShader planetTextureShader;

    public static void init() {
        dalani = new SurfaceShader("dalani");
        dalaniLayer = new CacheLayer.ShaderLayer(dalani);
        CacheLayer.add(dalaniLayer);

        planetTextureShader = new PlanetTextureShader();
    }

    public static void dispose(){
        if(!headless){
            dalani.dispose();
        }
    }

    public static class PlanetTextureShader extends OlLoadShader{
        public Vec3 lightDir = new Vec3(1, 1, 1).nor();
        public Color ambientColor = Color.white.cpy();
        public Vec3 camDir = new Vec3();
        public float alpha = 1f;
        public Planet planet;

        public PlanetTextureShader(OlLoadShader fragment, String vertex){
            super(fragment, vertex);
        }

        public PlanetTextureShader(String fragment, OlLoadShader vertex){
            super(fragment, vertex);
        }

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
        public OlLoadShader(OlLoadShader fragment, String vertex){
            super(vertex, fragment.getFragmentShaderSource());
        }
        public OlLoadShader(String fragment, OlLoadShader vertex){
            super(vertex.getVertexShaderSource(), fragment);
        }

        public OlLoadShader(String fragment, String vertex){
            super(
                    load("" + vertex + ".vert"),
                    load("" + fragment + ".frag")
            );
        }

        public static Fi load(String path){
            Fi tree = Vars.tree.get("shaders/" + path);
            return tree.exists() ? tree : OmaloonMod.modInfo.root.child("shaders").findAll(file ->
                    file.name().equals(path)).first();
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

    public static class SurfaceShader extends Shader {
        Texture noiseTex;

        public SurfaceShader(String frag) {
            super(Shaders.getShaderFi("screenspace.vert"), tree.get("shaders/" + frag + ".frag"));
            loadNoise();
        }

        public String textureName() {
            return "noise";
        }

        public void loadNoise() {
            assets.load("sprites/" + textureName() + ".png", Texture.class).loaded = t -> {
                t.setFilter(Texture.TextureFilter.linear);
                t.setWrap(Texture.TextureWrap.repeat);
            };
        }

        @Override
        public void apply() {
            setUniformf("u_campos",
                    camera.position.x - camera.width / 2,
                    camera.position.y - camera.height / 2
            );
            setUniformf("u_ccampos", camera.position);
            setUniformf("u_resolution", camera.width, camera.height);
            setUniformf("u_rresolution", graphics.getWidth(), graphics.getHeight());
            setUniformf("u_time", Time.time);

            if(hasUniform("u_noise")) {
                if(noiseTex == null) {
                    noiseTex = assets.get("sprites/" + textureName() + ".png", Texture.class);
                }

                noiseTex.bind(1);
                renderer.effectBuffer.getTexture().bind(0);

                setUniformi("u_noise", 1);
            }
        }
    }
}
