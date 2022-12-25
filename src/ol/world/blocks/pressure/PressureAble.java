package ol.world.blocks.pressure;

import arc.func.*;
import arc.struct.*;

import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.world.*;

import ol.utils.pressure.*;

import static arc.math.Mathf.*;
import static mindustry.Vars.*;

public interface PressureAble<T extends Building> {
    T self();

    float pressure();
    void pressure(float pressure);

    default boolean updatePressure() {
        return true;
    }

    default boolean sdx(Building b2, Seq<Building> buildings, boolean jun) {
        return b2 instanceof PressureAble<?> && PressureAPI.netAble(b2, self(), jun) &&
                !buildings.contains(b2) && b2.enabled;
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

    @Deprecated
    default float sumx(FloatSeq arr) {
        return Math.max(arr.sum(), 0);
    }

    default float damageScl() {
        return 0.05f;
    }

    float maxPressure();
    boolean canExplode();
    Effect explodeEffect();

    default void onUpdate() {
        onUpdate(maxPressure(), explodeEffect());
    }

    default void onUpdate(float maxPressure, Effect explodeEffect) {
        if(PressureAPI.overload(this)) {
            Building self = self();

            float x = self.x;
            float y = self.y;

            self.damage((damageScl() + rand.random(0,1)) * (pressure() / maxPressure)/8);

            if(self.health < damageScl() * 1.5f) {
                explodeEffect.at(x, y);

                net(self, PressureJunction.PressureJunctionBuild::netKill)
                        .filter(b -> ((PressureAble<?>) b).online());
            }
        }
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
                if(b2 != self()) {
                    buildings.add(b2);
                    ((PressureAble<?>) b2).net(b2, cons, buildings);
                }
            }
        }

        return buildings;
    }

    int tier();

    default boolean downPressure() {
        return false;
    }

    default float calculatePressureDown() {
        return 0;
    }

    default boolean online() {
        return true;
    }

    default boolean producePressure() {
        return false;
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

    default Seq<Building> childrens() {
        return new Seq<>();
    }

    default boolean inNet(Building b, PressureAble<?> p, boolean junction) {
        if(b == null) {
            return false;
        }

        Building self = self();

        if(self == b) {
            return true;
        }

        int delta = 1;
        if(junction) {
            delta++;
        }

        if(!(PressureAPI.tierAble(p, tier())) || !p.online()) {
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