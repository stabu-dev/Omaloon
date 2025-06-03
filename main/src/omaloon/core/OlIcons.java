package omaloon.core;

import arc.scene.style.*;

import static arc.Core.atlas;
import static mindustry.gen.Icon.icons;

public class OlIcons{
    public static TextureRegionDrawable olSettings, glasmore, purpura;

    public static void load(){
        olSettings = atlas.getDrawable("omaloon-settings");
        glasmore = atlas.getDrawable("omaloon-glasmore");
        purpura = atlas.getDrawable("omaloon-purpura");
        icons.put("omaloon-settings", olSettings);
        icons.put("glasmore", glasmore);
        icons.put("purpura", purpura);
    }
}