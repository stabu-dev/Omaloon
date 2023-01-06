package ol.utils;

import arc.ApplicationListener;
import arc.Events;
import mindustry.game.EventType;
import mindustry.gen.*;
import mindustry.world.*;

import java.util.function.*;
import static mindustry.Vars.*;

public class OlMapInvoker {
    public static void load() {
        for(Class<?> aClass : new Class[] {
                EventType.BlockBuildEndEvent.class,
                EventType.BlockBuildEndEvent.class,
                EventType.BlockDestroyEvent.class,
                EventType.BuildSelectEvent.class,
                EventType.BuildDamageEvent.class,
                EventType.BuildingCommandEvent.class,
                EventType.BuildingBulletDestroyEvent.class,
                EventType.WorldLoadEndEvent.class,
                EventType.BlockInfoEvent.class
        }) {
            Events.on(aClass, e -> Events.fire(new TileChangeEvent()));
        }
    }

    public void update() {
    }

    public static void eachTile(Consumer<Tile> tileConsumer) {
        if(world == null || world.tiles == null) {
            return;
        }

        world.tiles.eachTile(tileConsumer::accept);
    }

    public static Block blockOf(Building building) {
        return building == null ? null : building.block;
    }

    public static Building getBuildingOf(Tile tile) {
        return tile == null ? null : tile.build;
    }

    public static void eachBuild(Consumer<Building> buildingConsumer) {

        OlMapInvoker.eachTile(tile -> {
            Building building = OlMapInvoker.getBuildingOf(tile);

            //if build not null when accept
            if(building != null) {
                buildingConsumer.accept(building);
            }
        });
    }

    public static class TileChangeEvent {
    }
}