package omaloon.ai.drone;

import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arclibrary.graphics.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.*;
import mindustry.world.blocks.ConstructBlock.*;
import mindustry.world.blocks.storage.*;
import mindustry.world.blocks.storage.CoreBlock.*;
import ol.gen.*;
import omaloon.ai.*;
import omaloon.gen.*;
import omaloon.utils.*;
import org.intellij.lang.annotations.*;
import org.jetbrains.annotations.*;

import static mindustry.Vars.*;

/**
 * @author Zelaux
 */
public class UtilityDroneAI extends DroneAI{
    public static final float SMOOTH = 30f;
    public static final Vec2 PUBLIC_TMP_TO_OUT = Tmp.v3;
    public static final float POINT_CIRCLE_LENGHT = 1f;
    protected final Vec2 tmpCalculatedPosition = new Vec2();
    public float mineRangeScl = 0.75f;
    public float buildRangeScl = 0.75f;
    public float buildRangeSclInv = 1 - buildRangeScl;
    @MagicConstant(valuesFromClass = BuildState.class)
    protected int buildPositionState = BuildState.unset;

    public UtilityDroneAI(Unit owner){
        super(owner);
    }

    private static float rangeOrInfinite(float buildRange){
        return state.rules.infiniteResources ? Float.MAX_VALUE : buildRange;
    }

    @Override
    public void updateMovement(){
        unit.updateBuilding = false;
        unit.mineTile = null;
        tryTransportItems();

        if(tryBuildMultiple()) return;
        if(tryMine()) return;
        rally();
        if(unit.moving() && unit.type.omniMovement){
            unit.lookAt(unit.vel().angle());
        }
    }

    @Override
    public void unit(Unit unit){
        super.unit(unit);
        buildPositionState = BuildState.unset;
    }

    private boolean tryBuildMultiple(){
        Dronec drone = (Dronec)unit;
        boolean hasBuild = false;
        float buildCounter = drone.buildCounter();
        buildCounter += Time.delta;
        float counter = 1 - Time.delta;
        if(buildCounter < 1f){
            hasBuild |= tryBuild(drone, false);
        }
        for(int i = 0; i < buildCounter; i++){
            drone.buildCounter(counter);
            hasBuild |= tryBuild(drone, true);
            buildCounter -= 1f;
        }

        drone.buildCounter(buildCounter);
        return hasBuild;
    }

    @Override
    public float prefRotation(){
        return unit.rotation;
    }

    private void tryTransportItems(){
        if(unit.stack.amount <= 0) return;

        CoreBlock.CoreBuild core = unit.closestCore();

        if(core != null && !unit.within(core, owner.type.range)){
            core = owner.closestCore();
            if(owner.within(core, mineTransferRange)){
                OlCall.chainTransfer(unit.stack.item, unit.x, unit.y, owner, core);
            }else{
                for(int i = 0; i < unit.stack.amount; i++){
                    Call.transferItemToUnit(unit.stack.item, unit.x, unit.y, owner);
                }
            }
        }else{
            Call.transferItemTo(unit, unit.stack.item, unit.stack.amount, unit.x, unit.y, core);
        }
        unit.clearItem();
    }

