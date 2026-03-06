package omaloon.type;

import arc.struct.*;
import mindustry.type.*;
import omaloon.world.interfaces.*;

public abstract class FluidInteraction{
    public static final Seq<FluidInteraction> interactions = new Seq<>();

    {
        interactions.add(this);
    }

    public abstract boolean canInteract(Liquid liquid1, Liquid liquid2);

    /**
     * Method containing the interaction that will happen if {@link #shouldInteract} returns true.
     */
    public abstract void interaction(HasPressure build);

    /**
     * @return true when the specified building has this interaction possible.
     */
    public abstract boolean shouldInteract(HasPressure build);
}
