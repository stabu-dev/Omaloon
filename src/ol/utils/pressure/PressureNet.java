package ol.utils.pressure;

import arc.graphics.*;
import arc.math.*;
import arc.struct.*;
import mindustry.*;
import mindustry.annotations.Annotations.*;
import mindustry.gen.*;
import ol.gen.*;
import ol.world.blocks.pressure.PressureJunction.*;
import org.jetbrains.annotations.*;

public class PressureNet{
    protected final static LongQueue blockQueue = new LongQueue();
    public final Color color = new Color();
    private IntSet buildingsSet = new IntSet();
    private IntSeq buildingsSeq = new IntSeq();
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
                        if(from == null || PressureAPI.tierAble(from.as(), d)){
                            d.pressureNet(this);
                            blockQueue.addFirst(packBuild(b, target.as()));
                        }
                    }
                });
            }


        }
    }

    public boolean shouldMerge(PressureNet otherNet){
        for(int i = 0; i < buildingsSeq.size; i++){
            int buildPos = buildingsSeq.get(i);
            if(Vars.world.build(buildPos) instanceof PressureJunctionBuild) continue;
            if(otherNet.containsBuilding(buildPos)){
                return true;
            }
        }
        return false;
    }

    public boolean containsBuilding(int buildingPosition){
        return buildingsSet.contains(buildingPosition);
    }

    public void merge(PressureNet otherNet){
        for(int i = 0; i < otherNet.buildingAmount(); i++){
            int next = otherNet.buildingPosition(i);
            if(addBuilding(next)){
                ((PressureAblec)Vars.world.build(next)).pressureNet(this);
            }
        }
    }

    public int buildingAmount(){
        return buildingsSet.size;
    }

    public int buildingPosition(int index){
        return buildingsSeq.get(index);
    }

    public boolean addBuilding(@NotNull Building build){
        return addBuilding(build.pos());
    }

    public boolean addBuilding(int buildPosition){
        boolean add = buildingsSet.add(buildPosition);
        if(add) buildingsSeq.add(buildPosition);
        return add;
    }

    public float calculatePressure(){
        float sum = 0;
        for(int i = 0; i < buildingsSeq.size; i++){
            sum += Pressure.calculateWithCooldown(Vars.world.build(buildingsSeq.get(i)));
        }
        return Math.max(sum, 0);
    }

    public void reset(){
        for(int i = 0; i < buildingsSeq.size; i++){
            Vars.world.build(buildingsSeq.get(i)).<PressureAblec>as().pressureNet(null);
        }
        buildingsSeq.clear();
        buildingsSet.clear();
    }

    public boolean isEmpty(){
        return buildingsSet.isEmpty();
    }

    @Struct
    static class BuildingDataStruct{
        int tileIndex;
        int fromTileIndex;
    }
}