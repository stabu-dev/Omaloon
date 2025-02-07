package omaloon.ui.dialogs;

import arc.*;
import arc.files.*;
import arc.input.*;
import arc.util.*;
import arc.util.io.*;
import arc.util.serialization.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;

import java.net.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class OlUpdateCheckerDialog{
    public static final String repo = "xstabux/Omaloon";

    public static Mods.LoadedMod mod = Vars.mods.locateMod("omaloon");
    public static String url = ghApi + "/repos/" + repo + "/releases/latest";
    public static String changes = "https://github.com/xstabux/Omaloon/releases/latest";

    public static float progress;
    public static String download;

    public static void check(){
        Log.info("Checking for Omaloon updates...");

        Http.get(url, res -> {
            Jval json = Jval.read(res.getResultAsString());
            String latest = json.getString("tag_name").substring(1);
            download = json.get("assets").asArray().get(0).getString("browser_download_url");

            if(!latest.equals(mod.meta.version)){
                BaseDialog dialog = new BaseDialog("@dialog.omaloon-updater.tile");

                dialog.cont.add(bundle.format("dialog.omaloon-updater", mod.meta.version, latest))
                           .width(mobile ? 400f : 500f)
                           .wrap()
                           .pad(4f)
                           .get()
                           .setAlignment(Align.center, Align.center);

                dialog.buttons.defaults().size(200f, 54f).pad(2f);

                dialog.setFillParent(false);
                dialog.buttons.button("@button.omaloon-ignore", Icon.cancel, dialog::hide);

                dialog.buttons.button("@button.omaloon-updater-show-changes", Icon.link, () -> {
                    if(!Core.app.openURI(changes)){
                        ui.showInfoFade("@linkfail");
                        Core.app.setClipboardText(changes);
                    }
                });

                dialog.buttons.button("@button.omaloon-install-update", Icon.download, OlUpdateCheckerDialog::update);

                dialog.keyDown(KeyCode.escape, dialog::hide);
                dialog.keyDown(KeyCode.back, dialog::hide);
                Core.app.post(dialog::show);
            }else{
                Log.info("Omaloon has latest version");
            }
        });
    }

    public static void update(){
        try{
            if(mod.loader instanceof URLClassLoader cl){
                cl.close();
            }

            mod.loader = null;
        }catch(Throwable ignored){
        }

        ui.loadfrag.show("@downloading");
        ui.loadfrag.setProgress(() -> progress);

        Http.get(download, OlUpdateCheckerDialog::handle);
    }

    public static void handle(Http.HttpResponse res){
        try{
            Fi file = tmpDirectory.child(repo.replace("/", "") + ".zip");
            Streams.copyProgress(
            res.getResultAsStream(),
            file.write(false),
            res.getContentLength(),
            4096,
            p -> progress = p
            );

            mods.importMod(file).setRepo(repo);
            file.delete();

            app.post(ui.loadfrag::hide);
            ui.showInfoOnHidden("@mods.reloadexit", app::exit);
        }catch(Throwable ignored){
        }
    }
}
