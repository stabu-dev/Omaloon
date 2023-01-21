package ol.core;

import ol.*;

import static arc.Core.bundle;
import static mindustry.Vars.ui;

public class OlSettings{
    public static void init(){

        boolean tmp = SettingsManager.uiRechallenged.get();

        SettingsManager.uiRechallenged.set(false);
        SettingsManager.uiRechallenged.set(tmp);
    }

    public static void loadCategory(){
        //add omaloon settings
        ui.settings.addCategory("@mod.ol.omaloon-settings", OlVars.fullName("settings-icon"), table -> {

            //checks

            table.checkPref(SettingsManager.show.key, SettingsManager.show.def());
            table.checkPref(SettingsManager.check.key, SettingsManager.check.def());

            //pressure update slider
            table.sliderPref(SettingsManager.pressureUpdate.key, SettingsManager.pressureUpdate.def(), 0, 50, 2, val -> {
                //if val > 30 bugs appear
                if(val > 30) return val + " " + bundle.get("setting.mod.ol.pressureupdate.possible-bugs");
                //just print val
                return val + " " + bundle.get("setting.mod.ol.pressureupdate.ticks");

            });
            table.checkPref(SettingsManager.debug.key,SettingsManager.debug.def());
        });
    }
}
