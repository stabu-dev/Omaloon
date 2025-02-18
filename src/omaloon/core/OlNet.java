package omaloon.core;

import mindustry.annotations.Annotations;
import mindustry.annotations.Annotations.Loc;
import mindustry.annotations.Annotations.Remote;
import mindustry.gen.Building;
import mindustry.gen.Itemsc;
import mindustry.gen.Unit;
import mindustry.type.Item;
import omaloon.world.save.OlDelayedItemTransfer;

public class OlNet {
    @Remote(called = Loc.both, targets = Loc.server)
    public static void chainTransfer(Item item, float x, float y, Unit owner, Building core) {
        OlDelayedItemTransfer.makeRequest(item,x,y,owner,core);
//        InputHandler.transferItemToUnit();
    }
}
