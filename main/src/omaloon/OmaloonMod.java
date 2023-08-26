package omaloon;

import arc.*;
import arc.util.*;
import mindustry.game.*;
import mindustry.mod.*;
import omaloon.content.OlBlocks;
import omaloon.content.OlLiquids;
import omaloon.core.*;
import omaloon.ui.dialogs.*;
import static arc.Core.*;
import static mindustry.Vars.*;

public class OmaloonMod extends Mod{

    public OmaloonMod(){
        super();
        Events.on(EventType.ClientLoadEvent.class, ignored -> {
           OlSettings.load();
            app.post(() -> {
                if(!settings.getBool("@setting.omaloon.show-disclaimer")){
                    new OlDisclaimerDialog().show();
                }
            });
        });
        Log.info("Loaded OmaloonMod constructor.");
    }

    @Override
    public void init(){
        super.init();

        if(headless) return;

        if(settings.getBool("@setting.omaloon.check-updates")){
            OlUpdateCheckDialog.check();
        }

        if(settings.getBool("@setting.omaloon.check-crashes")) {
            new OlCrashReportDialog().load();
        }
    }

    @Override
    public void loadContent(){
        Log.info("Loading some Omaloon content.");
        OlLiquids.load();
        OlBlocks.load();
    }

}
