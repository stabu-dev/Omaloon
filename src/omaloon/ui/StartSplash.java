package omaloon.ui;

import arc.*;
import arc.math.*;
import arc.scene.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import arc.scene.ui.Label;
import arc.scene.ui.layout.*;
import mindustry.ui.*;

import static arc.Core.scene;
import static omaloon.OmaloonMod.*;

public class StartSplash {
    private static Table omaloonIcon, cont;
    public static void build(Group group){
        group.fill(t -> {
            cont = t;
            t.touchable(() -> Touchable.enabled);
            t.setBackground(Styles.grayPanel);

            t.fill(w -> {
                omaloonIcon = w;
                w.image(Core.atlas.find("omaloon-icon")).center().expand();
                w.setTransform(true);
            });
        });
    }

    public static void show(){
        cont.visible(() -> true);

        omaloonIcon.actions(
                Actions.alpha(0f),
                Actions.delay(1f),
                Actions.fadeIn(1f, Interp.pow3Out),
                Actions.delay(1f),
                Actions.fadeOut(1f, Interp.pow3Out)
        );

        cont.actions(Actions.delay(6f, Actions.fadeOut(1f)), Actions.run(() -> {
            cont.visible(() -> false);
            cont.touchable(() -> Touchable.disabled);
        }));
    }
}
