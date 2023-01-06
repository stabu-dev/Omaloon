package ol.entities;

import arc.func.*;
import arc.struct.*;
import mindustry.annotations.Annotations.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.world.*;
import mma.annotations.ModAnnotations.*;
import ol.gen.*;
import ol.utils.pressure.*;
import ol.world.blocks.pressure.*;
import ol.world.blocks.pressure.PressureJunction.*;

import static arc.math.Mathf.rand;
import static mindustry.Vars.world;

@Component
@GenerateDefaultImplementation
abstract class PressureAbleComp implements Buildingc,PressureAblec{
    @Override

 public abstract   float pressure();
public abstract    void pressure(float pressure);

    public float pressureThread() {
        return 0F;
    }

    public    boolean sdx(Building b2, Seq<Building> buildings, boolean jun) {
        return b2 instanceof PressureAblec && PressureAPI.netAble(b2, self(), jun) &&
                   !buildings.contains(b2) && b2.enabled;
    }

    public  Seq<Building> net(Building building, Cons<PressureJunctionBuild> cons) {
        return net(building, cons, new Seq<>());
    }

    public   Seq<Building> net(Building building) {
        return net(building, j -> {});
    }

    public   Seq<Building> net() {
        return net(self());
    }

    @Deprecated
    public  float sumx(FloatSeq arr) {
        return Math.max(arr.sum(), 0);
    }

    public float damageScl() {
        return 0.05f;
    }

  public abstract float maxPressure();
 public abstract   boolean canExplode();
 public abstract   Effect explodeEffect();

    public  void onUpdate() {
        onUpdate(maxPressure(), explodeEffect());
    }

    public   void onUpdate(float maxPressure, Effect explodeEffect) {
        if(PressureAPI.overload(this)) {
            Building self = self();

            float x = self.x;
            float y = self.y;

            self.damage((damageScl() + rand.random(0,1)) * (pressure() / maxPressure)/8);

            if(self.health < damageScl() * 1.5f) {
                explodeEffect.at(x, y);

                net(self, PressureJunction.PressureJunctionBuild::netKill)
                    .filter(b -> ((PressureAblec) b).online());
            }
        }
    }

    public Seq<Building> net(Building building, Cons<PressureJunction.PressureJunctionBuild> cons, Seq<Building> buildings) {
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
                    ((PressureAblec) b2).net(b2, cons, buildings);
                }
            }
        }

        return buildings;
    }

  public abstract int tier();

    public   boolean downPressure() {
        return false;
    }

    public   float calculatePressureDown() {
        return 0;
    }

    public  boolean online() {
        return true;
    }

    public boolean producePressure() {
        return false;
    }

    public   boolean alignX(int rotation) {
        return rotation == 0 || rotation == 2;
    }

    public  boolean alignY(int rotation) {
        return rotation == 1 || rotation == 3;
    }

    public  Seq<Building> childrens() {
        return new Seq<>();
    }

    public   boolean inNet(Building b, PressureAblec p, boolean junction) {
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
