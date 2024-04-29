package omaloon.content;

import arc.scene.style.TextureRegionDrawable;

import static arc.Core.*;
import static mindustry.gen.Icon.*;

public class OlIcons {
    public static TextureRegionDrawable settings, glasmore;
    public static void load() {
        settings = atlas.getDrawable("omaloon-settings");
        glasmore = atlas.getDrawable("omaloon-glasmore");
        icons.put("settings", settings);
        icons.put("glasmore", glasmore);
    }
}
