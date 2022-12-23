package ol.utils.pressure;

import mindustry.gen.Building;
import ol.world.blocks.pressure.PressureAble;

import java.util.ArrayList;

public class PressureNet {
    public ArrayList<Building> net = new ArrayList<>();

    public void set(PressureAble<?> block) {
        if(block == null) {
            return;
        }

        //set net
        this.net.clear();
        for(Building building : block.net()) {
            if(building == null) {
                continue;
            }

            this.net.add(building);
        }
    }
}