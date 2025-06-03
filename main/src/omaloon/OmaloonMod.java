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

import static arc.Core.app;
import static mindustry.Vars.*;

/**
 * The Omaloon's main class. Contains static references to other modules.
 * @author stabu_
 */
@LoadRegs("error")// Need this temporarily, so the class gets generated.
@EnsureLoad
public class OmaloonMod extends Mod{
    public static boolean tools = false;
    protected static LoadedMod mod;

    public OmaloonMod(){
        this(false);
    }

    /**
     * Constructs the Omaloon and binds some functionality to the game under certain circumstances.
     * @param tools Whether the Omaloon is in an asset-processing context.
     */
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
        //below has to be done after all things are loaded.
        OlEntityMapping.init();
    }

    public static boolean isOmaloon(Content content){
        return content.minfo.mod != null && content.minfo.mod.name.equals("omaloon");
    }

    public static LoadedMod mod(){
        return mod;
    }
}