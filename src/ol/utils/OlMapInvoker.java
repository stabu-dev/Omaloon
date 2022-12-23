package ol.utils;

import mindustry.gen.*;
import mindustry.world.*;

import java.util.function.*;

import static mindustry.Vars.*;

public class OlMapInvoker {
    public static void eachTile(Consumer<Tile> tileConsumer) {
        if(world == null || world.tiles == null) {
            return;
        }

        world.tiles.eachTile(tileConsumer::accept);
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
}