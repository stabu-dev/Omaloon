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

/**
 * A class that handles checking for updates and showing a dialog to the user.
 * 
 * @author stabu_
 */
public class UpdateDialog{
    public static final String repo = "stabu-dev/Omaloon";

    public static Mods.LoadedMod mod = Vars.mods.locateMod("omaloon");
    public static String url = ghApi + "/repos/" + repo + "/releases/latest";
    public static String changes = "https://github.com/stabu-dev/Omaloon/releases/latest";

    public static float progress;
    public static String download;

    /** Checks for updates and shows a dialog if a new version is available. */
    public static void check(){
        if(!Core.settings.getBool("omaloon-check-updates")) return;
        Http.get(url, res -> {
            Jval json = Jval.read(res.getResultAsString());
            String latest = json.getString("tag_name").substring(1);
            download = json.get("assets").asArray().get(0).getString("browser_download_url");

            if(!latest.equals(mod.meta.version)){
                BaseDialog dialog = new BaseDialog("@dialog.omaloon-updater.title");

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

                dialog.buttons.button("@button.omaloon-install-update", Icon.download, UpdateDialog::update);

                dialog.keyDown(KeyCode.escape, dialog::hide);
                dialog.keyDown(KeyCode.back, dialog::hide);
                Core.app.post(dialog::show);
            }
        });
    }

    /** Closes the old mod jar and starts the download of the new version. */
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

        Http.get(download, UpdateDialog::handle);
    }

    /**
     * Handles the downloaded mod file.
     * 
     * @param res The HTTP response containing the downloaded file.
     */
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