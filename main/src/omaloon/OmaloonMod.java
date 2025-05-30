package omaloon;

import arc.*;

import mindustry.ctype.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;

import omaloon.annotations.Annotations.*;
import omaloon.gen.*;

import static mindustry.Vars.*;

/**
 * The Omaloon's main class. Contains static references to other modules.
 * @author stabu_
 */
@LoadRegs("error")// Need this temporarily, so the class gets generated.
@EnsureLoad
public class OmaloonMod extends Mod{
    public static boolean tools = false;

    /** Default constructor for Mindustry mod loader to instantiate. */
    public OmaloonMod(){
        this(false);
    }

    /**
     * Constructs the Omaloon and binds some functionality to the game under certain circumstances.
     * @param tools Whether the Omaloon is in an asset-processing context.
     */
    public OmaloonMod(boolean tools){
        OmaloonMod.tools = tools;

        if(!headless){
            Events.on(FileTreeInitEvent.class, e -> Core.app.post(OlSounds::load));

            Events.on(ClientLoadEvent.class, e -> {
                //upon startup
            });
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
}