package omaloon.core;

import arc.func.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import mindustry.gen.*;
import mindustry.ui.dialogs.*;
import mindustry.ui.dialogs.SettingsMenuDialog.SettingsTable.*;
import omaloon.ui.*;

import static arc.Core.*;
import static mindustry.Vars.ui;

/**
 * A class that adds Omaloon's settings to the settings dialog.
 * @author stabu_
 */
public class OlSettings{
    public static String discordURL = "https://discord.gg/bNMT82Hswb";

    public static void load(){
        //add omaloon settings
        ui.settings.addCategory("@settings.omaloon", OlIcons.olSettings, table -> {
            /*if(!mobile || Core.settings.getBool("keyboard")){
                table.pref(new TableSetting("category", new Table(Tex.button, cat -> {
                    cat.button(
                    "@settings.controls",
                    Icon.move,
                    Styles.flatt,
                    iconMed,
                    () -> OmaloonMod.olInputDialog.show()
                    ).growX().marginLeft(8f).height(50f).row();
                    cat.button(
                    "@settings.omaloon-moddata",
                    Icon.save,
                    Styles.flatt,
                    iconMed,
                    () -> OmaloonMod.olGameDataDialog.show()
                    ).growX().marginLeft(8f).height(50f).row();
                })));
            }*/
            table.sliderPref("omaloon-shelter-opacity", 20, 0, 100, s -> s + "%");
            //checks
            table.checkPref("omaloon-loading-screen", true);
            table.checkPref("omaloon-show-disclaimer", false);
            table.checkPref("omaloon-check-updates", false);
            table.checkPref("omaloon-override-stats", true);

            table.pref(new ButtonSetting("reset-hints", () -> {
                Button b = new Button(Tex.buttonSideLeft, Tex.buttonSideLeftDown);

                b.add("@setting.omaloon-reset-hints").margin(10);
                b.clicked(EventHints::reset);

                return b;
            }));

            // discord link
            table.pref(new TableSetting("discord-link", new Table(c -> {
                c.bottom().right().button(
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
                .tooltip(bundle.get("setting.omaloon-discord-join.name"))
                .size(84, 45)
                .name("discord");
            })));
        });
    }

    /** A setting that contains a table. */
    public static class TableSetting extends Setting{
        public Table t;

        public TableSetting(String name, Table table){
            super(name);
            this.t = table;
        }

        @Override
        public void add(SettingsMenuDialog.SettingsTable table){
            table.add(t).fillX().row();
        }
    }

    /** A setting that contains a button. */
    public static class ButtonSetting extends Setting{
        public Button button;

        public ButtonSetting(String name, Prov<Button> button){
            super(name);
            this.button = button.get();
        }

        @Override
        public void add(SettingsMenuDialog.SettingsTable table){
            table.add(button).fillX().row();
        }
    }
}