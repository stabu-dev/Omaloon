package ol.utils.pressure;

import arc.*;
import arc.graphics.*;
import arc.struct.*;
import arc.struct.IntSet.*;
import arc.util.*;
import mindustry.gen.*;
import ol.gen.*;
import ol.utils.*;
import ol.world.blocks.pressure.PressureJunction.*;

import static mindustry.Vars.*;

public class PressureUpdater implements ApplicationListener{
    public static final Seq<PressureNet> nets = new Seq<>();
    public static float TICK_TIMER = 0;

    public static void load(){
        Events.on(OlMapInvoker.TileChangeEvent.class, e -> {
            PressureUpdater.reload();
            postCallReload();
        });
    }

    public static void postCallReload(){
        Time.run(5f, PressureUpdater::reload);
    }

    public static void uncolor(){
        nets.forEach(net -> {
            net.color.set(Color.whiteRgba);
        });
    }


    public static void clearNets(){
        nets.clear();
    }

    public static void mergeNets(){
        if(true) return;
        for(int i = 0; i < nets.size; i++){
            PressureNet net = nets.get(i);
            for(int j = i + 1; j < nets.size; j++){
                PressureNet otherNet = nets.get(j);

                if(net.shouldMerge(otherNet)){
                    net.merge(otherNet);
                    nets.remove(j);
                }
            }
        }
    }

    public static void reload(){
        nets.clear();

        //need not repeat nets
        IntSet visited = new IntSet();

        //move each pressure build
        for(PressureAblec pressureAble : OlGroups.pressureAble){
            //if building is scanned when skip
            if(visited.contains(pressureAble.pos()) || pressureAble instanceof PressureJunctionBuild){
                continue;
            }
            //create pressureNet
            PressureNet pressureNet = new PressureNet();
            pressureNet.set(pressureAble);

            //add childrens to the bridge
          /*  for(Building build : pressureAble.children()){
                pressureNet.addBuilding(build);
            }
*/
            //length 0 is impossible

            if(pressureNet.isEmpty()){
                pressureNet.addBuilding(pressureAble.as());
            }

            //add net to nets and net buildings to cache
            nets.add(pressureNet);
            for(int i = 0; i < pressureNet.buildingAmount(); i++){
                visited.addAll(pressureNet.buildingPosition(i));
            }
        }
        mergeNets();
    }

    @Override
    public void update(){
        if(state == null || !state.isGame()){
            return;
        }

        //check if timer reached end
        PressureUpdater.TICK_TIMER -= Time.delta;
        if(PressureUpdater.TICK_TIMER > 0){
            return;
        }

        //launch timer again
        PressureUpdater.TICK_TIMER =
        Pressure.getPressureRendererProgress() + 1;

        //set pressure for each net
        for(PressureNet net : PressureUpdater.nets){

            int blocks = net.buildingAmount();

            //if net is empty
            if(blocks == 0){
                continue;
            }

            //get pressure
            float pressure = net.calculatePressure();

            if(blocks > 1){
                pressure = Math.max(pressure, net.calculatePressure());
            }
            //list each build in net links
            for(int i = 0; i < net.buildingAmount(); i++){
                int pos = net.buildingPosition(i);
                Building building = world.build(pos);

                //if build is pressure and his need to update when update
                if(building instanceof PressureAblec pressureAble){
                    pressureAble.pressure(pressure);
                    pressureAble.onUpdate();
                }
            }
        }
    }
}