    private boolean tryBuild(Dronec drone, boolean shouldReallyBuild){
        Queue<BuildPlan> prev = unit.plans;
        prev.clear();

        Queue<BuildPlan> plans = owner.plans;
        if(plans.isEmpty()) return false;
        if(!owner.updateBuilding) return false;

        CoreBlock.CoreBuild core = unit.team.core();


        int totalSkipped = 0;
        if(DebugDraw.isDraw()){
            DrawText.defaultFont = Fonts.def;
            DebugDraw.request(Layer.end, () -> {
                Draw.color(Pal.heal);
                Lines.circle(owner.x, owner.y, owner.type.buildRange);
                Draw.color(Pal.berylShot);
                Lines.circle(unit.x, unit.y, unit.type.buildRange);
                EFill.polyCircle(unit.x, unit.y, Vars.tilesize / 4f);
            });
            for(int i = 0; i < plans.size; i++){
                BuildPlan plan = plans.get(i);
                int i1 = i;
                DebugDraw.request(Layer.end, () -> {
                    DrawText.drawText(plan, "" + i1);
                });
            }
        }
        final float ownerRange = rangeOrInfinite(owner.type.buildRange);

        //IMPORTANT unit.plans.size must be 0
        for(int i = 0; i < plans.size; i++){
            BuildPlan buildPlan = plans.first();
            if(canBuild(buildPlan, core, ownerRange))
                break;
            plans.removeFirst();
            if(DebugDraw.isDraw()) Fx.fireSmoke.at(buildPlan);
            plans.addLast(buildPlan);
            totalSkipped++;
        }
        @NotNull
        var currentPlan = plans.first();


        boolean withinOwner = owner.within(currentPlan, ownerRange);
        boolean isConstructing = withinOwner && currentPlan.tile().build instanceof ConstructBuild;
        if(totalSkipped == plans.size && !isConstructing)
            return false;

        float myRange = unit.type.buildRange;
        float moveToRange = myRange * buildRangeScl;


        if(unit.within(currentPlan, rangeOrInfinite(myRange))){
            unit.lookAt(currentPlan);
        }else{
            unit.lookAt(unit.vel().angle());
        }
        if(!state.rules.infiniteResources){
            if(isCachedBuilding()){
                if(!canBuild(currentPlan, myRange)){
                    buildPositionState = BuildState.unset;
                }
            }
            if(isNotCachedBuilding()){
                label:
                {
                    if(plans.size <= 1){
                        moveTo(currentPlan, moveToRange, SMOOTH);
                        break label;
                    }


                    for(int i = 1; i < plans.size; i++){
                        BuildPlan next = plans.get(i);
                        if(!canBuild(next, core, ownerRange)) continue;
                        BuildPlan next2 = i + 1 < plans.size ? plans.get(i + 1) : null;
                        Vec2 direction = Tmp.v4.set(unit).sub(currentPlan);
                        buildPositionState = BuildState.buildingPair;
                        if(canBuild(next2, core, ownerRange)){
                            if(OlGeometry.calculateCircle(
                                Tmp.v1.set(currentPlan),
                                Tmp.v2.set(next),
                                Tmp.v3.set(next2),
                                Tmp.cr1
                            )){
                                if(DebugDraw.isDraw()){
                                    float x1 = Tmp.cr1.x;
                                    float y1 = Tmp.cr1.y;
                                    float radius1 = Tmp.cr1.radius;
                                    float x2 = Tmp.v3.set(next2).x;
                                    float y2 = Tmp.v3.y;
                                    DebugDraw.request(Layer.end, () -> {
                                        Draw.color(radius1 < moveToRange ? Pal.ammo : Pal.negativeStat);
                                        Lines.circle(x1, y1, radius1);
                                        Draw.color(Pal.lancerLaser);
                                        Lines.circle(x2, y2, moveToRange);
                                    });
                                }
                                if(Tmp.cr1.radius < moveToRange){
                                    buildPositionState = BuildState.building3;
                                    tmpCalculatedPosition.set(Tmp.cr1.x, Tmp.cr1.y);
                                    moveTo(tmpCalculatedPosition, POINT_CIRCLE_LENGHT, SMOOTH);
                                    break label;
                                }
                            }

                            direction.set(next2).sub(currentPlan);
                        }
                        Vec2 out = PUBLIC_TMP_TO_OUT;

                        resolveMidPosition(currentPlan, next, moveToRange, out, direction);
                        moveTo(out, POINT_CIRCLE_LENGHT, SMOOTH);
                        tmpCalculatedPosition.set(out);

                        break label;
                    }
                    moveTo(currentPlan, moveToRange, SMOOTH);
                }
                if(!canBuild(currentPlan, myRange))
                    return true;
            }else{
                moveTo(tmpCalculatedPosition, POINT_CIRCLE_LENGHT, SMOOTH);
            }
        }
        if(!shouldReallyBuild) return true;
        unit.plans = plans;
        unit.updateBuilding = true;
        int wasSize = plans.size;
        unit.updateBuildLogic();

        boolean isFirst = plans.size != 0 && plans.first() == currentPlan;
        boolean isProcessFinished = currentPlan.breaking ? currentPlan.progress == 0f : currentPlan.progress == 1f;
        if(isFirst && isProcessFinished){
            plans.removeFirst();
        }
        int curSize = plans.size;
        //noinspection UnnecessaryLocalVariable
        boolean finished = isProcessFinished;
        if(!finished){
            unit.lookAt(currentPlan);
        }else{
            if((buildPositionState == BuildState.buildingPair || buildPositionState == BuildState.building2Of3) && wasSize >= 2){
                buildPositionState = BuildState.buildingLastBuildOfPair;
            }else if(buildPositionState == BuildState.building3 && wasSize >= 3){
                buildPositionState = BuildState.building2Of3;
            }else if(buildPositionState == BuildState.buildingLastBuildOfPair){
                buildPositionState = BuildState.unset;
            }else{
                buildPositionState = BuildState.unset;
            }

        }
        if(!state.rules.infiniteResources && currentPlan.progress <= 1){
            for(int i = 0; i < plans.size; i++){
                BuildPlan nextPlan = plans.get(i);
                if(!canBuild(nextPlan, core, ownerRange) || nextPlan == currentPlan) continue;
                if(finished){
                    if(isNotCachedBuilding()) moveTo(nextPlan, moveToRange, SMOOTH);
                    unit.lookAt(nextPlan);
                    break;
                }
                break;
            }
        }
        for(BuildPlan plan : plans){//TODO remove double looping
            if(plan.tile().build instanceof ConstructBlock.ConstructBuild it){
                if(it.progress > 0 && !plan.initialized){
                    plan.initialized = true;
                }
            }
        }
        unit.updateBuilding = false;
        unit.plans = prev;
        return true;
    }

