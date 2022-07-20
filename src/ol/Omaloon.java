package ol;

import arc.*;
import arc.func.Func;
import arc.util.*;
import mindustry.Vars;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;
import ol.content.*;
import ol.graphics.OlShaders;
import ol.system.SolarSystem;
import ol.ui.Disclaimer;
import ol.ui.OlSettings;

import static arc.Core.app;
import static mindustry.Vars.headless;
import static mindustry.Vars.ui;

public class Omaloon extends Mod{
    public static Mods.LoadedMod modInfo;

    @Override
    public void init(){
        super.init();
        SolarSystem.init();
        LoadedMod mod = Vars.mods.locateMod("ol");
        if(!headless){
            //forom Betamindy by sk7725
            Func<String, String> stringf = value -> Core.bundle.get("mod." + value);

            mod.meta.displayName = stringf.get(mod.meta.name + ".name");
            mod.meta.description = Core.bundle.get("mod.ol.description") +"\n\n"+ Core.bundle.get("mod.ol.musics");
            mod.meta.author = Core.bundle.get("mod.ol.author") + "\n\n" + Core.bundle.get("mod.ol.contributors");
            mod.meta.subtitle = "[#7f7f7f]"+"v"+mod.meta.version+"[]" +"\n"+ Core.bundle.get("mod.ol.subtitle");
            Events.on(ClientLoadEvent.class, e -> {
                loadSettings();
                OlSettings.init();
                Core.app.post(() -> Core.app.post(() -> {
                    if(!Core.settings.getBool("@mod.ol.show", false)) {
                        new Disclaimer().show();
                    }
                }));
            });
        }
    }

    void loadSettings() {
        ui.settings.addCategory("@mod.ol.omaloon-settings", "ol-omaloon-settings-icon", t -> {
            t.checkPref("@mod.ol.show", true);
        });
    }


    public Omaloon(){
        Events.on(FileTreeInitEvent.class, e -> app.post(OlSounds::load));
        Log.info("Loaded Omaloon constructor.");
        Vars.mods.getMod(getClass());
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
