package ol;

import arc.*;
import arc.func.Func;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;
import ol.content.*;
import ol.graphics.OlShaders;
import ol.system.SolarSystem;
import ol.ui.Disclaimer;
import ol.ui.OlSettings;

import java.util.Random;

import static arc.Core.app;
import static arc.Core.settings;
import static mindustry.Vars.*;

public class Omaloon extends Mod{
    public static Mods.LoadedMod modInfo;

    @Override
    public void init(){
        super.init();
        SolarSystem.init();
        LoadedMod mod = mods.locateMod("ol");
        if(!headless){
            //forom Betamindy by sk7725
            Func<String, String> stringf = value -> Core.bundle.get("mod." + value);

            mod.meta.displayName = stringf.get(mod.meta.name + ".name");
            mod.meta.description = Core.bundle.get("mod.ol.description") +"\n\n"+ Core.bundle.get("mod.ol.musics");
            mod.meta.author = Core.bundle.get("mod.ol.author") + "\n\n" + Core.bundle.get("mod.ol.contributors");
            //String 1 = Integer.parseInt(String.valueOf(Integer.parseInt(Core.bundle.get("mod.ol.subtitle1"))));
            String [] r = {
                    Core.bundle.get("mod.ol.subtitle1"),
                    Core.bundle.get("mod.ol.subtitle2"),
                    Core.bundle.get("mod.ol.subtitle3")
            };
            Random rand = new Random();
            String mogus = String.valueOf(
                    r[rand.nextInt(3)]
            );
            mod.meta.subtitle = "[#7f7f7f]"+"v"+mod.meta.version+"[]" +"\n"+ mogus;
            Events.on(ClientLoadEvent.class, e -> {
                loadSettings();
                OlSettings.init();
                app.post(() -> app.post(() -> {
                    if(!settings.getBool("mod.ol.show", false)) {
                        new Disclaimer().show();
                    }
                }));
            });
        }
    }

    void loadSettings() {
        ui.settings.addCategory("@mod.ol.omaloon-settings", "ol-settings-icon", t -> {
            t.checkPref("mod.ol.show", false);
            t.checkPref("mod.ol.update-check", true);
        });
    }

    public static void log(String info) {
        app.post(() -> Log.infoTag("Omaloon", info));
    }

    public static void error(Throwable info) {
        app.post(() -> Log.err("Omaloon", info));
    }

    public Omaloon(){
        Events.on(FileTreeInitEvent.class, e -> app.post(OlSounds::load));
        Log.info("Loaded Omaloon constructor.");
        mods.getMod(getClass());
        Events.on(FileTreeInitEvent.class, e -> Core.app.post(OlShaders::load));
    }

    @Override
    public void loadContent(){
        Log.info("Loading some content.");
        OlItems.load();
        OlStatusEffects.load();
        OlLiquids.load();
        OlBlocks.load();
        OlPlanets.load();
        OlSounds.load();
    }
}
