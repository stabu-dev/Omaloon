package omaloon.world.blocks.environment;

import arc.math.geom.*;
import arc.struct.*;
import mindustry.*;
import mindustry.async.*;
import mindustry.world.*;
import omaloon.world.*;
import omaloon.world.interfaces.*;

public class CustomShapePropProcess implements AsyncProcess{
    public static CustomShapePropProcess instance;
    private final Bits tileSet = new Bits();
    private final Seq<Tile> tempNonThreadSafeSeq = new Seq<>();
    //TODO interfaces
    public Seq<Tile> multiPropTiles = new Seq<>();
    public Seq<MultiPropGroup> multiProps = new Seq<>();

    public static void create(){
        Vars.asyncCore.processes.add(instance = new CustomShapePropProcess());
    }

    @Override
    public void init(){
        multiPropTiles.clear();
        multiProps.clear();
        tileSet.clear();
        for(Tile tile : Vars.world.tiles){
            Block block = tile.block();
            if(!(block instanceof MultiPropI) || tileSet.get(tile.pos())) continue;
            MultiPropGroup multiProp = createMultiProp(tile);
            multiProps.add(multiProp);

            multiPropTiles.add(multiProp.group);
            multiProp.findCenter();
            multiProp.findShape();
        }
    }

    public MultiPropGroup createMultiProp(Tile from){
        Seq<Tile> temp = tempNonThreadSafeSeq.clear().add(from);
        MultiPropGroup out = new MultiPropGroup(from.block());
        out.group.add(from);
        tileSet.set(from.pos());

        while(!temp.isEmpty()){
            Tile tile = temp.pop();
            for(Point2 point : Geometry.d4){
                Tile nearby = tile.nearby(point);
                if(nearby == null) continue;
                if(nearby.block() instanceof MultiPropI && !out.group.contains(nearby) && nearby.block() == out.type){
                    out.group.add(nearby);
                    tileSet.set(nearby.pos());
                    temp.add(nearby);
                }
            }
        }
        temp.clear();
        return out;
    }

    @Override
    public void process(){
        for(int i = 0; i < multiProps.size; i++){
            MultiPropGroup multiProp = multiProps.get(i);
            multiProp.update();
            if(!multiProp.removed) continue;

            multiProps.remove(i);
            i--;
        }

    }

    public void onRemoveBlock(Tile tile, Block block){
        multiProps.each(multiPropGroup -> {
            if(multiPropGroup.group.contains(tile)){
                multiPropGroup.remove();
            }
        });
    }
}
