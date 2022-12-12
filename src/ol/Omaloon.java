package ol;

import arc.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.Vars;
import mindustry.core.GameState;
import mindustry.ctype.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.Mods.*;
import mma.*;
import mma.utils.*;
import ol.content.*;
import ol.graphics.*;
import ol.ui.*;
import ol.ui.dialogs.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class Omaloon extends MMAMod {
    public Omaloon(){
        OlVars.load();

        Events.on(FileTreeInitEvent.class, e -> {
            app.post(OlSounds::load);
        });

        Log.info("Loaded Omaloon constructor.");

        Events.on(FileTreeInitEvent.class, e -> {
            Core.app.post(OlShaders::load);
        });
    }

    @Override
    public void init() {
        super.init();

        ManyPlanetSystems.init();
        LoadedMod mod = ModVars.modInfo;

        if(headless) {
            return;
        }

        mod.meta.displayName = bundle.get("mod." + mod.meta.name + ".name");
        mod.meta.description = bundle.get("mod.ol.description") + "\n\n" + bundle.get("mod.ol.musics");
        mod.meta.author = bundle.get("mod.ol.author") + "\n\n" + bundle.get("mod.ol.contributors");

        //Random subtitles vote
        String mogus = bundle.get(bundle.getProperties().keys().toSeq().filter(it-> {
            return it.startsWith("mod.ol.subtitle");
        }).random());

        mod.meta.subtitle = "[#7f7f7f]" + "v" + mod.meta.version + "[]" + "\n" + mogus;

        Events.on(ClientLoadEvent.class, e -> {
            loadSettings();
            OlSettings.init();

            app.post(() -> app.post(() -> {
                if(!settings.getBool("mod.ol.show", false)){
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

                scene.add(t.visible(() -> {
                    return state.is(GameState.State.menu);
                }));
            });
        }

        if(settings.getBool("mod.ol.check", false)) {
            OlUpdateCheckDialog.check();
        }
    }

    @Override
    protected void modContent(Content content){
        super.modContent(content);

        /*if(content instanceof MappableContent){
            OlContentRegions.loadRegions((MappableContent)content);
        }*/
    }

    void loadSettings(){
        ui.settings.addCategory("@mod.ol.omaloon-settings", OlVars.fullName("settings-icon"), t -> {
            t.checkPref("mod.ol.show", false);
            t.checkPref("mod.ol.check", true);

            t.fill(c -> {
                c
                        .bottom()
                        .right()
                        .button(
                                Icon.discord,
                                new ImageButton.ImageButtonStyle(),
                                new OlDiscordLink()::show
                        )
                        .marginTop(9f)
                        .marginLeft(10f)
                        .tooltip(bundle.get("setting.ol.discord-join"))
                        .size(84, 45)
                        .name("discord");
            });
        });
    }

    @Override
    public void loadContent(){
        ModVars.modLog("Loading some content.");
        super.loadContent();
    }
}