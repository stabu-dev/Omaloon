package ol;

import arc.Core;
import arc.Events;
import arc.func.Func;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Table;
import arc.util.Log;
import mindustry.game.EventType.ClientLoadEvent;
import mindustry.game.EventType.FileTreeInitEvent;
import mindustry.gen.Icon;
import mindustry.mod.Mod;
import mindustry.mod.Mods.LoadedMod;
import ol.content.*;
import ol.graphics.OlShaders;
import ol.system.SolarSystem;
import ol.ui.dialogs.OlDisclaimer;
import ol.ui.dialogs.OlDiscordLink;
import ol.ui.OlSettings;

import java.util.Random;

import static arc.Core.*;
import static mindustry.Vars.*;

public class Omaloon extends Mod{
    @Override
    public void init(){
        super.init();
        SolarSystem.init();
        LoadedMod mod = mods.locateMod("ol");
        if(!headless){
            //forom Betamindy by sk7725
            Func<String, String> stringf = value -> bundle.get("mod." + value);

            mod.meta.displayName = stringf.get(mod.meta.name + ".name");
            mod.meta.description = bundle.get("mod.ol.description") +"\n\n"+ bundle.get("mod.ol.musics");
            mod.meta.author = bundle.get("mod.ol.author") + "\n\n" + bundle.get("mod.ol.contributors");
            //Random subtitles vote
            String [] r = {
                    bundle.get("mod.ol.subtitle1"),
                    bundle.get("mod.ol.subtitle2"),
                    bundle.get("mod.ol.subtitle3"),
                    bundle.get("mod.ol.subtitle4")
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
                    if (!settings.getBool("mod.ol.show", false)) {
                        new OlDisclaimer().show();
                    }
                }));
            });
            if(!mobile) {
                Events.on(ClientLoadEvent.class, e -> {
                    Table t = new Table();
                    t.margin(4f);
                    t.labelWrap("[#87ceeb]" + "Omaloon" + "[]" + "[#7f7f7f]" + " v" + mod.meta.version + "[]" + "\n" + mogus);
                    t.pack();
                    scene.add(t.visible(() -> state.isMenu()));
                });
            }
        }
    }

    void loadSettings() {
        ui.settings.addCategory("@mod.ol.omaloon-settings", "ol-settings-icon", t -> {
            t.checkPref("mod.ol.show", false);
            t.checkPref("mod.ol.check", true);
            t.fill(c -> c.bottom().right().button(Icon.discord, new ImageButton.ImageButtonStyle(), new OlDiscordLink()::show).marginTop(9f).marginLeft(10f).tooltip(bundle.get("setting.ol.discord-join")).size(84, 45).name("discord"));
        });
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
