package ol.world.blocks.pressure;

import arc.Core;
import arc.util.io.Reads;
import arc.util.io.Writes;

import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.Building;
import mindustry.ui.Bar;
import mindustry.world.Block;

import ol.utils.PressureAPI;
import ol.world.meta.*;

import static ol.graphics.OlPal.*;

public class PressureBlock extends Block {
    public Effect explodeEffect = Fx.none;

    public float dangerPressure = -1;
    public float maxPressure = 100;
    public boolean canExplode = true;
    public int tier = PressureAPI.NULL_TIER;

    public PressureBlock(String name) {
        super(name);
    }

    @Override
    public void setBars() {
        super.setBars();

        if(canExplode) {
            addBar("pressure", (PressureBlockBuild build) -> new Bar(
                    () -> Core.bundle.get("bar.pressure") + " " + (int) build.pressure(),
                    () -> mixcol(oLPressureMin, oLPressure, build.getPressureProgress()),
                    build::getPressureProgress
            ));
        }
    }

    @Override
    public void setStats() {
        super.setStats();

        if(canExplode) {
            stats.add(OlStat.maxPressure, maxPressure, OlStatUnit.pressure);
        }
    }

    @SuppressWarnings("unchecked")
    public class PressureBlockBuild extends Building implements PressureAble<PressureBlockBuild> {
        //public float lag_counter = 0;
        public float pressure = 0;

        @Override
        public PressureBlockBuild self() {
            return this;
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(pressure());
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            pressure(read.f());
        }

        @Override
        public void updateTile() {
            onUpdate();

            //lag_counter--;
            //if(lag_counter < 0) {
            //    onUpdate(canExplode, maxPressure, explodeEffect);
            //    lag_counter = Pressure.getPressureRendererProgress();
            //}
        }

        @Override
        public float pressure() {
            return pressure;
        }

        @Override
        public void pressure(float pressure) {
            this.pressure = pressure;
        }

        @Override
        public float maxPressure() {
            return maxPressure;
        }

        @Override
        public boolean canExplode() {
            return canExplode;
        }

        @Override
        public Effect explodeEffect() {
            return explodeEffect;
        }

        @Override
        public int tier() {
            return tier;
        }

        public float getPressureProgress() {
            return pressure() / maxPressure();
        }
    }
}