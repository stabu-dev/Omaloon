package omaloon.world.interfaces;

import arc.struct.*;
import arc.util.*;
import mindustry.gen.*;
import mindustry.type.*;
import omaloon.world.graph.*;
import omaloon.world.meta.*;
import omaloon.world.modules.*;

/**
 * Interface representing a pressure building.
 * <h2>Usage</h2>
 * Modify the following as needed for the custom block.
 * <pre>{@code public class ExampleBlock extends Block{
 *     public PressureConfig pressureConfig = new PressureConfig();
 *
 *     public ExampleBlock(String name){
 *         super(name);
 *     }
 *
 *     @Override
 *     public void init(){
 *         super.init();
 *
 *         if(hasLiquids){
 *             hasLiquids = false;
 *             pressureConfig.hasPressure = true;
 *         }
 *     }
 *
 *     @Override
 *     public void setBars(){
 *         super.setBars();
 *         pressureConfig.addBar(this);
 *     }
 *
 *     @Override
 *     public void setStats(){
 *         super.setStats();
 *         pressureConfig.addStats(this, stats);
 *     }
 *
 *     public class ExampleBlockBuild extends Building implements HasPressure{
 *         public PressureModule pressure;
 *
 *         @Override
 *         public Building create(Block block, Team team){
 *             super.create(block, team);
 *             if (pressureConfig().hasPressure) {
 *                 pressure = new PressureModule();
 *                 pressureGraph().addRaw(this);
 *             }
 *             return this;
 *         }
 *
 *         @Override
 *         public void onProximityUpdate(){
 *             super.onProximityUpdate();
 *             if (pressureConfig.hasPressure){
 *                 new PressureGraph().floodMergeGraph(this);
 *             }
 *         }
 *
 *         @Override public PressureModule pressure(){
 *             return pressure;
 *         }
 *         @Override public PressureConfig pressureConfig(){
 *             return pressureConfig;
 *         }
 *
 *         @Override
 *         public void read(Reads read, byte revision){
 *             super.read(read, revision);
 *             if (pressureConfig.hasPressure){
 *                 pressure.read(read);
 *             }
 *         }
 *
 *         @Override
 *         public void write(Writes write){
 *             super.write(write);
 *             if (pressureConfig.hasPressure){
 *                 pressure.write(write);
 *             }
 *         }
 *     }
 * }}</pre>
 * @author Liz
 */
public interface HasPressure{
    static boolean canTransfer(HasPressure from, HasPressure to, @Nullable Liquid fluid, float amount) {
        return
        (from.outputsFluid(to, fluid, amount) && to.acceptsFluid(from, fluid, amount) && amount > 0) ||
        (to.outputsFluid(from, fluid, amount) && from.acceptsFluid(to, fluid, amount) && amount < 0);
    }
    /**
     * Mutually exclusive static connection, should not be influenced by current pressure in a Building.
     */
    static boolean connects(HasPressure from, HasPressure to){
        return from.connects(to) && to.connects(from);
    }

    default boolean acceptsFluid(HasPressure from, @Nullable Liquid fluid, float amount) {
        return pressureConfig().acceptsPressure;
    }

    default void addFluid(@Nullable Liquid fluid, float amount){
        if(amount >= 0){
            pressureSection().addFluid(fluid, amount);
        }else removeFluid(fluid, -amount);
    }

    /**
     * All builds that this block will connect to. By default it returns all blocks available in the Building's {@code proximity} seq.
     */
    default Seq<HasPressure> connections(){
        return toBuilding().proximity
        .map(b -> b instanceof HasPressure p ? p.getFluidDestination(this, null) : null)
        .retainAll(p -> p != null && connects(this, p));
    }

    /**
     * One sided static connection, should not be influenced by current pressure in a Building.
     */
    default boolean connects(HasPressure to){
        return
        pressureConfig().hasPressure &&
        to.toBuilding().team == toBuilding().team;
    }

    /**
     * @return true when this building is vulnerable to over-pressure / under-pressure damage.
     */
    default boolean doPressureDamage(){
        return true;
    }

    default float getFluid(@Nullable Liquid fluid){
        return pressure().getAmount(fluid == null ? -1 : fluid.id);
    }

    default HasPressure getFluidDestination(HasPressure from, @Nullable Liquid fluid){
        return this;
    }

    default float getPressure(@Nullable Liquid fluid){
        return pressure().getPressure(fluid == null ? -1 : fluid.id);
    }

    /**
     * Called whenever a new building is added / removed from this Building's graph.
     */
    default void onPressureGraphUpdate(){

    }

    default boolean outputsFluid(HasPressure to, @Nullable Liquid fluid, float amount) {
        return pressureConfig().outputsPressure;
    }

    PressureModule pressure();

    PressureConfig pressureConfig();

    default PressureGraph pressureGraph(){
        return pressure().graph;
    }

    default PressureTank pressureSection(){
        return pressure().section;
    }

    default void removeFluid(@Nullable Liquid fluid, float amount){
        if(amount >= 0){
            pressureSection().removeFluid(fluid, amount);
        }else addFluid(fluid, -amount);
    }

    default Building toBuilding(){
        return (Building)this;
    }
}
