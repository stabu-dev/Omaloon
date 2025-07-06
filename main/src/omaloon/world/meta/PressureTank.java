package omaloon.world.meta;

import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.type.*;
import omaloon.content.*;
import omaloon.world.interfaces.*;

public class PressureTank{
    public Seq<HasPressure> builds = new Seq<>();

    /**
     * Adds a certain amount of fluid distributed over the whole tank.
     */
    public void addFluid(@Nullable Liquid fluid, float amount){
        if(amount < 0) removeFluid(fluid, -amount);
        float div = amount / builds.size;
        int id = fluid == null ? -1 : fluid.id;

        for(HasPressure build : builds){
            build.pressure().setAmount(id, build.pressure().getAmount(id) + div);

            float pressure =
            build.pressure().getAmount(id) /
            build.pressureConfig().fluidCapacity /
            OlLiquids.getDensity(fluid);
            build.pressure().setPressure(id, pressure);
        }
    }

    /**
     * Evens out the amount of fluid. Unlike normal flow based on pressure, all builds of the same tank must have an equal amount of each fluid.
     */
    public void equalize() {
        if (builds.size <= 1) return;
        for(int i = -1; i < Vars.content.liquids().size; i++){
            float sum = 0;
            for(HasPressure build : builds) sum += build.pressure().getAmount(i);
            sum /= builds.size;

            for(HasPressure build : builds) build.pressure().setAmount(i, sum);
        }
    }

    /**
     * Removes a certain amount of fluid distributed over the whole tank.
     */
    public void removeFluid(@Nullable Liquid fluid, float amount){
        if(amount < 0) addFluid(fluid, -amount);
        float div = amount / builds.size;
        int id = fluid == null ? -1 : fluid.id;

        for(HasPressure build : builds){
            build.pressure().setAmount(id, build.pressure().getAmount(id) - div);

            float pressure =
            build.pressure().getAmount(id) /
            build.pressureConfig().fluidCapacity /
            OlLiquids.getDensity(fluid);
            build.pressure().setPressure(id, pressure);
        }
    }

    public enum TankGroup{
        transportation,
        pumps,
        production
    }
}
