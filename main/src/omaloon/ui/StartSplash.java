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
    private static Table collabContainer;
    private static Table iconContainer;
    private static Table splashContainer;

    public static void build(){
        if(splashContainer != null) return;

        splashContainer = new Table();
        splashContainer.setFillParent(true);
        splashContainer.setBackground(Styles.grayPanel);
        splashContainer.visible = false;

        Stack stack = new Stack();

        collabContainer = new Table(table -> {
            table.setTransform(true);
            table.image(Core.atlas.find("omaloon-splash-collab")).center();
        });
        stack.add(collabContainer);

        iconContainer = new Table(table -> {
            table.setTransform(true);
            table.image(Core.atlas.find("omaloon-splash-icon")).center();
            table.row();
            Label versionLabel = new Label(mod().meta.version, new LabelStyle(Fonts.tech, Color.valueOf("444444")));
            table.add(versionLabel).center().padTop(20);
        });
        stack.add(iconContainer);

        splashContainer.add(stack).grow();

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
            if(splashContainer == null){
                Log.err("StartSplash: Failed to build splash.");
                return;
            }
            if(splashContainer.parent == null){
                if(Core.scene.root != null){
                    Core.scene.root.addChild(splashContainer);
                }else{
                    Log.err("StartSplash: splashContainer built but Core.scene.root is null, cannot attach.");
                    return;
                }
            }
        }

        splashContainer.clearActions();
        if(collabContainer != null) collabContainer.clearActions();
        if(iconContainer != null) iconContainer.clearActions();

        splashContainer.visible = true;
        splashContainer.touchable = Touchable.enabled;
        splashContainer.toFront();

        if(collabContainer != null) collabContainer.color.a = 0f;
        if(iconContainer != null) iconContainer.color.a = 0f;

        if(collabContainer != null){
            collabContainer.actions(
            Actions.alpha(0f),
            Actions.delay(0.5f),
            Actions.fadeIn(1.0f, Interp.pow3Out),
            Actions.delay(0.7f),
            Actions.fadeOut(1.0f, Interp.pow3Out)
            );
        }

        if(iconContainer != null){
            iconContainer.actions(
            Actions.alpha(0f),
            Actions.delay(3.0f),
            Actions.fadeIn(1.0f, Interp.pow3Out),
            Actions.delay(0.7f),
            Actions.fadeOut(1.0f, Interp.pow3Out)
            );
        }

        float totalImageSequenceDuration = 6.25f;

        splashContainer.actions(
        Actions.delay(totalImageSequenceDuration),
        Actions.fadeOut(0.5f, Interp.fastSlow),
        Actions.run(() -> {
            splashContainer.visible = false;
            splashContainer.touchable = Touchable.disabled;
            onComplete();
            if(splashContainer != null){
                splashContainer.remove();
            }
            splashContainer = null;
            collabContainer = null;
            iconContainer = null;
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