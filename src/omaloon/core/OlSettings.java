package omaloon.core;

import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import mindustry.gen.*;
import mindustry.ui.dialogs.*;
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.*;
import omaloon.content.*;
import omaloon.ui.dialogs.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class OlSettings{
    public static void load(){
        //add omaloon settings
        ui.settings.addCategory("@settings.omaloon", OlIcons.settings, table -> {
//            if(!mobile || Core.settings.getBool("keyboard")) {
//                table.pref(new TableSetting("category", new Table(Tex.button, cat -> {
//                    cat.button("@settings.controls", Icon.move, Styles.flatt, iconMed, ui.controls::show).growX().marginLeft(8f).height(50f).row();
//                })));
//            }
            table.sliderPref("@setting.omaloon-shield-opacity", 20, 0, 100, s -> s + "%");
            //checks
            table.checkPref("@setting.omaloon-show-disclaimer", false);
            table.checkPref("@setting.omaloon-check-updates", true);

            //discord link
            table.fill(c -> c
                    .bottom()
                    .right()
                    .button(
                            Icon.discord,
                            new ImageButton.ImageButtonStyle(),
                            new OlDiscordLink()::show
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