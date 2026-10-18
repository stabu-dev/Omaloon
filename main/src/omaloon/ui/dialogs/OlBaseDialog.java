package omaloon.ui.dialogs;

import arc.*;
import arc.scene.actions.*;
import arc.scene.style.*;
import arc.scene.ui.*;
import mindustry.ui.dialogs.*;

public class OlBaseDialog extends BaseDialog{
    public OlBaseDialog(String title){
        super(title, Core.scene.getStyle(DialogStyle.class));

        buttons.defaults().size(200f, 54f).pad(2f);
        setFillParent(false);
    }

    protected void fadingButton(String text, Drawable icon, float delay, Runnable clicked){
        TextButton b = buttons.button(text, icon, clicked).get();

        b.setDisabled(() -> b.color.a < 1);

        b.actions(
        Actions.alpha(0),
        Actions.delay(delay),
        Actions.fadeIn(1f),
        Actions.delay(1f)
        );

        b.getStyle().disabledFontColor = b.getStyle().fontColor;
        b.getStyle().disabled = b.getStyle().up;
    }
}
