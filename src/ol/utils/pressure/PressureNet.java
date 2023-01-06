package ol.utils.pressure;

import arc.math.Mathf;
import mindustry.gen.*;

import ol.gen.*;

import java.util.ArrayList;

public class PressureNet {
    public ArrayList<Building> net = new ArrayList<>();
    public int r, g, b;

    public PressureNet() {
        r = Mathf.random(255);
        g = Mathf.random(255);
        b = Mathf.random(255);
    }

    public void set(PressureAblec block) {
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