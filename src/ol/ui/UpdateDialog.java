package ol.ui;


import arc.util.Http;
import arc.util.Http.HttpResponse;
import arc.util.io.Streams;
import arc.util.serialization.Jval;
import arc.files.Fi;
import mindustry.mod.Mods.LoadedMod;
import ol.Omaloon;

import static arc.Core.*;
import static mindustry.Vars.*;

import java.net.*;

public class UpdateDialog {

    public static final String repo = " xStaBUx/Omaloon-mod-public ";

    public static LoadedMod mod;
    public static String url;

    public static float progress;
    public static String download;

    public static void load() {
        mod = mods.getMod(Omaloon.class);
        url = ghApi + "/repos/" + repo + "/releases/latest";
    }

    public static void check() {
        Omaloon.log("Checking for updates.");
        Http.get(url, res -> {
            Jval json = Jval.read(res.getResultAsString());
            String latest = json.getString("tag_name").substring(1);
            download = json.get("assets").asArray().get(0).getString("browser_download_url");

            if (!latest.equals(mod.meta.version)) ui.showCustomConfirm(
                    "@ol.update.name", bundle.format("ol.update.info", mod.meta.version, latest),
                    "@ol.update.load", "@ok", UpdateDialog::update, () -> {});
        }, Omaloon::error);
    }

    public static void update() {
        try { // dancing with tambourines, just to remove the old mod
            if (mod.loader instanceof URLClassLoader cl) cl.close();
            mod.loader = null;
        } catch (Throwable e) { Omaloon.error(e); } // this has never happened before, but everything can be

        ui.loadfrag.show("@downloading");
        ui.loadfrag.setProgress(() -> progress);

        Http.get(download, UpdateDialog::handle, Omaloon::error);
    }

    public static void handle(HttpResponse res) {
        try {
            Fi file = tmpDirectory.child(repo.replace("/", "") + ".zip");
            Streams.copyProgress(res.getResultAsStream(), file.write(false), res.getContentLength(), 4096, p -> progress = p);

            mods.importMod(file).setRepo(repo);
            file.delete();

            app.post(ui.loadfrag::hide);
            ui.showInfoOnHidden("@mods.reloadexit", app::exit);
        } catch (Throwable e) { Omaloon.error(e); }
    }
    public static Fi script() {
        return mod.root.child("scripts").child("main.js");
    }
}