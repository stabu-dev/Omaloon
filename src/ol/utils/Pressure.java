package ol.utils;

import arc.struct.Seq;
import mindustry.gen.Building;
import ol.world.blocks.crafting.PressureCrafter;
import ol.world.blocks.pressure.*;

public class Pressure {
    public static void findCrafters(Building building, Seq<PressureAble<?>> foundNow, Seq<Building> cache) {
        findCrafters(building, foundNow, cache, false);
    }

    public static void findCrafters(Building building, Seq<PressureAble<?>> foundNow, Seq<Building> cache, boolean childr) {
        if(building instanceof PressureAble<?> pressureAble) {
            cache.add(building);

            for(Building b : building.proximity) {
                Building b2 = b;

                boolean jun = false;
                if(b2 instanceof PressureJunction.PressureJunctionBuild bj) {
                    b2 = bj.getInvert(building);
                    jun = true;
                }

                if(b2 instanceof PressureAble<?> p && !cache.contains(b2) && b2 != building && b2.enabled) {
                    if(p.inNet(building, pressureAble, jun) && pressureAble.inNet(b2, p, jun) || childr) {
                        cache.add(b2);

                        if(p.producePressure() || p instanceof PressureCrafter.PressureCrafterBuild) {
                            if(!foundNow.contains(p)) {
                                foundNow.add(p);
                            }
                        } else {
                            findCrafters(b2, foundNow, cache, false);

                            for(Building child : p.childrens()) {
                                findCrafters(child, foundNow, cache, true);
                            }
                        }
                    }
                }
            }

            for(Building child : pressureAble.childrens()) {
                if(cache.contains(child)) {
                    continue;
                }

                cache.add(child);
                findCrafters(child, foundNow, cache, true);
            }
        }
    }

    public static float calculateWithCooldown(PressureAble<?> pressureAble) {
        if(pressureAble.producePressure()) {
            return pressureAble.pressure();
        }

        if(!pressureAble.downPressure()) {
            return 0;
        }

        return -pressureAble.calculatePressureDown();
    }

    public static float calculatePressure(Building source) {
        Seq<PressureAble<?>> crafters = new Seq<>();
        findCrafters(source, crafters, new Seq<>());

        return Math.max(crafters.sumf(Pressure::calculateWithCooldown), 0);
    }
}