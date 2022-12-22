package ol.utils;

import mindustry.gen.Building;
import mindustry.world.Tile;
import java.util.function.Consumer;

//I hate name of this class
import static mindustry.Vars.*;

public class OlMapInvoker {
    public static void eachTile(Consumer<Tile> tileConsumer) {
        if(world == null || world.tiles == null) {
            return;
        }

        //get each tile
        world.tiles.eachTile(tileConsumer::accept);
    }

    public static Building getBuildingAt(int x, int y) {
        if(world == null || world.tiles == null) {
            return null;
        }

        //if x out of bounds
        if(x > world.width() || x < 0) {
            return null;
        }

        //if y out of bounds
        if(y > world.height() || y < 0) {
            return null;
        }

        Tile tile = world.tiles.get(x, y);
        return tile == null ? null : tile.build;
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