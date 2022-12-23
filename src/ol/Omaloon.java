package ol;

import arc.Events;
import arc.scene.ui.ImageButton;
import arc.scene.ui.layout.Table;
import arc.util.Log;

import mindustry.core.GameState;
import mindustry.ctype.Content;
import mindustry.game.EventType;
import mindustry.gen.Icon;
import mindustry.mod.Mods;

import mma.MMAMod;
import mma.ModVars;
import mma.utils.ManyPlanetSystems;

import ol.content.OlCategory;
import ol.content.OlSounds;
import ol.graphics.OlShaders;
import ol.logic.OlLogicIO;

import ol.ui.OlSettings;
import ol.ui.dialogs.OlDisclaimer;
import ol.ui.dialogs.OlDiscordLink;
import ol.ui.dialogs.OlUpdateCheckDialog;

import ol.utils.OlBundle;
import ol.utils.pressure.PressureIndicator;
import ol.utils.pressure.PressureRenderer;

import static arc.Core.*;
import static mindustry.Vars.*;

public class Omaloon extends MMAMod {
    public static final String MOD_PREFIX = "ol";

    public Omaloon() {
        OlVars.load();

        //sound / shaders
        Events.on(EventType.FileTreeInitEvent.class, ignored -> {
            app.post(OlSounds::load);
            app.post(OlShaders::load);
        });

        //why Log.info if you have ModVars.modLog
        Log.info("Loaded Omaloon constructor.");
    }

    @Override
    public void init() {
        super.init();

        //cringe code but ok
        ManyPlanetSystems.init();

        if(headless) {
            return;
        }

        //if not headless when load for pc / mobile
        Mods.LoadedMod mod = ModVars.modInfo;

        //change mod info
        mod.meta.displayName = OlBundle.get("name");
        mod.meta.description = OlBundle.get("description") + "\n\n" + OlBundle.get("musics");
        mod.meta.author      = OlBundle.get("author")      + "\n\n" + OlBundle.get("contributors");

        //Random subtitles vote
        String amogus = bundle.get(bundle.getProperties().keys().toSeq().filter(it-> {
            return it.startsWith(OlBundle._mod_ol_id_220358("subtitle"));
        }).random());

        //why need add two String if you can just print in one string?
        //wrong: "a" + "b" //ab
        //right: "ab" //ab

        mod.meta.subtitle = "[#7f7f7f]v" + mod.meta.version + "[]\n" + amogus;

        //do not name variables in 1 letter (for vars i for example can be)
        //but var can have name x or y
        Events.on(EventType.ClientLoadEvent.class, ignored -> {
            loadSettings();
            OlSettings.init();
            //ALWAYS PLACE {}!!!

            app.post(() -> {
                //2 app posts don`t make something right?
                if(!settings.getBool(OlBundle._mod_ol_id_220358("show"), false)) {
                    new OlDisclaimer().show();
                }
            });

            //load pressure
            app.addListener(new PressureRenderer());
            PressureIndicator.load();
            PressureRenderer.load();

            //load categories
            OlCategory.loadCategories();
            OlCategory.loadUI();
        });

        //if not mobile create text in menu
        if(!mobile) {
            Events.on(EventType.ClientLoadEvent.class, ignored -> {
                Table table = new Table();

                //setup table
                table.margin(4f);
                table.labelWrap("[#87ceeb]Omaloon[] " + mod.meta.subtitle);
                table.pack();

                scene.add(table.visible(() -> {
                    return state.is(GameState.State.menu);
                }));
            });
        }

        //check updates if enabled
        if(settings.getBool(OlBundle._mod_ol_id_220358("check"), false)) {
            OlUpdateCheckDialog.check();
        }
    }

    @Override
    protected void modContent(Content content) {
        super.modContent(content);

        //if(content instanceof MappableContent) {
        //    OlContentRegions.loadRegions((MappableContent) content);
        //}
    }

    void loadSettings() {
        //add omaloon settings
        ui.settings.addCategory("@" + OlBundle._mod_ol_id_220358("omaloon-settings"), OlVars.fullName("settings-icon"), table -> {
            //pressure update slider
            table.sliderPref(OlBundle._mod_ol_id_220358("pressureupdate"), 4, 0, 120, 2, val -> {
                if(val > 30) {
                    //if val > 30 when pressure will render not fast and can call bugs
                    return val + " " + OlBundle.getSetting("pressureupdate.possible-bugs");
                }

                //just print val
                return val + " " +  OlBundle.getSetting("pressureupdate.ticks");
            });

            //checks
            table.checkPref(OlBundle._mod_ol_id_220358("show"), false);
            table.checkPref(OlBundle._mod_ol_id_220358("check"), true);
            table.checkPref(OlBundle._mod_ol_id_220358("content-sort"), true);

            //discord link
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
                        .tooltip(OlBundle.getSetting("discord-join"))
                        .size(84, 45)
                        .name("discord");
            });
        });
    }

    @Override
    public void loadContent() {
        ModVars.modLog("Loading some content.");
        super.loadContent();
        
        //logic
        OlLogicIO.load();
    }
}