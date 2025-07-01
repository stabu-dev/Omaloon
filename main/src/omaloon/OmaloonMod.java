package omaloon;

import arc.*;
import mindustry.ctype.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.mod.Mods.*;
import omaloon.annotations.Annotations.*;
import omaloon.content.*;
import omaloon.core.*;
import omaloon.gen.*;
import omaloon.graphics.*;
import omaloon.ui.*;
import omaloon.ui.dialogs.*;
import omaloon.world.blocks.environment.customsshapeproop.*;

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
            CustomShapePropProcess.create();

            DisclaimerDialog.check();
            UpdateDialog.check();
        });

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

        Events.on(FileTreeInitEvent.class, e -> {
            app.post(() -> {
                mod = mods.getMod(OmaloonMod.class);

                if(!headless){
                    SplashDrawer.add(mod);
                }
            });
            app.post(OlShaders::load);
        });

        Events.on(DisposeEvent.class, e -> {
            OlShaders.dispose();
        });
    }

    @Override
    public void init(){
    }

    public static boolean isOmaloon(Content content){
        return content.minfo.mod != null && content.minfo.mod.name.equals("omaloon");
    }

    @Override
    public void loadContent(){
        OlSounds.load();
        OlItems.load();
        OlLiquids.load();
        OlStatusEffects.load();
        OlBlocks.load();

        OlEntityMapping.init();
    }

    public static LoadedMod mod(){
        return mod;
    }
}