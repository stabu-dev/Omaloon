package ol;

import arc.*;
import arc.audio.*;
import arc.scene.ui.layout.*;

import mindustry.core.*;
import mindustry.ctype.*;
import mindustry.game.*;
import mindustry.game.EventType.*;
import mindustry.mod.Mods.*;

import mma.*;
import mma.utils.*;

import ol.core.*;
import ol.gen.*;
import ol.graphics.*;
import ol.logic.*;
import ol.ui.ModMetaDialogFinder;
import ol.ui.dialogs.*;
import ol.utils.*;
import ol.utils.pressure.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class Omaloon extends MMAMod{

    public Omaloon(){
        OlVars.load();

        OlGroups.init();
        Events.on(ResetEvent.class, e -> {
            OlGroups.clear();
        });

        Events.on(EventType.ClientLoadEvent.class, ignored -> {
            OlSettings.init();
            OlSettings.loadCategory();

            app.post(() -> {

                if(!SettingsManager.show.get()){
                    new OlDisclaimer().show();
                }
            });

        });
        ModVars.modLog("Loaded Omaloon constructor.");
    }

    @Override
    public void init(){
        super.init();
        ManyPlanetSystems.init();

        //load pressure
        app.addListener(new PressureUpdater());
        PressureUpdater.load();

        //map events
        OlMapInvoker.load();


        if(headless) return;

        LoadedMod mod = ModVars.modInfo;

        //change mod info
        mod.meta.displayName = bundle.get("mod." + mod.meta.name + ".name");
        mod.meta.description = bundle.get("mod.ol.description") + "\n\n" + bundle.get("mod.ol.musics");
        mod.meta.author = bundle.get("mod.ol.author") + "\n\n" + bundle.get("mod.ol.contributors");

        //random subtitles vote
        String subtitle =
            bundle.get(
                bundle.getProperties()
                    .keys().toSeq()
                    .filter(it -> it.startsWith("mod.ol.subtitle"))
                    .random()
            );

        mod.meta.subtitle = "[#7f7f7f]v" + mod.meta.version + "[]\n" + subtitle;


        //if not mobile create text in menu
        if(!mobile){
            Events.on(EventType.ClientLoadEvent.class, ignored -> {
                Table table = new Table();

                //setup table
                table.margin(4f);
                table.labelWrap("[#87ceeb]Omaloon[] " + mod.meta.subtitle);
                table.pack();

                scene.add(table.visible(() -> state.is(GameState.State.menu)));

            });
        }

        if(!headless){
            ModMetaDialogFinder.onNewListener(d -> {
                if(d instanceof OlModDialog) return;
                d.hide(null);
                new OlModDialog().show();
            });
        }

        //check updates if enabled
        if(SettingsManager.check.get()){
            OlUpdateCheckDialog.check();
        }
    }

    @Override
    protected void modContent(Content content){
        super.modContent(content);

        if(content instanceof MappableContent){
            OlContentRegions.loadRegions((MappableContent)content);
        }
    }


    @Override
    public void loadContent(){
        ModVars.modInfo = mods.getMod(getClass());
        ModVars.modLog("Loading some content.");
        if(!headless){//FileTreeInitEvent invokes before this method
            new Sound(ModVars.modInfo.root.child("sounds").child("boiler.ogg"));
            OlVars.inTry(OlSounds::loadNow);
            OlVars.inTry(OlMusics::loadNow);
            OlVars.inTry(OlShaders::load);
        }
        OlCacheLayer.init();


        super.loadContent();

        //logic
        OlLogicIO.load();
    }
}