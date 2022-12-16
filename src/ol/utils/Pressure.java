package ol.utils;

import arc.ApplicationListener;
import arc.Core;
import arc.struct.Seq;
import arc.util.Timer;

import ol.Omaloon;
import ol.world.blocks.crafting.PressureCrafter;
import ol.world.blocks.pressure.*;

import mindustry.gen.Building;
import ol.world.blocks.sandbox.SandboxCompressor;

import static mindustry.Vars.*;

public class Pressure implements ApplicationListener {
    public float timer = 0;

    @Override
    public void update() {
        timer--;
        if(timer > 0) {
            return;
        }

        timer = Pressure.getPressureRendererProgress() + 1;
        Seq<Building> SCANNED_BUILDINGS = new Seq<>();

        if(world != null && world.tiles != null) {
            world.tiles.eachTile(t -> {
                Building building = t.build;

                //building null check
                if(building == null) {
                    return;
                }

                //if building in building when remove
                if(SCANNED_BUILDINGS.contains(building)) {
                    return;
                }

                //its don't have pressure xd
                if(building instanceof SandboxCompressor.SandboxCompressorBuild) {
                    SCANNED_BUILDINGS.add(building);
                    return;
                }

                //don`t render pressure crafters because his changes pressure of itself
                if(building instanceof PressureCrafter.PressureCrafterBuild crafterBuild) {
                    if(crafterBuild.producePressure()) {
                        SCANNED_BUILDINGS.add(building);
                        return;
                    }
                }

                if(building instanceof PressureAble<?> pressureAble) {
                    //getting some data
                    float netPressure = Pressure.calculatePressure(building);
                    Seq<Building> net = pressureAble.net();

                    //set pressure to all net
                    pressureAble.pressure(netPressure);
                    net.forEach(building1 -> {
                        ((PressureAble<?>) building1).pressure(netPressure);
                    });

                    //net is pressured
                    SCANNED_BUILDINGS.add(net);
                }
            });
        }

        //update all crafters in the array
        SCANNED_BUILDINGS.forEach(building -> {
            if(building instanceof PressureAble<?> pressureAble) {
                pressureAble.onUpdate();
            }
        });
    }

    public static int getPressureRendererProgress() {
        return Core.settings.getInt("mod." + Omaloon.MOD_PREFIX + ".pressureupdate");
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
                    if(pr.inNet(building, pressureAble, jun) && pressureAble.inNet(b2, pr, jun) || childr)
                    {
                        if(pr.tier() == -1 || pressureAble.tier() == -1 || pressureAble.tier() == pr.tier())
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