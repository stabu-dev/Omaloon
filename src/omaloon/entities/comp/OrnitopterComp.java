package omaloon.entities.comp;

import arc.math.*;
import arc.util.*;
import ent.anno.Annotations.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.type.*;
import omaloon.gen.*;
import omaloon.type.*;
import omaloon.type.Blade.*;

@EntityComponent
abstract class OrnitopterComp implements Unitc, Ornitopterc{
    @Import
    float x, y, rotation;
    @Import
    boolean dead;
    @Import
    UnitType type;
    public BladeMount[] blades;
    public float bladeMoveSpeedScl = 1f;

    @Override
    public void afterRead(){
        setBlades(type);
    }

    @Override
    public void setType(UnitType type){
        setBlades(type);
    }

    public void setBlades(UnitType type){
        if(type instanceof OrnitopterUnitType ornitopter){
            blades = new BladeMount[ornitopter.blades.size];
            for(int i = 0; i < blades.length; i++){
                Blade bladeType = ornitopter.blades.get(i);
                blades[i] = new BladeMount(bladeType);
            }
        }
    }

    public long drawSeed = 0;
    private float driftAngle;
    private boolean hasDriftAngle = false;

    public float driftAngle(){
        return driftAngle;
    }

    @Override
    public void update(){
        drawSeed++;
        OrnitopterUnitType type = (OrnitopterUnitType)this.type;
        float rX = x + Angles.trnsx(rotation - 90, type.fallSmokeX, type.fallSmokeY);
        float rY = y + Angles.trnsy(rotation - 90, type.fallSmokeX, type.fallSmokeY);

        // When dying
        if(dead || health() <= 0){
            if(Mathf.chanceDelta(type.fallSmokeChance)){
                Fx.fallSmoke.at(rX, rY);
                Fx.burning.at(rX, rY);
            }

            // Compute random drift angle if not already set
            if(!hasDriftAngle){
                float speed = Math.max(Math.abs(vel().x), Math.abs(vel().y));
                float maxAngle = Math.min(180f, speed * type.fallDriftScl); // Maximum drift angle based on speed
                driftAngle = (Angles.angle(x, y, x + vel().x, y + vel().y) + Mathf.range(maxAngle)) % 360f;
                hasDriftAngle = true;
            }

            // Drift in random direction
            float driftSpeed = Math.max(0f, vel().len() - type().drag) * type.accel;
            float driftX = driftSpeed * Mathf.cosDeg(driftAngle);
            float driftY = driftSpeed * Mathf.sinDeg(driftAngle);
            move(driftX, driftY);

            rotation = Mathf.lerpDelta(rotation, driftAngle, 0.01f);

            bladeMoveSpeedScl = Mathf.lerpDelta(bladeMoveSpeedScl, 0f, type.bladeDeathMoveSlowdown);
        }else{
            hasDriftAngle = false; // Reset the drift angle flag
            bladeMoveSpeedScl = Mathf.lerpDelta(bladeMoveSpeedScl, 1f, type.bladeDeathMoveSlowdown);
        }

        for(BladeMount blade : blades){
            blade.bladeRotation += ((blade.blade.bladeMaxMoveAngle * bladeMoveSpeedScl) + blade.blade.bladeMinMoveAngle) * Time.delta;
        }
        type.fallSpeed = 0.01f;
    }
}