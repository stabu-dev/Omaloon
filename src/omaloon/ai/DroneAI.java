package omaloon.ai;

import arc.math.geom.*;
import arc.util.*;
import mindustry.entities.units.*;
import mindustry.gen.*;

public class DroneAI extends AIController{
    public static final float maxAnchorDst = 5f;
    public static final float minAnchorDst = 2f;
    public static final float maxAnchorDst2 = maxAnchorDst * maxAnchorDst;
    public static final float minAnchorDst2 = minAnchorDst * minAnchorDst;
    protected Unit owner;
    protected Vec2 anchorPos = new Vec2();
    protected PosTeam posTeam;

    public DroneAI(Unit owner){
        this.owner = owner;
        this.posTeam = PosTeam.create();
    }

    @Override
    public void updateVisuals(){
        if(this.unit.isFlying()){
            this.unit.wobble();

            this.unit.lookAt(prefRotation());
        }
    }

    public float prefRotation(){
        return unit.prefRotation();
    }

    @Override
    public void updateUnit(){
        if(!owner.isValid()){
            Call.unitDespawn(unit);
            return;
        }
        super.updateUnit();
    }

    @Override
    public void updateMovement(){
        rally();
    }

    public void rally(Vec2 pos){
        anchorPos.set(pos);
    }

    public void rally(){
        Vec2 targetPos = Tmp.v1
            .set(anchorPos)
            .rotate(owner.rotation - 90)
            .add(owner);

        float distance2 = unit.dst2(targetPos);
        moveTo(targetPos, minAnchorDst, 30f);

        if(distance2 <= maxAnchorDst2){
            unit.lookAt(owner.rotation());
        }
    }

    public void updateFromClient(){
//TODO some sync command, to detect is DroneAI on server
    }
}
