package ol;

import arc.*;
import mindustry.game.*;
import mindustry.mod.*;
import ol.content.*;
import ol.graphics.*;

import static mindustry.Vars.headless;

public class OmaloonMod extends Mod {
    public OmaloonMod() {
        if(!headless) {
            run(EventType.FileTreeInitEvent.class, () -> Core.app.post(OlShaders::load));
            run(EventType.DisposeEvent.class, OlShaders::dispose);
        }
    }

    @Override
    public void loadContent(){
        OlSounds.load();
        OlItems.load();
        OlStatusEffects.load();
        OlLiquids.load();
        OlUnitTypes.load();
        OlBlocks.load();
    }

    public void run(Class<?> aClass, Runnable runnable) {
        Events.on(aClass, (ignored) -> runnable.run());
    }
}