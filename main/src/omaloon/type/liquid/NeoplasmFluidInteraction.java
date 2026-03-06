package omaloon.type.liquid;

import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.type.*;
import omaloon.type.*;
import omaloon.world.interfaces.*;

public class NeoplasmFluidInteraction extends FluidInteraction{
    @Override
    public boolean canInteract(Liquid liquid1, Liquid liquid2) {
        return
            (liquid1 == Liquids.water && liquid2 == Liquids.neoplasm) ||
            (liquid2 == Liquids.water && liquid1 == Liquids.neoplasm);
    }

    @Override
    public void interaction(HasPressure build){
        float remove = Math.min(0.7f * Time.delta, build.getFluid(Liquids.water));

        build.removeFluid(Liquids.water, remove);
        build.addFluid(Liquids.neoplasm, remove);
        build.toBuilding().damageContinuous(((CellLiquid)Liquids.neoplasm).spreadDamage);
        Puddles.deposit(build.toBuilding().tile, Liquids.neoplasm, remove * ((CellLiquid)Liquids.neoplasm).removeScaling);
    }

    @Override
    public boolean shouldInteract(HasPressure build){
        return !Mathf.zero(build.getFluid(Liquids.neoplasm), 0.001f) && !Mathf.zero(build.getFluid(Liquids.water), 0.001f);
    }
}
