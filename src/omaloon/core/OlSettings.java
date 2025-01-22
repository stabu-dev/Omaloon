package omaloon.core;

import arc.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.*;
import omaloon.*;
import omaloon.content.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class OlSettings{
    public static String discordURL = "https://discord.gg/bNMT82Hswb";
    public static void load(){
        //add omaloon settings
        ui.settings.addCategory("@settings.omaloon", OlIcons.settings, table -> {
            table.table(Tex.button, cat -> {
	              if(!mobile || Core.settings.getBool("keyboard")) {
                    cat.button(
                      "@settings.controls",
                      Icon.move,
                      Styles.flatt,
                      iconMed,
                      () -> OmaloonMod.olInputDialog.show()
                    ).growX().marginLeft(8f).height(50f).row();
	              }
                cat.button(
                  "@settings.game",
                  Icon.settings,
                  Styles.flatt,
                  iconMed,
                  () -> OmaloonMod.olGameDialog.show()
                ).growX().marginLeft(8f).height(50f).row();
                cat.button(
                  "@settings.omaloon-moddata",
                  Icon.save,
                  Styles.flatt,
                  iconMed,
                  () -> OmaloonMod.olGameDataDialog.show()
                ).growX().marginLeft(8f).height(50f).row();
            }).width(Math.min(Core.graphics.getWidth() / 1.2f, 460.0F)).padBottom(45);
//            table.sliderPref("@setting.omaloon-shield-opacity", 20, 0, 100, s -> s + "%");
//            //checks
//            table.checkPref("@setting.omaloon-show-disclaimer", false);
//            table.checkPref("omaloon-enable-soft-cleaner", true);
//            table.checkPref("@setting.omaloon-check-updates", true);

            //discord link
            table.fill(c -> c
                    .bottom()
                    .right()
                    .button(
                            Icon.discord,
                            new ImageButton.ImageButtonStyle(),
                            () -> {
                                if(!app.openURI(discordURL)) {
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

    public static class TableSetting extends Setting {
        public Table t;

        public TableSetting(String name, Table table) {
            super(name);
            t = table;
        }

        @Override
        public void add(SettingsMenuDialog.SettingsTable table) {
            addDesc(table.add(t).growX().get());
            table.row();
        }
    }
}