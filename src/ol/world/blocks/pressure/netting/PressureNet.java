package ol.world.blocks.pressure.netting;

import arc.struct.Seq;
import mindustry.gen.Building;

public class PressureNet {
    public Seq<Building> buildings;

    public PressureNet(Seq<Building> buildings) {
        this.buildings = buildings;
    }

    public Seq<Building> hostOnly() {
        var list = new Seq<Building>();
        buildings.forEach(building -> {
            var c = building.getClass();
            if(c.isAnnotationPresent(IncludeToTheNet.class)) {
                if(c.getAnnotation(IncludeToTheNet.class).inHostNet()) {
                    list.add(building);
                }
            }
        });
        return list;
    }
}