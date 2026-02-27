package omaloon.entities.comp;
import arc.math.*;
import arc.util.*;
import mindustry.entities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.type.*;
import omaloon.annotations.Annotations.*;

@EntityComponent
abstract class FloatMechComp implements Unitc, Mechc {
    @Import
    UnitType type;
    @Import
    float elevation, walkTime, x, y;

    transient boolean wasElevated;

    @Override
    public void update(){
        elevation = Mathf.approachDelta(elevation, onSolid() || tileOn().floor().isLiquid || isUnderBuildPlan() ? 1f : 0f, type.riseSpeed);

        boolean isElevated = elevation >= 0.09f;
        if(isElevated != wasElevated){
            if(wasElevated){
                mindustry.world.Tile tile = tileOn();
                if(tile != null){
                    mindustry.content.Fx.unitLand.at(x, y, tile.floor().isLiquid ? 1f : 0.5f, tile.getFloorColor());
                }
            }
            wasElevated = isElevated;
        }

        if(isElevated){
            walkTime = 0f;
        }
    }

    boolean isUnderBuildPlan(){
        hitboxTile(Tmp.r1);
        for(Unit unit : Groups.unit){
            for(BuildPlan plan : unit.plans()){
                if(plan.breaking || plan.block == null) continue;
                if(!plan.block.solid && !plan.block.solidifes) continue;
                plan.hitbox(Tmp.r2);
                if(Tmp.r1.overlaps(Tmp.r2)) return true;
            }
        }
        return false;
    }

    @Replace
    @Override
    public float walkExtend(boolean scaled) {
        if (elevation > 0.05f) return 0f;
        
        float raw = walkTime % (type.mechStride * 4);
        if (scaled) return raw / type.mechStride;
        
        if (raw > type.mechStride * 3) raw = raw - type.mechStride * 4;
        else if (raw > type.mechStride * 2) raw = type.mechStride * 2 - raw;
        else if (raw > type.mechStride) raw = type.mechStride * 2 - raw;
        
        return raw;
    }

    @Replace(1)
    @Override
    public EntityCollisions.SolidPred solidity(){
        return null;
    }

    @Replace
    @Override
    public boolean isFlying(){
        return false;
    }

    @Replace
    @Override
    public boolean checkTarget(boolean targetAir, boolean targetGround) {
        return (isGrounded() && targetGround) || (elevation >= 0.09f && targetAir);
    }
}