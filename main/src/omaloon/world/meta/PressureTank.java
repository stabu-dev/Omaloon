package omaloon.world.meta;

import arc.struct.*;
import arc.util.*;
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
        production
    }
}
