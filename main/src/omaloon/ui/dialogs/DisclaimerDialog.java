package omaloon.ui.dialogs;

import arc.*;
import arc.util.*;
import mindustry.gen.*;

/**
 * A dialog that shows a disclaimer that the user has to accept before playing with the mod.
 * @author stabu_
 */
public class DisclaimerDialog extends OlBaseDialog{
    public DisclaimerDialog(){
        super("@dialog.omaloon-disclaimer.title");

        cont.add("@dialog.omaloon-disclaimer")
        .width(500f)
        .wrap()
        .pad(4f)
        .get()
        .setAlignment(Align.center, Align.center);

        fadingButton("@ok", Icon.ok, 1.5f, this::hide);

        fadingButton("@button.omaloon-show-disclaimer", Icon.cancel, 2f, () -> {
            hide();
            Core.settings.put("omaloon-show-disclaimer", true);
        });
    }

    public static DisclaimerDialog check(){
        if(Core.settings.getBool("omaloon-show-disclaimer", false)) return null;

        DisclaimerDialog dialog = new DisclaimerDialog();
        dialog.show();
        return dialog;
    }
}
