package ol.core;

import arc.scene.ui.*;
import mindustry.gen.*;
import ol.*;
import ol.ui.dialogs.*;
import zelaux.arclib.settings.*;

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

            //discord link
            table.fill(c -> {
                c.bottom();
                c.right();
                c.button(Icon.discord, new ImageButton.ImageButtonStyle(), new OlDiscordLink()::show)
                    .marginTop(9f)
                    .marginLeft(10f)
                    .tooltip(bundle.get("setting.ol.discord-join"))
                    .size(84, 45)
                    .name("discord");
            });
        });
    }
}
