package ol.ui;

import arc.*;
import arc.func.*;
import arc.scene.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;

import mindustry.game.EventType.*;
import mindustry.ui.dialogs.*;

import java.util.*;

import static mindustry.Vars.headless;
import static mma.ModVars.modInfo;

public class ModMetaDialogFinder{
    private static Dialog currentModInfoDialog = null;
    private static boolean init = false;
    private static Cons<Dialog> onNewListener;

    public static Dialog getCurrentModInfoDialog(){
        return currentModInfoDialog;
    }

    public static void onNewListener(Cons<Dialog> onNewListener){
        init();
        ModMetaDialogFinder.onNewListener = onNewListener;
    }

    public static void init(){
        if(init) return;
        init = true;

        Events.run(Trigger.update, () -> {
            String description = modInfo.meta.description;
//                modInfo.meta.description = descriptionBuilder.toString();
            if(Core.scene != null && !headless){
                if(currentModInfoDialog != null && !currentModInfoDialog.isShown()){
                    currentModInfoDialog = null;
                }
                Dialog dialog = Core.scene.getDialog();
                if(dialog instanceof BaseDialog && Objects.equals(dialog.title.getText().toString(), modInfo.meta.displayName())){
                    if(dialog == currentModInfoDialog){
                        return;
                    }
                    if(currentModInfoDialog != null && Core.scene.root.getChildren().contains(currentModInfoDialog)){
                        return;
                    }
                    dialog.cont.getChildren().each(e -> {
                        if(e instanceof ScrollPane scrollPane){
                            Element widget = scrollPane.getWidget();
                            if(widget instanceof Table table){
                                for(Element child : table.getChildren()){
                                    if(child instanceof Label label && label.getText().toString().equals(description)){
                                        onNewListener.get(dialog);
                                        currentModInfoDialog = dialog;
                                    }
                                }
                            }
                        }
                    });
                }
            }
        });
    }
}