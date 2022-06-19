package ol.graphics;

import arc.*;
import arc.files.*;
import arc.graphics.*;
import arc.graphics.Texture.*;
import arc.graphics.g2d.*;
import arc.graphics.g3d.*;
import arc.graphics.gl.*;
import arc.math.geom.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.graphics.CacheLayer;
import mindustry.graphics.Shaders;
import mindustry.type.*;

import static mindustry.Vars.*;
import static mindustry.graphics.Shaders.getShaderFi;

public class OlShaders {
    public static @Nullable OlSurfaceShader dalanite;
    public static CacheLayer.ShaderLayer dalaniteLayer;
    protected static boolean loaded;

    public static void init(){
        dalanite = new OlSurfaceShader("dalanite");
    }

    public static void load() {
        if(!headless){
            dalanite = new OlSurfaceShader("dalanite");
        }
        Log.info("[accent]<FTE + POST (CACHELAYER)>[]");
        dalaniteLayer = new CacheLayer.ShaderLayer(dalanite);
        CacheLayer.add(dalaniteLayer);
    }

    public static class OlSurfaceShader extends Shader{
        Texture noiseTex;

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
            return "noise";
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
