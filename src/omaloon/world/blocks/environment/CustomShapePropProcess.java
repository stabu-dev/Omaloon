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
    //TODO interfaces
    public Seq<Tile> multiPropTiles = new Seq<>();
    public Seq<MultiPropGroup> multiProps = new Seq<>();

    @Override
    public void init(){
        multiPropTiles.clear();
        multiProps.clear();
        for(Tile tile : Vars.world.tiles){
            Block block = tile.block();
            if(block instanceof MultiPropI && !multiPropTiles.contains(tile)){
                MultiPropGroup multiProp = createMultiProp(tile);
                multiProps.add(multiProp);
                multiPropTiles.add(multiProp.group);
                multiProp.findCenter();
                multiProp.findShape();
            }
        }
    }

    public MultiPropGroup createMultiProp(Tile from){
        Seq<Tile> temp = Seq.with(from);
        MultiPropGroup out = new MultiPropGroup(from.block());
        out.group.add(from);

        while(!temp.isEmpty()){
            Tile tile = temp.pop();
            for(Point2 point : Geometry.d4){
                Tile nearby = tile.nearby(point);
                if(nearby == null) continue;
                if(nearby.block() instanceof MultiPropI && !out.group.contains(nearby) && nearby.block() == out.type){
                    out.group.add(nearby);
                    temp.add(nearby);
                }
            }
        }

        return out;
    }

    @Override
    public void process(){
        multiProps.each(multiProp -> {
            multiProp.update();
            if(multiProp.removed) multiProps.remove(multiProp);
        });

    }

    public void onRemoveBlock(Tile tile, Block block){
        multiProps.each(multiPropGroup -> {
            if(multiPropGroup.group.contains(tile)){
                multiPropGroup.remove();
            }
        });
    }
}
