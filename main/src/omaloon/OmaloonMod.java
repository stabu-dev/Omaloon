package omaloon;

import arc.*;
import arc.util.*;
import mindustry.ctype.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;
import omaloon.annotations.Annotations.*;
import omaloon.core.*;
import omaloon.gen.*;
import omaloon.ui.*;
import omaloon.ui.dialogs.*;

import static arc.Core.app;
import static mindustry.Vars.*;

@LoadRegs("error")
@EnsureLoad
public class OmaloonMod extends Mod{
    public static boolean tools = false;
    protected static LoadedMod mod;

    public OmaloonMod(){
        this(false);
    }

    public OmaloonMod(boolean tools){
        OmaloonMod.tools = tools;

        Events.on(ClientLoadEvent.class, e -> {
            OlIcons.load();
            OlSettings.load();

            app.post(() -> {
                if(Core.scene != null && Core.scene.root != null){
                    StartSplash.build();
                    StartSplash.show();
                }else{
                    Log.err("OmaloonMod: Scene not initialized during ClientLoadEvent. StartSplash cannot be shown.");
                }
            });
        });

        if(!headless){
            Events.on(FileTreeInitEvent.class, e -> app.post(OlSounds::load));
        }

        Events.on(ContentInitEvent.class, e -> {
            if(!headless){
                Regions.load();
                content.each(content -> {
                    if(isOmaloon(content) && content instanceof MappableContent mContent){
                        OlContentRegionRegistry.load(mContent);
                    }
                });
            }
        });

        app.post(() -> mod = mods.getMod(OmaloonMod.class));
    }

    @Override
    public void init(){
    }

    @Override
    public void loadContent(){
        OlEntityMapping.init();
    }

    public static boolean isOmaloon(Content content){
        return content.minfo.mod != null && content.minfo.mod.name.equals("omaloon");
    }

    public static LoadedMod mod(){
        return mod;
    }
}