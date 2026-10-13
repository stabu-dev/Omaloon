package omaloon.world.patterns;

import mindustry.world.*;

/** Field overrides for the cells of a pattern. */
public interface PatternPatch{
    /** Called before the cell is initialized. */
    void apply(Block cell);
    /** Called after the cell is loaded, e.g. for DrawBlock.load. */
    default void load(Block cell){}
}
