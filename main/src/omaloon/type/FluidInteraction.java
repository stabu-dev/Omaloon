package omaloon.type;

import arc.struct.*;
import omaloon.world.interfaces.*;

public abstract class FluidInteraction{
    public static final Seq<FluidInteraction> interactions = new Seq<>();

    {
        interactions.add(this);
    }

    /**
     * Method containing the interaction that will happen if {@link #shouldInteract} returns true.
     */
    public abstract void interaction(HasPressure build);

    /**
     * @return true when the specified building has this interaction possible.
     */
    public abstract boolean shouldInteract(HasPressure build);
}
