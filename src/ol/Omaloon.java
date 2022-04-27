package ol;

import arc.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;
import mindustry.ui.dialogs.*;
import ol.content.*;

import static arc.Core.app;

public class Omaloon extends Mod{

    public Omaloon(){

        Events.on(FileTreeInitEvent.class, e -> app.post(olSounds::load));
        Log.info("Loaded Omaloon constructor.");
    }
    @Override
    public void loadContent(){
        Log.info("Loading some content.");
        olItems.load();
        olLiquids.load();
        olPlanets.load();
        olBlocks.load();
        olSounds.load();

    }

}
