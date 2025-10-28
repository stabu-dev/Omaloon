package omaloon.type.liquid;

import arc.math.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.type.*;
import omaloon.type.*;
import omaloon.world.interfaces.*;

public class BoilFluidInteraction extends FluidInteraction{
    @Override
    public void interaction(HasPressure build){
        Seq<Liquid> lowTemp = Vars.content.liquids().select(l -> !Mathf.zero(build.getFluid(l), 0.001f) && l.blockReactive && l.temperature < 0.55f);
        Seq<Liquid> highTemp = Vars.content.liquids().select(l -> !Mathf.zero(build.getFluid(l), 0.001f) && l.blockReactive && l.temperature > 0.7f);

        float remove = Math.min(0.7f * Time.delta, Math.min(build.getFluid(lowTemp.first()), build.getFluid(highTemp.first())));

        build.removeFluid(lowTemp.first(), remove);
        build.removeFluid(highTemp.first(), remove);
        // TODO make something special since pipes are completely sealed?
        if(Mathf.chance(0.2)) Fx.steam.at(build.toBuilding().x + Mathf.range(4f), build.toBuilding().y + Mathf.range(4f));
    }

    @Override
    public boolean shouldInteract(HasPressure build){
        Seq<Liquid> lowTemp = Vars.content.liquids().select(l -> !Mathf.zero(build.getFluid(l), 0.001f) && l.blockReactive && l.temperature < 0.55f);
        Seq<Liquid> highTemp = Vars.content.liquids().select(l -> !Mathf.zero(build.getFluid(l), 0.001f) && l.blockReactive && l.temperature > 0.7f);
        return !lowTemp.isEmpty() && !highTemp.isEmpty();
    }
}
