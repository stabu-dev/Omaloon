package ol.entities;

import arc.func.*;
import arc.struct.*;
import arc.struct.IntSet.*;
import mindustry.*;
import mindustry.annotations.Annotations.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mma.annotations.ModAnnotations.*;
import ol.gen.*;
import ol.utils.pressure.*;
import ol.world.blocks.pressure.*;
import ol.world.blocks.pressure.PressureJunction.*;
import org.jetbrains.annotations.*;

import static arc.math.Mathf.rand;

@Component
@GenerateDefaultImplementation
abstract class PressureAbleComp implements Buildingc, PressureAblec{
    @Override
    public abstract float pressure();

    @Override
    public abstract void pressure(float pressure);

    @Override
    public abstract PressureNet pressureNet();

    @Override
    public abstract void pressureNet(PressureNet pressureNet);

    public float pressureThread(){
        return 0F;
    }

    public boolean sdx(Building b2, Seq<Building> buildings, boolean jun){
        return b2 instanceof PressureAblec && PressureAPI.netAble(b2, self(), jun) &&
                   !buildings.contains(b2) && b2.enabled;
    }

    @Deprecated
    public float sumx(FloatSeq arr){
        return Math.max(arr.sum(), 0);
    }
    public void nextBuildings(@Nullable Building income,Cons<Building> consumer){
        for(Building building : proximity()){
            if (income==building)continue;
            consumer.get(building);
        }
    }

    public float damageScl(){
        return 0.05f;
    }

    public abstract float maxPressure();

    public abstract boolean canExplode();

    public abstract Effect explodeEffect();

    public void onUpdate(){
        onUpdate(maxPressure(), explodeEffect());
    }

    public void onUpdate(float maxPressure, Effect explodeEffect){
        if(PressureAPI.overload(this)){
            Building self = self();

            float x = self.x;
            float y = self.y;

            self.damage((damageScl() + rand.random(0, 1)) * (pressure() / maxPressure) / 8);

            if(self.health < damageScl() * 1.5f){
                explodeEffect.at(x, y);
                IntSetIterator iterator = pressureNet().buildings.iterator();
                while(iterator.hasNext){
                    if(Vars.world.build(iterator.next()) instanceof PressureJunctionBuild build){
                        build.netKill();
                    }
                }
            }
        }
    }


    public abstract int tier();

    public boolean downPressure(){
        return false;
    }

    public float calculatePressureDown(){
        return 0;
    }

    public boolean online(){
        return true;
    }

    public boolean producePressure(){
        return false;
    }

    public boolean alignX(int rotation){
        return rotation == 0 || rotation == 2;
    }

    public boolean alignY(int rotation){
        return rotation == 1 || rotation == 3;
    }

    public Seq<Building> children(){
        return new Seq<>();
    }

    public boolean inNet(Building b, PressureAblec p, boolean junction){
        if(b == null){
            return false;
        }

        if(self() == b){
            return true;
        }

        int delta = 1;
        if(junction){
            delta++;
        }

        if(!(PressureAPI.tierAble(p, tier())) || !p.online()){
            return false;
        }

        if(nearby(-delta, 0) == b || nearby(delta, 0) == b){
            return alignX(rotation()) || alignX(b.rotation());
        }

        if(nearby(0, delta) == b || nearby(0, -delta) == b){
            return alignY(rotation()) || alignY(b.rotation);
        }

        return false;
    }
}
