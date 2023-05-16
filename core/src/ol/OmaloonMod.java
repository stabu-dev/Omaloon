package ol;

import arc.*;
import mindustry.game.*;
import mindustry.mod.*;

import ol.content.*;
import ol.graphics.*;

import static mindustry.Vars.headless;

public class OmaloonMod extends Mod {

    @Override
    public void init(){
        super.init();
    }

    public OmaloonMod(){
        if(!headless) {
            Events.on(EventType.FileTreeInitEvent.class, e -> Core.app.post(OlShaders::load));

            Events.on(EventType.DisposeEvent.class, e -> {
                OlShaders.dispose();
            });
        }
    }

    @Override
    public void loadContent(){
        OlSounds.load();
        OlItems.load();
        OlStatusEffects.load();
        OlLiquids.load();
        OlBlocks.load();
    }
}