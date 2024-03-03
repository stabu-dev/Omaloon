package omaloon.core;

import static mindustry.Vars.*;

public class OlSettings{

    public static void load(){
        //add omaloon settings
        ui.settings.addCategory("@mod.ol.omaloon-settings", "omaloon-settings-icon", table -> {
            //checks
            table.checkPref("@setting.omaloon.show-disclaimer", false);
            table.checkPref("@setting.omaloon.check-crashes", true);
            table.checkPref("@setting.omaloon.check-updates", true);
            table.checkPref("@setting.omaloon.advanced-shadows", false);
        });
    }
}