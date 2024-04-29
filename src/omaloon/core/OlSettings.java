package omaloon.core;

import arc.scene.ui.*;
import mindustry.gen.*;
import omaloon.content.*;
import omaloon.ui.dialogs.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class OlSettings{

    public static void load(){
        //add omaloon settings
        ui.settings.addCategory("@mod.ol.omaloon-settings", OlIcons.settings, table -> {
            //checks
            table.checkPref("@setting.omaloon.show-disclaimer", false);
            table.checkPref("@setting.omaloon.check-updates", true);

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
                    .tooltip(bundle.get("@setting.omaloon.discord-join"))
                    .size(84, 45)
                    .name("discord"));
        });
    }
}