package omaloon.core;

import arc.*;
import arc.scene.ui.*;
import arclibrary.settings.number.*;
import arclibrary.settings.other.*;
import mindustry.gen.*;
import mindustry.ui.*;
import omaloon.content.*;

import static arc.Core.*;
import static mindustry.Vars.*;
import static omaloon.utils.Constant.*;

public interface OlSettings{
    String discordURL = "https://discord.gg/bNMT82Hswb";
    //TODO compile-time check that all settings declared here, and not using as plain strings
    FloatSettingKey shieldOpacity = new FloatSettingKey("omaloon-shield-opacity", () -> 20f);
    BooleanSettingKey showDisclaimer = new BooleanSettingKey("omaloon-show-disclaimer", FALSE_PROV);
    BooleanSettingKey enableSoftCleaner = new BooleanSettingKey("omaloon-enable-soft-cleaner", TRUE_PROV);
    BooleanSettingKey checkUpdates = new BooleanSettingKey("omaloon-check-updates", TRUE_PROV);
    BooleanSettingKey displayLiquidStats = new BooleanSettingKey("omaloon-display-liquid-stats", TRUE_PROV);
    BooleanSettingKey droneAutoAIM_Always = new BooleanSettingKey("omaloon-drone-auto-aim-always", TRUE_PROV);
    //TODO implement this
    BooleanSettingKey droneAutoAIM_Build = new BooleanSettingKey("omaloon-drone-auto-aim-build", TRUE_PROV);

    static void load(){
        //add omaloon settings
        ui.settings.addCategory("@settings.omaloon", OlIcons.settings, table -> {
            table.table(Tex.button, cat -> {
                cat.button(
                    "@settings.game",
                    Icon.settings,
                    Styles.flatt,
                    iconMed,
                    () -> OlUI.olGameDialog.show()
                ).growX().marginLeft(8f).height(50f).row();
                if(!mobile || Core.settings.getBool("keyboard")){
                    cat.button(
                        "@settings.controls",
                        Icon.move,
                        Styles.flatt,
                        iconMed,
                        () -> OlUI.olInputDialog.show()
                    ).growX().marginLeft(8f).height(50f).row();
                }
                cat.button(
                    "@settings.omaloon-moddata",
                    Icon.save,
                    Styles.flatt,
                    iconMed,
                    () -> OlUI.olGameDataDialog.show()
                ).growX().marginLeft(8f).height(50f).row();
            }).width(Math.min(Core.graphics.getWidth() / 1.2f, 460.0F)).padBottom(45);
//            table.sliderPref("@setting.omaloon-shield-opacity", 20, 0, 100, s -> s + "%");
//            //checks
//            table.checkPref("@setting.omaloon-show-disclaimer", false);
//            table.checkPref(OlSettings.enableSoftCleaner, true);
//            table.checkPref("@setting.omaloon-check-updates", true);

            //discord link
            table.fill(c -> c
                .bottom()
                .right()
                .button(
                    Icon.discord,
                    new ImageButton.ImageButtonStyle(),
                    () -> {
                        if(!app.openURI(discordURL)){
                            ui.showInfoFade("@linkfail");
                            app.setClipboardText(discordURL);
                        }
                    }
                )
                .marginTop(9f)
                .marginLeft(10f)
                .tooltip(bundle.get("setting.omaloon-discord-join"))
                .size(84, 45)
                .name("discord"));
        });
    }
}