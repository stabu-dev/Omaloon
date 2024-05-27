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
import omaloon.ui.dialogs.*;
import omaloon.world.blocks.environment.*;

import static arc.Core.*;

public class OmaloonMod extends Mod{
    public static Mods.LoadedMod modInfo;

    public OmaloonMod(){
        super();
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
                app.post(OlShaders::init)
        );

        Events.on(EventType.DisposeEvent.class, e ->
                OlShaders.dispose()
        );

        Log.info("Loaded OmaloonMod constructor.");
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
    }
}
