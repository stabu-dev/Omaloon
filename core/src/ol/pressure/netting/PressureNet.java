package ol.pressure.netting;

import arc.struct.Seq;
import mindustry.gen.Building;

public record PressureNet(Seq<Building> buildings) {
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