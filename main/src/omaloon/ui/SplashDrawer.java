package omaloon.ui;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.layout.Scl;
import arc.util.*;
import mindustry.graphics.Pal;
import mindustry.mod.Mods;
import omaloon.OmaloonMod;

import java.io.*;

import static arc.Core.assets;
import static mindustry.Vars.clientLoaded;

public class SplashDrawer implements Disposable{
    private static final Color
    backgroundColor = Color.valueOf("222222"),
    versionColor = Color.valueOf("444444");
    private static final float
    iconSize = 256f,
    backgroundFadeInDuration = 0.15f,
    elementsFadeInDuration = 0.4f,
    elementsFadeOutDuration = 0.3f,
    backgroundFadeOutDelay = 0.2f,
    backgroundFadeOutDuration = 0.4f;

    private final Texture iconTex;
    private final TextureRegion icon;
    private Font font;
    private final String version;
    private final long startTime;

    private boolean fadingOut = false;
    private long fadeOutStartTime;

    private byte[] readStream(InputStream inputStream) throws IOException{
        try(ByteArrayOutputStream buffer = new ByteArrayOutputStream()){
            int nRead;
            byte[] data = new byte[1024];
            while((nRead = inputStream.read(data, 0, data.length)) != -1){
                buffer.write(data, 0, nRead);
            }
            return buffer.toByteArray();
        }
    }

    public SplashDrawer(Mods.LoadedMod mod){
        this.version = mod.meta.version;

        try(InputStream iconStream = OmaloonMod.class.getResourceAsStream("/sprites/ui/splash-icon.png")){
            if(iconStream == null){
                throw new IOException("Splash screen image stream was null. Check asset packaging.");
            }

            byte[] iconBytes = readStream(iconStream);
            Pixmap iconPixmap = new Pixmap(iconBytes, 0, iconBytes.length);
            iconTex = new Texture(iconPixmap);
            iconPixmap.dispose();
            icon = new TextureRegion(iconTex);

            startTime = Time.millis();
            Core.app.post(this::draw);
        }catch(Exception e){
            throw new RuntimeException("Failed to load Omaloon splash screen images.", e);
        }
    }

    private void draw(){
        float totalFadeTime = backgroundFadeOutDelay + backgroundFadeOutDuration;
        if(fadingOut && Time.timeSinceMillis(fadeOutStartTime) >= totalFadeTime * 1000f){
            dispose();
            return;
        }

        if(clientLoaded && !fadingOut){
            fadingOut = true;
            fadeOutStartTime = Time.millis();
        }

        float backgroundAlpha;
        float elementsAlpha;

        if(fadingOut){
            float elapsed = Time.timeSinceMillis(fadeOutStartTime) / 1000f;
            elementsAlpha = 1f - Interp.fade.apply(Math.min(elapsed / elementsFadeOutDuration, 1f));

            backgroundAlpha = 1f;
            if(elapsed > backgroundFadeOutDelay){
                float progress = (elapsed - backgroundFadeOutDelay) / backgroundFadeOutDuration;
                backgroundAlpha = 1f - Interp.fade.apply(Math.min(progress, 1f));
            }
        }else{
            float time = Time.timeSinceMillis(startTime) / 1000f;
            backgroundAlpha = Interp.fade.apply(Math.min(time / backgroundFadeInDuration, 1f));
            elementsAlpha = Interp.fade.apply(Math.min(time / elementsFadeInDuration, 1f));
        }

        Draw.proj().setOrtho(0, 0, Core.graphics.getWidth(), Core.graphics.getHeight());

        Draw.color(backgroundColor, backgroundAlpha);
        Fill.rect(Core.graphics.getWidth()/2f, Core.graphics.getHeight()/2f, Core.graphics.getWidth(), Core.graphics.getHeight());

        float scaledIconSize = iconSize * Scl.scl();
        Draw.color(Color.white, elementsAlpha);
        Draw.rect(icon, Core.graphics.getWidth() / 2f, Core.graphics.getHeight() / 2f, scaledIconSize, scaledIconSize);

        if(assets.isLoaded("tech")){
            if(font == null) font = assets.get("tech");

            font.getData().setScale(1f);
            font.setColor(Tmp.c1.set(versionColor).a(elementsAlpha));

            font.draw(version,
            Core.graphics.getWidth() / 2f,
            Core.graphics.getHeight() / 2f - scaledIconSize / 2f - Scl.scl(20f),
            0, Align.center, false);

            font.setColor(Color.white);
        }

        if(!fadingOut){
            float progress = assets.getProgress();
            float w = Core.graphics.getWidth();
            Draw.color(Pal.accent, elementsAlpha);
            Fill.rect(w / 2, 40, w * progress, 10);
        }

        Draw.flush();

        Core.app.post(this::draw);
    }

    @Override
    public void dispose(){
        iconTex.dispose();
    }
}