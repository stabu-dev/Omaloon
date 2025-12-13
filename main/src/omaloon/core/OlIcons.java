package omaloon.core;

import arc.scene.style.*;

import static arc.Core.atlas;
import static mindustry.gen.Icon.icons;

public class OlIcons{
    public static TextureRegionDrawable olSettings, glasmore, purpura;

    public static void load(){
        olSettings = atlas.getDrawable("omaloon-settings-ui");
        glasmore = atlas.getDrawable("omaloon-glasmore-ui");
        purpura = atlas.getDrawable("omaloon-purpura-ui");
        icons.put("omaloon-settings", olSettings);
        icons.put("glasmore", glasmore);
        icons.put("purpura", purpura);
    }
}