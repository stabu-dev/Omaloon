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
import omaloon.editor.*;
import omaloon.world.blocks.environment.customsshapeproop.*;
import omaloon.world.patterns.*;

import static arc.Core.*;
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

        OlRenderer.init();

        Events.on(WorldLoadEvent.class, e -> {
            PatternManager.rebuild();
        });

        Events.on(ClientLoadEvent.class, e -> {
            PatternManager.register();
            EventHints.addHints();
            OlIconLoader.loadIcons();
            OlSettings.load();
            CustomShapePropProcess.create();

            OlLiquids.init();

            DisclaimerDialog.check();

            if (!headless) {
                OlEditorExtension.init();
            }

            LoadedMod contextMod = mods.getMod("context");
            if (contextMod != null && contextMod.enabled() && settings.getBool("omaloon-developer-mode", false)) {
                mods.getScripts().runConsole("ContextMod.loadToConsole(Vars.mods.getMod(\"omaloon\"), \"omaloon\")");
            }
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
            if(!headless) app.post(OlShaders::load);
        });

        Events.on(DisposeEvent.class, e -> {
            if(!headless) OlShaders.dispose();
        });
    }

    public static boolean isOmaloon(Content content){
        return content.minfo.mod != null && content.minfo.mod.name.equals("omaloon");
    }

    public static LoadedMod mod(){
        return mod;
    }

    @Override
    public void loadContent(){
        OlSounds.load();
        OlItems.load();
        OlLiquids.load();
        OlInteractions.load();
        OlStatusEffects.load();
        OlWeathers.load();
        OlUnitTypes.load();
        OlBlocks.load();
        OlPlanets.load();
        OlSectorPresets.load();
        GlasmoreTechTree.load();

        OlEntityMapping.init();
    }
}