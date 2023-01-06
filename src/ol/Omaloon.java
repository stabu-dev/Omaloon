package ol;

import arc.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;

import mindustry.core.*;
import mindustry.ctype.*;
import mindustry.game.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.mod.Mods.*;

import mma.*;
import mma.utils.*;

import ol.content.*;
import ol.gen.*;
import ol.graphics.*;
import ol.logic.*;
import ol.ui.*;
import ol.ui.dialogs.*;
import ol.utils.OlMapInvoker;
import ol.utils.pressure.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class Omaloon extends MMAMod {

    public Omaloon() {
        OlVars.load();

        OlGroups.init();
        Events.on(ResetEvent.class, e -> {
            OlGroups.clear();
        });
        //sound / shaders
        Events.on(EventType.FileTreeInitEvent.class, ignored -> {
            app.post(OlSounds::load);
            app.post(OlShaders::load);
        });

        ModVars.modLog("Loaded Omaloon constructor.");
    }

    @Override
    public void init() {
        super.init();
        ManyPlanetSystems.init();

        if(headless) return;

        LoadedMod mod = ModVars.modInfo;

        //change mod info
        mod.meta.displayName = bundle.get("mod." + mod.meta.name+".name");
        mod.meta.description = bundle.get("mod.ol.description") + "\n\n" + bundle.get("mod.ol.musics");
        mod.meta.author = bundle.get("mod.ol.author") + "\n\n" + bundle.get("mod.ol.contributors");

        //random subtitles vote
        String subtitle =
                bundle.get(
                        bundle.getProperties()
                        .keys().toSeq()
                        .filter(it->it.startsWith("mod.ol.subtitle"))
                        .random()
                );

        mod.meta.subtitle = "[#7f7f7f]v" + mod.meta.version + "[]\n" + subtitle;

        Events.on(EventType.ClientLoadEvent.class, ignored -> {
            loadSettings();
            OlSettings.init();

            app.post(() -> {
                if(!settings.getBool("mod.ol.show", false)) {
                    new OlDisclaimer().show();
                }
            });

            //load pressure
            app.addListener(new PressureRenderer());
            PressureRenderer.load();

            //map events
            OlMapInvoker.load();
        });

        //if not mobile create text in menu
        if(!mobile) {
            Events.on(EventType.ClientLoadEvent.class, ignored -> {
                Table table = new Table();

                //setup table
                table.margin(4f);
                table.labelWrap("[#87ceeb]Omaloon[] " + mod.meta.subtitle);
                table.pack();

                scene.add(table.visible(() -> state.is(GameState.State.menu)));

            });
        }

        //check updates if enabled
        if(settings.getBool("mod.ol.check", false)) {
            OlUpdateCheckDialog.check();
        }
    }

    @Override
    protected void modContent(Content content) {
        super.modContent(content);

        if(content instanceof MappableContent) {
            OlContentRegions.loadRegions((MappableContent) content);
        }
    }

    void loadSettings() {
        //add omaloon settings
        ui.settings.addCategory("@mod.ol.omaloon-settings", OlVars.fullName("settings-icon"), table -> {

            //checks
            table.checkPref("mod.ol.show", false);
            table.checkPref("mod.ol.check", true);

            //pressure update slider
            table.sliderPref("mod.ol.pressureupdate", 0, 0, 50, 2, val -> {
                //if val > 30 bugs appear
                if(val > 30) return val + " " + bundle.get("setting.mod.ol.pressureupdate.possible-bugs");
                //just print val
                return val + " " +  bundle.get("setting.mod.ol.pressureupdate.ticks");

            });

            //discord link
            table.fill(c -> c
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
                    .name("discord"));
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