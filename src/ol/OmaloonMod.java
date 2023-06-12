package ol;

import arc.*;
import mindustry.game.*;
import mindustry.mod.*;
import mindustry.world.blocks.production.Drill;
import ol.content.*;
import ol.content.blocks.OlMiningBlocks;
import ol.graphics.*;
import ol.world.unit.MiningUnitType;

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

        ((MiningUnitType) OlUnitTypes.drillUnit).placedDrill = (Drill) OlMiningBlocks.unitDrill;
    }

    public void run(Class<?> aClass, Runnable runnable) {
        Events.on(aClass, (ignored) -> runnable.run());
    }
}