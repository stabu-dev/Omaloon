package ol.utils.pressure;

import arc.*;
import arc.struct.*;

import ol.world.blocks.crafting.*;
import ol.world.blocks.pressure.*;

import mindustry.gen.*;

public class Pressure {
    public static int getPressureRendererProgress() {
        return Core.settings.getInt("mod.ol.pressureupdate");
    }

    public static void findCrafters(Building building, Seq<PressureAble<?>> foundNow, Seq<Building> cache) {
        findCrafters(building, foundNow, cache, false);
    }

    public static void findCrafters(Building building, Seq<PressureAble<?>> foundNow, Seq<Building> cache, boolean childr) {
        if(building instanceof PressureAble<?> pressureAble) {
            cache.add(building);

            for(Building build : building.proximity) {
                Building b2 = build;

                boolean jun = false;
                if(b2 instanceof PressureJunction.PressureJunctionBuild bj) {
                    b2 = bj.getInvert(building);
                    jun = true;
                }

                if(b2 instanceof PressureAble<?> pr && !cache.contains(b2) && b2 != building && b2.enabled)
                {
                    if(PressureAPI.netAble(b2, building, jun) || childr)
                    {
                        if(PressureAPI.tierAble(pr, pressureAble))
                        {
                            cache.add(b2);

                            if(pr.producePressure() || pr instanceof PressureCrafter.PressureCrafterBuild) {
                                if(!foundNow.contains(pr)) {
                                    foundNow.add(pr);
                                }
                            }

                            findCrafters(b2, foundNow, cache, false);

                            for(Building child : pr.childrens()) {
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
            return 0F;
        }

        return -pressureAble.calculatePressureDown();
    }

    public static float calculatePressure(Building source) {
        Seq<PressureAble<?>> crafters = new Seq<>();
        findCrafters(source, crafters, new Seq<>());

        return Math.max(crafters.sumf(Pressure::calculateWithCooldown), 0);
    }
}