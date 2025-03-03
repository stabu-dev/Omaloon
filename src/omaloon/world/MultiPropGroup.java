package omaloon.world;

import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.world.*;
import omaloon.world.interfaces.*;

public class MultiPropGroup{
    public Seq<Tile> group = new Seq<>();
    public Tile center;
    public int shape = 0;
    public Block type;
    public boolean removed = false;

    public MultiPropGroup(Block type){
        this.type = type;
    }

    public void findCenter(){
        center = group.max(tile -> tile.x + Vars.world.width() * tile.y);
        if(center == null) Log.errTag("what", "HUH?");
    }

    public void findShape(){
        if(!(type instanceof MultiPropI prop)) throw new IllegalArgumentException("that's not a multiprop ya dummy");
        shape = prop.shapes().indexOf(shape -> {
            var find = new Object(){
                boolean found = true;
            };
            shape.eachRelativeCenter((x, y) -> {
                switch(shape.getIdRelativeCenter(x, y)){
                    case 2 -> {
                        if(!group.contains(center.nearby(x, y))) find.found = false;
                    }
                    case 3 -> {
                    }
                    default -> {
                        if(group.contains(center.nearby(x, y))) find.found = false;
                    }
                }
            });
            return find.found;
        });
        if(shape == -1) shape = 0;
    }

    public void remove(){
        group.each(tile -> tile.setBlock(Blocks.air));
        ((MultiPropI)type).removed(this).run();
        removed = true;
    }

    public void update(){
        if(group.contains(tile -> tile.block() != type)) remove();
    }
}
