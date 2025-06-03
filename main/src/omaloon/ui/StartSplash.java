package omaloon.ui;

import arc.*;
import arc.graphics.*;
import arc.math.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.Label.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.ui.*;
import omaloon.ui.dialogs.*;

import static omaloon.OmaloonMod.mod;

public class StartSplash{
    private static Table iconContainer;
    private static Table splashContainer;

    public static void build(){
        if(splashContainer != null) return;

        splashContainer = new Table();
        splashContainer.setFillParent(true);
        splashContainer.setBackground(Styles.grayPanel);
        splashContainer.visible = false;

        splashContainer.add(new Table(innerTable -> {
            iconContainer = innerTable;
            iconContainer.setTransform(true);

            iconContainer.image(Core.atlas.find("omaloon-splash-icon")).center();
            iconContainer.row();
            Label versionLabel = new Label(mod().meta.version, new LabelStyle(Fonts.tech, Color.valueOf("444444")));
            iconContainer.add(versionLabel).center().padTop(20);
        })).expand().center();

        if(Core.scene != null && Core.scene.root != null){
            Core.scene.root.addChild(splashContainer);
        }else{
            Log.warn("StartSplash: Scene or root is null during build. Splash might not attach correctly.");
        }
    }

    public static void show(){
        if(Core.scene == null || Core.scene.root == null){
            Log.warn("StartSplash: Cannot show, scene or root is null.");
            return;
        }
        if(splashContainer == null){
            build();
            if(splashContainer == null || splashContainer.parent == null){
                if(splashContainer != null && Core.scene.root != null){
                    Core.scene.root.addChild(splashContainer);
                }else{
                    Log.err("StartSplash: Failed to build or attach splash to scene.");
                    return;
                }
            }
        }

        splashContainer.clearActions();
        iconContainer.clearActions();

        splashContainer.visible = true;
        splashContainer.touchable = Touchable.enabled;
        splashContainer.toFront();

        iconContainer.color.a = 0f;

        iconContainer.actions(
        Actions.alpha(0f),
        Actions.delay(0.5f),
        Actions.fadeIn(1.0f, Interp.pow3Out),
        Actions.delay(1.5f),
        Actions.fadeOut(1.0f, Interp.pow3Out)
        );

        splashContainer.actions(
        Actions.delay(4.0f),
        Actions.fadeOut(0.5f, Interp.fastSlow),
        Actions.run(() -> {
            splashContainer.visible = false;
            splashContainer.touchable = Touchable.disabled;
            onComplete();
            splashContainer.remove();
            splashContainer = null;
        })
        );
    }

    private static void onComplete(){
        if(!Core.settings.getBool("@setting.omaloon-show-disclaimer", false)){
            new DisclaimerDialog().show();
        }


        /*if(checkUpdates.get()){
            OlUpdateCheckerDialog.check();
        }
        */
    }
}