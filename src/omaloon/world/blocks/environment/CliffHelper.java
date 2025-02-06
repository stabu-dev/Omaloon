package omaloon.world.blocks.environment;

import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.util.*;
import mindustry.graphics.*;
import mindustry.world.*;

public class CliffHelper extends Block{
    public CliffHelper(String name){
        super(name);
        breakable = alwaysReplace = false;
        solid = true;
        cacheLayer = CacheLayer.walls;
        fillsTile = false;
        hasShadow = false;
    }

    @Override
    public void drawBase(Tile tile){
        Draw.color(Color.red);
        Fill.square(tile.worldx(), tile.worldy(), 1f, Time.time);
        Draw.color();
    }
}
