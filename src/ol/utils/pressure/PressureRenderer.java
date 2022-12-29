package ol.utils.pressure;

import arc.*;
import java.util.*;

import arc.util.Time;
import mindustry.game.*;
import mindustry.gen.*;

import ol.utils.*;
import ol.world.blocks.pressure.*;

import static mindustry.Vars.*;

public class PressureRenderer implements ApplicationListener {
    public static ArrayList<PressureNet> nets = new ArrayList<>();
    public static int TICK_TIMER = 0;

    public static void load() {
        Events.on(OlMapInvoker.TileChangeEvent.class, e -> {
            PressureRenderer.reload();
            postCallReload();
        });

        //always reload at load of world
        Events.on(EventType.WorldLoadEndEvent.class, e -> {
            PressureRenderer.reload();
            postCallReload();
        });
    }

    public static void postCallReload() {
        Time.run(5f, PressureRenderer::reload);
    }

    public static void uncolor() {
        nets.forEach(net -> {
            net.r = 255;
            net.g = 255;
            net.b = 255;
        });
    }

    public static void removeDublicates() {
        nets.forEach(net -> {
            ArrayList<Building> newNet = new ArrayList<>();

            for(Building building : net.net) {
                if(newNet.contains(building)) {
                    continue;
                }

                newNet.add(building);
            }

            net.net = newNet;
        });
    }

    public static void clearNets() {
        nets = new ArrayList<>();
    }

    public static void mergeNets() {
        ArrayList<PressureNet> merged = new ArrayList<>();

        for(PressureNet net : nets) {
            if(net == null) {
                continue;
            }

            for(PressureNet net2 : nets) {
                if(net2 == null || net2 == net) {
                    continue;
                }

                label: {
                    for(Building building : net2.net) {
                        for(Building building1 : net.net) {
                            if(building1 == building) {
                                net.net.addAll(net2.net);
                                nets.set(nets.indexOf(net2), null);
                                break label;
                            }
                        }
                    }
                }
            }

            merged.add(net);
        }

        nets = merged;
    }

    public static void reload() {
        nets = new ArrayList<>();

        //need not repeat nets
        ArrayList<Building> cache = new ArrayList<>();

        //move each build
        OlMapInvoker.eachBuild(building -> {
            //if building is scanned when skip
            if(cache.contains(building)) {
                return;
            }

            //if building is PressureBlock when create net
            if(building instanceof PressureAble<?> pressureAble) {
                //create pressureNet
                PressureNet pressureNet = new PressureNet();
                pressureNet.set(pressureAble);

                //add childrens to the bridge
                for(Building build : pressureAble.childrens()) {
                    pressureNet.net.add(build);
                }

                //length 0 is impossible
                if(OlArrays.lengthOf(pressureNet.net) == 0) {
                    pressureNet.net.add(building);
                }

                //add net to nets and net buildings to cache
                nets.add(pressureNet);
                cache.addAll(pressureNet.net);
            }
        });

        removeDublicates();
        mergeNets();
        removeDublicates();
    }

    @Override
    public void update() {
        if(state == null || state.isPaused()) {
            return;
        }

        //check if timer reached end
        PressureRenderer.TICK_TIMER--;
        if(PressureRenderer.TICK_TIMER > 0) {
            return;
        }

        //launch timer again
        PressureRenderer.TICK_TIMER =
                Pressure.getPressureRendererProgress() + 1;

        //set pressure for each net
        for(PressureNet net : PressureRenderer.nets) {
            int blocks = net.net.size();

            //if net is empty
            if(blocks == 0) {
                continue;
            }

            //get pressure
            float pressure = Pressure.calculatePressure(net.net.get(0));

            if(blocks > 1) {
                pressure = Math.max(pressure, Pressure.calculatePressure(net.net.get(blocks - 1)));
            }

            //list each build in net links
            for(Building building : net.net) {
                //if build is pressure and his need to update when update
                if(building instanceof PressureAble<?> pressureAble) {
                    if(pressureAble.updatePressure()) {
                        //set pressure
                        pressureAble.pressure(pressure);
                    }

                    //just update
                    pressureAble.onUpdate();
                }
            }
        }
    }
}