    private boolean canBuild(BuildPlan currentPlan, float myRange){
        return unit.within(currentPlan, myRange - Math.min(tilesize * 1.5f, myRange * buildRangeSclInv / 2));
    }

    private boolean isCachedBuilding(){
        return !isNotCachedBuilding();
    }

    private boolean isNotCachedBuilding(){
        return buildPositionState != BuildState.buildingLastBuildOfPair && buildPositionState != BuildState.building2Of3;
    }

    private void resolveMidPosition(BuildPlan currentPlan, BuildPlan nextPlan, float moveToRange, Vec2 out, Vec2 direction){

        boolean calculated = OlGeometry.calculateIntersectionPointOfCircles(
            Tmp.v1.set(currentPlan),
            Tmp.v2.set(nextPlan),
            moveToRange,
            out,
            direction
        );
        if(!calculated){
            out.set(Tmp.v2)
               .sub(Tmp.v1)
               .nor()
               .scl(moveToRange)
               .add(Tmp.v1);
        }
        if(DebugDraw.isDraw()){
            float x1 = Tmp.v1.x, y1 = Tmp.v1.y;
            float x2 = Tmp.v2.x, y2 = Tmp.v2.y;
            float x3 = out.x, y3 = out.y;
            DebugDraw.request(Layer.end, () -> {
                Draw.color(Pal.negativeStat);
                Lines.circle(x1, y1, moveToRange);
                Draw.color(Pal.lancerLaser);
                Lines.circle(x2, y2, moveToRange);

                Draw.color(Pal.place);
                EFill.polyCircle(x3, y3, tilesize / 4f);

            });
        }
    }

    private boolean canBuild(BuildPlan buildPlan, CoreBuild core, float ownerRange){
        return buildPlan != null && !unit.shouldSkip(buildPlan, core) && owner.within(buildPlan, ownerRange);
    }

    protected boolean tryMine(){
        Tile mineTile = owner.mineTile();
        if(mineTile == null) return false;
        if(owner.stack.amount == owner.type.itemCapacity) return false;
        if((owner.getMineResult(owner.mineTile) != owner.stack.item || owner.stack.amount <= 0) && (owner.stack.amount != 0))
            return false;


        if(!owner.within(mineTile.worldx(), mineTile.worldy(), owner.type.mineRange)) return false;
        unit.mineTile = owner.mineTile;

        moveTo(Tmp.v1.set(mineTile.worldx(), mineTile.worldy()), unit.type.mineRange * mineRangeScl, SMOOTH);
        unit.lookAt(unit.angleTo(owner.mineTile));
        return true;
    }

    static class BuildState{
        public static final int unset = -1;
        public static final int buildingLastBuildOfPair = 0;
        public static final int buildingPair = 1;
        public static final int building3 = 2;
        public static final int building2Of3 = 3;
    }
}
