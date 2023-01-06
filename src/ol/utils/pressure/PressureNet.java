package ol.utils.pressure;

import arc.graphics.*;
import arc.math.*;
import arc.struct.*;
import arc.struct.IntSet.*;
import mindustry.*;
import mindustry.annotations.Annotations.*;
import mindustry.gen.*;
import ol.gen.*;
import ol.world.blocks.pressure.PressureJunction.*;
import org.jetbrains.annotations.*;

public class PressureNet{
    protected final static LongQueue blockQueue = new LongQueue();
    public final Color color = new Color();
    public IntSet buildings = new IntSet();
    //Building.pos()

    public PressureNet(){
        color.fromHsv(Mathf.random(360f), Mathf.random(0.1f, 0.9f), Mathf.random(0.25f, 1f));
        color.a = 1f;
    }

    protected static long packBuild(@NotNull Building target, @Nullable Building fromBuild){
        return BuildingData.get(target.tile.array(), fromBuild == null ? -1 : fromBuild.tile.array());
    }

    @Nullable
    protected static Building fromBuild(long buildingData){
        int i = BuildingData.fromTileIndex(buildingData);
        return i == -1 ? null : Vars.world.tiles.geti(i).build;
    }

    protected static PressureAblec targetBuild(long buildingData){
        return (PressureAblec)Vars.world.tiles.geti(BuildingData.tileIndex(buildingData)).build;
    }

    public void set(PressureAblec build){
        if(build == null){
            return;
        }
        reset();
        blockQueue.clear();
        blockQueue.addFirst(packBuild(build.as(), null));
        build.pressureNet(this);
        while(!blockQueue.isEmpty()){
            var nextLong = blockQueue.removeLast();
            var target = targetBuild(nextLong);
            @Nullable
            Building from = fromBuild(nextLong);

            if(addBuilding(target.as())){
                target.nextBuildings(from, b -> {
                    if(b instanceof PressureAblec d && d.pressureNet() != this){
                        d.pressureNet(this);
                        blockQueue.addFirst(packBuild(b, target.as()));
                    }
                });
            }


        }
    }

    public boolean shouldMerge(PressureNet otherNet){
        IntSetIterator iterator = buildings.iterator();
        while(iterator.hasNext){
            int buildPos = iterator.next();
            if(Vars.world.build(buildPos) instanceof PressureJunctionBuild) continue;
            if(otherNet.buildings.contains(buildPos)){
                return true;
            }
        }
        return false;
    }

    public void merge(PressureNet otherNet){
        for(IntSetIterator iterator = otherNet.buildings.iterator(); iterator.hasNext; ){

            int next = iterator.next();
            if(buildings.add(next)){
                ((PressureAblec)Vars.world.build(next)).pressureNet(this);
            }
        }
    }

    public boolean addBuilding(@NotNull Building build){
        return buildings.add(build.pos());
    }

    public float calculatePressure(){
        float sum = 0;
        for(IntSetIterator iterator = buildings.iterator(); iterator.hasNext; ){
            int value = iterator.next();
            sum += Pressure.calculateWithCooldown(Vars.world.build(value));
        }
        return Math.max(sum, 0);
    }

    public void reset(){
        for(IntSetIterator iterator = buildings.iterator(); iterator.hasNext; ){
            int value = iterator.next();
            Vars.world.build(value).<PressureAblec>as().pressureNet(null);
        }
        buildings.clear();
    }

    @Struct
    static class BuildingDataStruct{
        int tileIndex;
        int fromTileIndex;
    }
}