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
}
