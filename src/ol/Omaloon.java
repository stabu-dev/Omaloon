package ol;

import arc.*;
import arc.func.Func;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
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
import ol.utils.Pressure;

import static arc.Core.*;
import static mindustry.Vars.*;

public class Omaloon extends MMAMod {
    public static final String MOD_PREFIX = "ol";

    public Omaloon() {
        OlVars.load();

        Events.on(FileTreeInitEvent.class, ignored -> {
            app.post(OlSounds::load);
        });

        //why Log.info if you have ModVars.modLog
        Log.info("Loaded Omaloon constructor.");

        Events.on(FileTreeInitEvent.class, ignored -> {
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

        Func<String, String> modBundleText = (str) -> {
            return "mod." + Omaloon.MOD_PREFIX + "." + str;
        };

        Func<String, String> modBundle = (str) -> {
            return bundle.get(modBundleText.get(str));
        };

        mod.meta.displayName = modBundle.get("name");
        mod.meta.description = modBundle.get("description") + "\n\n" + modBundle.get("musics");
        mod.meta.author      = modBundle.get("author")      + "\n\n" + modBundle.get("contributors");

        //Random subtitles vote
        String amogus = bundle.get(bundle.getProperties().keys().toSeq().filter(it-> {
            return it.startsWith(modBundleText.get("subtitle"));
        }).random());

        //why need add two String if you can just print in one string?
        //wrong: "a" + "b" //ab
        //right: "ab" //ab

        mod.meta.subtitle = "[#7f7f7f]v" + mod.meta.version + "[]\n" + amogus;

        //do not name variables in 1 letter (for vars i for example can be)
        //but var can have name x or y
        Events.on(ClientLoadEvent.class, ignored -> {
            loadSettings();
            OlSettings.init();
            //ALWAYS PLACE {}!!!

            app.post(() -> {
                app.post(() -> {
                    if(!settings.getBool(modBundleText.get("show"), false)) {
                        new OlDisclaimer().show();
                    }
                });
            });

            //load pressure
            app.addListener(new Pressure());
        });

        if(!mobile) {
            Events.on(ClientLoadEvent.class, ignored -> {
                Table table = new Table();
                table.margin(4f);
                table.labelWrap("[#87ceeb]Omaloon[] " + mod.meta.subtitle);
                table.pack();

                scene.add(table.visible(() -> {
                    return state.is(GameState.State.menu);
                }));
            });
        }

        if(settings.getBool(modBundleText.get("check"), false)) {
            OlUpdateCheckDialog.check();
        }
    }

    @Override
    protected void modContent(Content content){
        super.modContent(content);

        //if(content instanceof MappableContent) {
        //    OlContentRegions.loadRegions((MappableContent) content);
        //}
    }

    void loadSettings() {
        ui.settings.addCategory("@mod." + Omaloon.MOD_PREFIX + ".omaloon-settings", OlVars.fullName("settings-icon"), table -> {
            table.sliderPref("mod." + Omaloon.MOD_PREFIX + ".pressureupdate", 4, 0, 120, 2, val -> {
                if(val > 30) {
                    return val + " " + bundle.get("setting.mod.ol.pressureupdate.possible-bugs");
                }

                return val + " " + bundle.get("setting.mod.ol.pressureupdate.ticks");
            });

            table.checkPref("mod." + Omaloon.MOD_PREFIX + ".show", false);
            table.checkPref("mod." + Omaloon.MOD_PREFIX + ".check", true);

            table.fill(c -> {
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
                        .tooltip(bundle.get("setting." + Omaloon.MOD_PREFIX + ".discord-join"))
                        .size(84, 45)
                        .name("discord");
            });
        });
    }

    @Override
    public void loadContent() {
        ModVars.modLog("Loading some content.");
        super.loadContent();
    }
}