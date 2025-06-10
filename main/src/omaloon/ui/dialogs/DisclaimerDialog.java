package omaloon.ui.dialogs;

import arc.*;
import arc.scene.actions.*;
import arc.scene.ui.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.ui.dialogs.*;

/**
 * A dialog that shows a disclaimer that the user has to accept before playing with the mod.
 * @author stabu_
 */
public class DisclaimerDialog extends BaseDialog{
    /** Creates the disclaimer dialog. */
    public DisclaimerDialog(){
        super("@dialog.omaloon-disclaimer.title", Core.scene.getStyle(DialogStyle.class));

        cont.add("@dialog.omaloon-disclaimer")
        .width(500f)
        .wrap()
        .pad(4f)
        .get()
        .setAlignment(Align.center, Align.center);

        buttons.defaults().size(200f, 54f).pad(2f);
        setFillParent(false);

        TextButton b = buttons.button("@ok", Icon.ok, this::hide).get();

        b.setDisabled(() -> b.color.a < 1);

        b.actions(
        Actions.alpha(0),
        Actions.moveBy(0f, 0f),
        Actions.delay(1.5f),
        Actions.fadeIn(1f),
        Actions.delay(1f)
        );

        b.getStyle().disabledFontColor = b.getStyle().fontColor;
        b.getStyle().disabled = b.getStyle().up;

        TextButton s = buttons.button("@button.omaloon-show-disclaimer", Icon.cancel, () -> {
            hide();
            Core.settings.put("omaloon-show-disclaimer", true);
        }).get();

        s.setDisabled(() -> s.color.a < 1);

        s.actions(
        Actions.alpha(0),
        Actions.moveBy(0f, 0f),
        Actions.delay(2f),
        Actions.fadeIn(1f),
        Actions.delay(1f)
        );

        s.getStyle().disabledFontColor = b.getStyle().fontColor;
        s.getStyle().disabled = s.getStyle().up;
    }

    public static void check(){
        if(!Core.settings.getBool("omaloon-show-disclaimer", false)){
            new DisclaimerDialog().show();
        }
    }
}