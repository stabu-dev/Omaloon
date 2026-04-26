package omaloon.type.liquid;

import arc.math.*;
import arc.util.*;
import mindustry.type.*;
import omaloon.content.*;
import omaloon.type.*;
import omaloon.world.interfaces.*;

public class GlaciumRecycleFluidInteraction extends FluidInteraction{
    @Override
    public boolean canInteract(Liquid liquid1, Liquid liquid2){
        return
            (liquid1 == OlLiquids.tiredGlacium && liquid2 == null) ||
            (liquid2 == OlLiquids.tiredGlacium && liquid1 == null);
    }

    @Override
    public void interaction(HasPressure build){
        float swap = Mathf.clamp(Math.min(build.getFluid(OlLiquids.tiredGlacium), build.getFluid(null) / 2f), 0f, 0.1f * Time.delta);

        build.removeFluid(OlLiquids.tiredGlacium, swap);
        build.removeFluid(null, swap * 2f);
        build.addFluid(OlLiquids.glacium, swap);
    }

    @Override
    public boolean shouldInteract(HasPressure build){
        return !Mathf.zero(build.getFluid(OlLiquids.tiredGlacium), 0.001f) && build.getFluid(null) > 0.001f;
    }
}
