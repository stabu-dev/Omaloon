package ol.world.blocks.pressure;

import arc.func.Cons;
import arc.math.Mathf;
import arc.struct.FloatSeq;
import arc.struct.Seq;
import mindustry.entities.Effect;
import mindustry.gen.Building;
import mindustry.world.Tile;
import ol.content.OlFx;

import static arc.math.Mathf.rand;
import static mindustry.Vars.*;

public interface PressureAble {
    Building self();

    float pressure();
    void pressure(float pressure);

    default boolean sdx(Building b2, Seq<Building> buildings, boolean jun) {
        return b2 instanceof PressureAble p && inNet(b2, p, jun) && p.inNet(self(), jun) && !buildings.contains(b2) && b2 != self() && b2.enabled;
    }

    default Seq<Building> net(Building building, Cons<PressureJunction.PressureJunctionBuild> cons) {
        return net(building, cons, new Seq<>());
    }

    default Seq<Building> net(Building building) {
        return net(building, j -> {});
    }

    default Seq<Building> net() {
        return net(self());
    }

    default float sumx(FloatSeq arr) {
        return Math.max(arr.sum(), 0);
    }

    default float damageScl() {
        return 0.05f;
    }

    default void onUpdate(boolean canExplode, float maxPressure, Effect explodeEffect) {
        if(pressure() > maxPressure && canExplode) {
            Building self = self();

            float x = self.x;
            float y = self.y;

            self.damage((damageScl() + rand.random(0,1)) * (pressure() / maxPressure)/8);
            /*if(!(self instanceof PressurePipe.PressurePipeBuild)) {
                OlFx.pressureDamage.at(x, y);
            }*/

            if(self.health < damageScl() * 1.5f) {
                explodeEffect.at(x, y);

                net(self, PressureJunction.PressureJunctionBuild::netKill)
                        .filter(b -> ((PressureAble) b).online());
            }
        }
        if(!storageOnly() || WTR()) {
            FloatSeq sum_arr = new FloatSeq();
            Seq<PressureAble> prox = new Seq<>();
            for(Building b : net()) {
                PressureAble p = (PressureAble) b;
                if(!p.storageOnly()) {
                    sum_arr.add(p.pressureThread());
                }

                prox.add(p);
            }

            float sum = sumx(sum_arr);

            pressure(sum);
            prox.each(p -> p.pressure(pressure()));
        }
    }

    default boolean WTR() {
        return false;
    }

    default Seq<Building> net(Building building, Cons<PressureJunction.PressureJunctionBuild> cons, Seq<Building> buildings) {
        for(Building b : building.proximity) {
            Building b2 = b;

            boolean jun = false;
            if(b instanceof PressureJunction.PressureJunctionBuild bj) {
                b2 = bj.getInvert(self());
                cons.get(bj);
                jun = true;
            }

            if(sdx(b2, buildings, jun)) {
                buildings.add(b2);
                ((PressureAble) b2).net(b2, cons, buildings);
            }
        }

        return buildings;
    }

    int tier();

    default boolean inNet(Building b, boolean junction) {
        return inNet(b, (PressureAble) b, junction);
    }

    default boolean online() {
        return true;
    }

    default boolean storageOnly() {
        return true;
    }

    default float pressureThread() {
        return 0;
    }

    default boolean alignX(int rotation) {
        return rotation == 0 || rotation == 2;
    }

    default boolean alignY(int rotation) {
        return rotation == 1 || rotation == 3;
    }

    default boolean inNet(Building b, PressureAble p, boolean junction) {
        if(b == null) {
            return false;
        }

        Building self = self();
        int delta = 1;
        if(junction) {
            delta++;
        }

        if(!(tier() == -1 || p.tier() == -1 || p.tier() == tier()) || !p.online()) {
            return false;
        }

        int tx = self.tileX();
        int ty = self.tileY();

        Tile left = world.tile(tx - delta, ty);
        Tile right = world.tile(tx + delta, ty);

        if(left.build == b || right.build == b) {
            return alignX(self.rotation) || alignX(b.rotation);
        }

        Tile top = world.tile(tx, ty + delta);
        Tile bottom = world.tile(tx, ty - delta);

        if(top.build == b || bottom.build == b) {
            return alignY(self.rotation) || alignY(b.rotation);
        }

        return false;
    }
}