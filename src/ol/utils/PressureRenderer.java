package ol.utils;

import arc.ApplicationListener;
import arc.Events;

import mindustry.game.EventType;
import mindustry.gen.Building;
import ol.world.blocks.pressure.PressureAble;

import java.util.ArrayList;

import static mindustry.Vars.state;

public class PressureRenderer implements ApplicationListener {
    public static ArrayList<PressureNet> nets = new ArrayList<>();
    public static int TICK_TIMER = 0;

    public static void load() {
        Events.on(PressureIndicator.PressureNetReloadEvent.class, e -> {
            //if building is instanceof PressureAble or removed any block when reload system
            if(e.building instanceof PressureAble<?> || e.remove) {
                PressureRenderer.reload();
            }
        });

        //always reload at load of world
        Events.on(EventType.WorldLoadEndEvent.class, e -> {
            PressureRenderer.reload();
        });
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

                //add net to nets and net buildings to cache
                nets.add(pressureNet);
                cache.addAll(pressureNet.net);
            }
        });
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
        for(PressureNet net : PressureRenderer.nets)
        {
            //if net is empty
            if(net.net.size() == 0) {
                continue;
            }

            //get pressure
            float pressure = Pressure.calculatePressure(net.net.get(0));

            //list each build in net links
            for(Building building : net.net)
            {
                //if build is pressure and his need to update when update
                if(building instanceof PressureAble<?> pressureAble)
                {
                    if(pressureAble.updatePressure())
                    {
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