package ol.ui.dialogs;

import arc.files.*;
import arc.input.KeyCode;
import arc.util.*;
import arc.util.io.*;
import arc.util.serialization.*;

import java.net.*;

import mindustry.gen.Icon;
import mindustry.mod.*;
import mindustry.ui.dialogs.BaseDialog;

import mma.*;

import ol.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class OlUpdateCheckDialog {
    public static final String repo = "xStaBUx/Omaloon-public";

    public static Mods.LoadedMod mod = ModVars.modInfo;
    public static String url = ghApi + "/repos/" + repo + "/releases/latest";

    public static float progress;
    public static String download;

    public static void check() {
        OlVars.log("Checking for Omaloon updates.");

        Http.get(url, res -> {
            Jval json = Jval.read(res.getResultAsString());
            String latest = json.getString("tag_name").substring(1);
            download = json.get("assets").asArray().get(0).getString("browser_download_url");

            if(!latest.equals(mod.meta.version)) {
                BaseDialog dialog = new BaseDialog("@mod.ol.updater.tile");

                dialog.cont.add(bundle.format("mod.ol.updater", mod.meta.version, latest))
                        .width(mobile ? 400f : 500f)
                        .wrap()
                        .pad(4f)
                        .get()
                        .setAlignment(Align.center, Align.center);

                dialog.buttons
                        .defaults()
                        .size(200f, 54f)
                        .pad(2f);

                dialog.setFillParent(false);
                dialog.buttons.button(
                        "@mod.ol.updater.later",
                        Icon.refresh,
                        dialog::hide
                );

                dialog.buttons.button(
                        "@mod.ol.updater.load",
                        Icon.download,
                        OlUpdateCheckDialog::update
                );

                dialog.keyDown(KeyCode.escape, dialog::hide);
                dialog.keyDown(KeyCode.back, dialog::hide);
                dialog.show();
            }
        });
    }

    public static void update() {
        try {
            if(mod.loader instanceof URLClassLoader cl) {
                cl.close();
            }

            mod.loader = null;
        } catch (Throwable ignored) {
        }

        ui.loadfrag.show("@downloading");
        ui.loadfrag.setProgress(() -> progress);

        Http.get(download, OlUpdateCheckDialog::handle);
    }

    public static void handle(Http.HttpResponse res) {
        try {
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
        } catch (Throwable ignored) {
        }
    }
}
