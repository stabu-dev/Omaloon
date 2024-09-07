package omaloon;

import arc.*;
import arc.util.*;
import mindustry.*;
import mindustry.game.*;
import mindustry.mod.*;
import omaloon.content.*;
import omaloon.core.*;
import omaloon.gen.*;
import omaloon.graphics.*;
import omaloon.ui.*;
import omaloon.ui.dialogs.*;
import omaloon.ui.fragments.*;
import omaloon.world.blocks.environment.*;

import static arc.Core.*;

public class OmaloonMod extends Mod{
    public static Mods.LoadedMod modInfo;

    public static EditorListener editorListener;
    public static ShapedEnvPlacerFragment shapedEnvPlacerFragment;
    public static CliffFragment cliffFragment;

    /**
     * Buffer radius increase to take splashRadius into account, increase if necessary.
     */
    public static float shieldBuffer = 40f;

    public OmaloonMod(){
        super();

        Events.on(EventType.ClientLoadEvent.class, e -> {
            StartSplash.build(Vars.ui.menuGroup);
            StartSplash.show();
            if (!Vars.mobile && !Vars.headless) {
                shapedEnvPlacerFragment.build(Vars.ui.hudGroup);
                cliffFragment.build(Vars.ui.hudGroup);
                OlBinding.load();
            }
        });

        if(!Vars.headless) editorListener = new EditorListener();
        Events.on(EventType.ClientLoadEvent.class, ignored -> {
            OlIcons.load();
            OlSettings.load();
            EventHints.addHints();
            CustomShapePropProcess.instance = new CustomShapePropProcess();
            Vars.asyncCore.processes.add(CustomShapePropProcess.instance);
            app.post(() -> {
                if(!settings.getBool("@setting.omaloon.show-disclaimer")){
                    new OlDisclaimerDialog().show();
                }

                if(settings.getBool("@setting.omaloon.check-updates")){
                    OlUpdateCheckerDialog.check();
                }
            });
        });

        Events.on(EventType.FileTreeInitEvent.class, e ->
                app.post(OlShaders::load)
        );

        Events.on(EventType.MusicRegisterEvent.class, e -> {
            OlMusics.load();
        });

        Events.on(EventType.DisposeEvent.class, e ->
                OlShaders.dispose()
        );

        Log.info("Loaded OmaloonMod constructor.");
    }

    @Override
    public void init() {
        super.init();
        if (!Vars.headless) {
            shapedEnvPlacerFragment = new ShapedEnvPlacerFragment();
            cliffFragment = new CliffFragment();
        }
    }

    @Override
    public void loadContent(){
        EntityRegistry.register();
        OlSounds.load();
        OlItems.load();
        OlLiquids.load();
        OlStatusEffects.load();
        OlUnitTypes.load();
        OlBlocks.load();
        OlWeathers.load();
        OlPlanets.load();
        OlSectorPresets.load();
        OlTechTree.load();
    }
}
