package ol.utils.pressure;

import arc.*;

import mindustry.game.*;
import mindustry.gen.*;
import mindustry.world.*;

import ol.utils.*;

public class PressureIndicator {
    public static void load() {
        Events.on(EventType.BlockDestroyEvent.class, e ->
                PressureNetReloadEvent.throwEvent(e.tile, true)
        );

        Events.on(EventType.BlockBuildEndEvent.class, e ->
                PressureNetReloadEvent.throwEvent(e.tile, false)
        );
    }

    public static class PressureNetReloadEvent {
        public Building building;
        public boolean remove;

        public PressureNetReloadEvent(Building building, boolean remove) {
            this.building = building;
            this.remove = remove;
        }

        public static void throwEvent(Building building, boolean remove) {
            Events.fire(new PressureNetReloadEvent(building, remove));
        }

        public static void throwEvent(Tile tile, boolean remove) {
            PressureNetReloadEvent.throwEvent(OlMapInvoker.getBuildingOf(tile), remove);
        }
    }
}