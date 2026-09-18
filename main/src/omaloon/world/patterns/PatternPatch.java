package omaloon.world.patterns;

import mindustry.world.*;

/** Defines permanent field overrides for a specific pattern's cloned block. */
public interface PatternPatch{
    /** Apply this patch's field values to a freshly cloned block. Called once at load time. */
    void applyToClone(Block block);
    /** Load assets into the synthetic block (e.g. DrawBlock.load). Called after applyToClone. */
    default void load(Block block){}
}
