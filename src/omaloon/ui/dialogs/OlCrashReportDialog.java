package omaloon.ui.dialogs;

import arc.*;
import arc.files.*;
import arc.scene.ui.*;
import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;

/**
 * Original code from Animalia
 * Author: @Flin
 */

public class OlCrashReportDialog extends Dialog {
    private static String latest = "";

    public OlCrashReportDialog(){
        super("");

        cont.add("@dialog.omaloon.crash-report.tile", Styles.defaultLabel, 1).padLeft(4).center();
        cont.row();
        cont.image(Tex.whiteui, Pal.accent).growX().height(5).pad(5).padTop(8).width(500).align(Align.center);
        cont.row();
        cont.add("@dialog.omaloon.crash-report").width(500).wrap().pad(10).get().setAlignment(Align.center, Align.center);
        cont.row();

        cont.table(t -> {
            t.button("@back", Icon.left, this::hide).size(200, 54).pad(8).padTop(6).align(Align.center);

            t.button("@button.omaloon.report", Icon.link, () -> {

                Core.app.openURI(URL());

            }).size(200, 54).pad(8).padTop(6).align(Align.center);
        });
    }

    public void load(){
        Log.info("Checking for last crashes...");
        int length = 0;

        try {
            length = Core.settings.getDataDirectory().child("crashes").file().listFiles().length;
        } catch(Throwable ignore) {}

        if(!Core.settings.has("crashcounter")){
            Core.settings.put("crashcounter", length);
        }

        if(Core.settings.getInt("crashcounter") < length &&
                Core.settings.getBool("@omaloon.check-crashes") &&
                (latest = latestCrash()).contains("omaloon.")
        ){
            show();
            Core.settings.put("crashcounter", length);
        };
    }

    public static String URL(){
        return "https://github.com/xstabux/Omaloon/issues/new?assignees=&labels=bug&body=" +
                Strings.encode(Strings.format(
                                """
                                ###### @xstabux will try to fix issue.
                
                                ---
                                
                                **Issue**: *Describe your issue in detail. Provide screen recording video or screenshots.*
                                            
                                **Steps to reproduce**: *How you happened across the issue, and what exactly you did to make the bug happen.*
                
                                ---
                                            
                                *Place an X (no spaces) between the brackets to confirm that you have read the line below.*
                                - [ ] **I have updated to the latest release (https://github.com/xStaBUx/Omaloon-public/releases) to make sure my issue has not been fixed.**
                                - [ ] **I have searched the closed and open issues to make sure that this problem has not already been reported.**
                                
                                ###### Latest crash found:
                                ```
                                %s
                                ```
                                """
                        ).replace("%s", latest)
                );
    }

    public long parseDate(Fi fi){
        return Strings.parseLong(fi.name().replaceAll("[^0-9]", ""), 0);
    }

    public String latestCrash(){
        try {
            Seq<Fi> files = Core.settings.getDataDirectory().child("crashes").seq();

            Fi first = files.max(Structs.comparingLong(this::parseDate));

            return "Crash: " + first.toString().substring(62) + "\n\n".concat(first.readString());
        } catch(Exception e){
            return "Failed Loading Crashes: " + e;
        }
    }
}
