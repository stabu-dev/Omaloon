package ol;

import arc.*;
import mindustry.content.UnitTypes;
import mindustry.game.*;
import mindustry.mod.*;

import net.tmmc.util.events.Events;
import ol.content.*;
import ol.graphics.*;

import static mindustry.Vars.headless;

public class OmaloonMod extends Mod {
    public OmaloonMod() {
        if(!headless) {
            Events.register(EventType.FileTreeInitEvent.class, () -> Core.app.post(OlShaders::load));
            Events.register(EventType.DisposeEvent.class, OlShaders::dispose);
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
}