package omaloon.content;

import omaloon.type.*;
import omaloon.type.liquid.*;

public class OlInteractions{
    public static FluidInteraction boilHeat, burnFlammable, neoplasmSpread;

    // not needed rn, but will be needed if there's any sort of recipe that uses this system
    public static void load(){
        boilHeat = new BoilFluidInteraction();
        burnFlammable = new BurnFluidInteraction();
        neoplasmSpread = new NeoplasmFluidInteraction();
    }
}
