package omaloon.ui.dialogs;

import arc.*;
import arc.scene.ui.*;
import arc.util.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;
import omaloon.content.*;
import omaloon.core.*;
import omaloon.core.settings.*;

import static arc.Core.*;

public class AfterUpdateCleanDialog{
    public static Mods.LoadedMod mod = Vars.mods.locateMod("omaloon");

    public static void check(){
        String lastVersion = settings.getString("last-omaloon-version", null);

        if(lastVersion == null){
            settings.put("last-omaloon-version", mod.meta.version);
        }else if(!mod.meta.version.equals(lastVersion)){
            showUpdateDialog();
        }
    }

    private static void showUpdateDialog(){
        BaseDialog dialog = new BaseDialog("@dialog.omaloon-update-cleanup.title", Core.scene.getStyle(Dialog.DialogStyle.class));

        dialog.cont.add(bundle.format("dialog.omaloon-update-cleanup.text", settings.getString("last-omaloon-version"), mod.meta.version))
                   .width(500f)
                   .wrap()
                   .pad(4f)
                   .get()
                   .setAlignment(Align.center, Align.center);

        dialog.buttons.defaults().size(200f, 54f).pad(2f);
        dialog.setFillParent(false);

        dialog.buttons.button("@button.omaloon-ignore", Icon.cancel, dialog::hide);

        dialog.buttons.button("@button.omaloon-update-cleanup.softclean", Icon.admin, () -> {
            OlSettings.enableSoftCleaner.set(true);
            settings.put("last-omaloon-version", mod.meta.version);
            dialog.hide();
        });

        dialog.buttons.button("@button.omaloon-update-cleanup.fullclean", Icon.trash, () -> {
            Vars.ui.showConfirm("@omaloon.update.dialog.fullclean.confirm", () -> {
                DataBase.resetTree(OlPlanets.glasmore.techTree);
                DataBase.resetSaves(OlPlanets.glasmore);
                EventHints.reset();
                settings.put("last-omaloon-version", mod.meta.version);
                dialog.hide();
            });
        });

        dialog.show();
    }
}
