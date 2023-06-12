package ol.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.gl.*;
import arc.util.*;
import mindustry.graphics.*;

import static mindustry.Vars.*;
import static mindustry.graphics.CacheLayer.*;

public class OlShaders {
    public static @Nullable OlSurfaceShader dalanii;
    public static CacheLayer.ShaderLayer dalaniiLayer;

    protected static boolean loaded;

    public static void load(){
        if(!headless){
            try{
                dalanii = new OlSurfaceShader("dalanii");
            }catch(Exception e){
                Log.err("There was an exception loading the shaders: @",e);
            }
            loaded = true;
        }
        dalaniiLayer = new CacheLayer.ShaderLayer(dalanii);
        CacheLayer.add(dalaniiLayer);
    }

    public static void dispose(){
        if(!headless && loaded){
            dalanii.dispose();
        }
    }

    /** Register a new CacheLayer. */
    public static void addUnder(CacheLayer... layers){
        int newSize = all.length + layers.length;
        var prev = all;
        //reallocate the array and copy everything over; performance matters very little here anyway
        all = new CacheLayer[newSize];
        System.arraycopy(prev, 0, all, layers.length, prev.length);
        System.arraycopy(layers, 0, all, 0, layers.length);

        for(int i = 0; i < all.length; i++){
            all[i].id = i;
        }
    }

    /** SurfaceShader but uses a mod fragment asset. */
    public static class OlSurfaceShader extends Shader{
        Texture noiseTex;
        String noiseTexName = "noise";

        public OlSurfaceShader(String frag){
            super(Core.files.internal("shaders/screenspace.vert"),
                    tree.get("shaders/" + frag + ".frag"));
            loadNoise();
        }

        public OlSurfaceShader(String vertRaw, String fragRaw){
            super(vertRaw, fragRaw);
            loadNoise();
        }

        public String textureName(){
            return noiseTexName;
        }

        public void loadNoise(){
            Core.assets.load("sprites/" + textureName() + ".png", Texture.class).loaded = t -> {
                t.setFilter(Texture.TextureFilter.linear);
                t.setWrap(Texture.TextureWrap.repeat);
            };
        }

        @Override
        public void apply(){
            setUniformf("u_campos", Core.camera.position.x - Core.camera.width / 2, Core.camera.position.y - Core.camera.height / 2);
            setUniformf("u_resolution", Core.camera.width, Core.camera.height);
            setUniformf("u_time", Time.time);

            if(hasUniform("u_noise")){
                if(noiseTex == null){
                    noiseTex = Core.assets.get("sprites/" + textureName() + ".png", Texture.class);
                }

                noiseTex.bind(1);
                renderer.effectBuffer.getTexture().bind(0);

                setUniformi("u_noise", 1);
            }
        }
    }
}