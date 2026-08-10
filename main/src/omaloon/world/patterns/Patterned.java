package omaloon.world.patterns;

import mindustry.world.*;

public interface Patterned{
    Pattern getPattern();

    default Pattern getPattern(Tile tile){
        return getPattern();
    }

    default boolean wholeShape(){
        return false;
    }

    /**
     * Which slot of {@code tile.extraData} this block owns.
     * <ul>
     *   <li>0 (default) — bits 7–0, used by floor and block-layer patterns</li>
     *   <li>1 — bits 15–8, used by overlay patterns</li>
     * </ul>
     */
    default int configSlot(){
        return 0;
    }
}
