package omaloon.world.blocks.environment;

import arc.Events;
import arc.struct.*;
import mindustry.Vars;
import mindustry.async.*;
import mindustry.game.EventType;
import mindustry.world.*;
import omaloon.world.interfaces.*;

import static mindustry.Vars.*;

public class CustomShapePropProcess implements AsyncProcess {
    //TODO interfaces
    private static final IntSeq multiProps = new IntSeq();

    /**
     * called whenever the world is loaded, it will clear all multi props and assign new ones
     */
    public void init(){
        multiProps.clear();
        for(Tile tile : world.tiles){
            Block block = tile.block();
            if(block instanceof MultiPropI prop){
                prop.initTile(tile);
                multiProps.add(tile.pos());
            }
        }
    }

    public void update(){
        if(multiProps.size == 0) return;

        for(int i = 0; i < multiProps.size; i++){
            int index = multiProps.get(i);
            Tile tile = world.tile(index);
            Block block = tile.block();
            if(block instanceof CustomShapePropI prop){
                prop.updateTile(tile);
            }
        }
    }

    /**
     * called whenever any block is removed, including props
     */
    public void onRemoveBlock(Tile tile, Block block){
        if(block instanceof SubMultiPropI slave){
            slave.parent().slaveRemoved(tile);
        }
    }
}
