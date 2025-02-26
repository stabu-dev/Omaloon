package omaloon;

import arc.*;
import arc.scene.actions.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.TechTree.*;
import mindustry.game.*;
import mindustry.mod.*;
import mindustry.type.*;
import ol.gen.*;
import omaloon.content.*;
import omaloon.core.*;
import omaloon.gen.*;
import omaloon.graphics.*;
import omaloon.net.*;
import omaloon.ui.*;
import omaloon.ui.dialogs.*;
import omaloon.utils.*;
import omaloon.world.blocks.environment.*;
import omaloon.world.save.*;

import static arc.Core.app;
import static omaloon.core.OlUI.*;

public class OmaloonMod extends Mod{

    /**
     * Buffer radius increase to take splashRadius into account, increase if necessary.
     */
    public static float shieldBuffer = 40f;
    public static SafeClearer safeClearer;
    public static OlUI ui;
    public static OlControl control;
    public static EditorListener editorListener;

    public OmaloonMod(){
        super();
        OlCall.registerPackets();
        new OlDelayedItemTransfer();
        if(!Vars.headless)
            editorListener = new EditorListener();
        app.addListener(ui = new OlUI(OlBinding.values()));
        app.addListener(control = new OlControl());
        Events.on(EventType.ClientLoadEvent.class, e -> {
            Vars.maps.all().removeAll(map -> {
                if(map.mod == null || !map.mod.name.equals("omaloon")){
                    return false;
                }
                Mods.LoadedMod otherMod = Vars.mods.getMod("test-utils");
                return otherMod == null || !otherMod.enabled();
            });
        });

        if(!Vars.headless){
            editorListener = new EditorListener();
        }
        Events.on(EventType.ClientLoadEvent.class, ignored -> {
            OlIcons.load();
            OlSettings.load();
            EventHints.addHints();
            CustomShapePropProcess.create();
            safeClearer = new SafeClearer();
        });

        Events.on(EventType.FileTreeInitEvent.class, e ->
            app.post(OlShaders::load)
        );

        Events.on(EventType.MusicRegisterEvent.class, e ->
            OlMusics.load()
        );

        Events.on(EventType.DisposeEvent.class, e ->
            OlShaders.dispose()
        );


        Log.info("Loaded OmaloonMod constructor.");
    }

    public static void olLog(String string, Object... args){
        Log.infoTag("omaloon", Strings.format(string, args));
    }

    public static void resetSaves(Planet planet){
        planet.sectors.each(sector -> {
            if(!sector.hasSave()) return;
            sector.save.delete();
            sector.save = null;
        });
    }

    public static void resetTree(TechNode root){
        root.reset();
        root.content.clearUnlock();
        root.children.each(OmaloonMod::resetTree);
    }

    @Override
    public void registerServerCommands(CommandHandler handler){
        OlServer.registerServerCommands(handler);
    }

    @Override
    public void registerClientCommands(CommandHandler handler){
        OlServer.registerClientCommands(handler);
    }

    @Override
    public void init(){
        super.init();
        IconLoader.loadIcons();
        if(Vars.headless) return;
        Events.on(EventType.SectorCaptureEvent.class, e -> {
            if(e.sector.preset == OlSectorPresets.deadValley) olEndDialog.show(Core.scene, Actions.sequence(
                Actions.fadeOut(0),
                Actions.fadeIn(1)
            ));
        });
    }

    @Override
    public void loadContent(){
        EntityRegistry.register();
        OlSounds.load();
        OlItems.load();
        OlStatusEffects.load();
        OlLiquids.load();
        OlUnitTypes.load();
        OlBlocks.load();
        OlWeathers.load();
        OlPlanets.load();
        OlSectorPresets.load();
        OlSchematics.load();
        OlTechTree.load();
    }
}
