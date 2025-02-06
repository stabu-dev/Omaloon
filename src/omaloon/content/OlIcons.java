package omaloon.content;

import arc.scene.style.*;

import static arc.Core.atlas;
import static mindustry.gen.Icon.icons;

public class OlIcons{
    public static TextureRegionDrawable settings, glasmore, purpura;

    public static void load(){
        settings = atlas.getDrawable("omaloon-settings");
        glasmore = atlas.getDrawable("omaloon-glasmore");
        purpura = atlas.getDrawable("omaloon-purpura");
        icons.put("settings", settings);
        icons.put("glasmore", glasmore);
        icons.put("purpura", purpura);
    }
}
