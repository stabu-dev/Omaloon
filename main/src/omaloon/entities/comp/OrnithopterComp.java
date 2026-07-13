package omaloon.entities.comp;

import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.type.*;
import omaloon.annotations.Annotations.*;
import omaloon.entities.*;
import omaloon.entities.Blade.*;
import omaloon.type.*;

@SuppressWarnings("unused")
@EntityComponent
abstract class OrnithopterComp implements Unitc{
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
        if(type instanceof GlasmoreUnitType Glasmore){
            blades = new BladeMount[Glasmore.blades.size];
            for(int i = 0; i < blades.length; i++){
                Blade bladeType = Glasmore.blades.get(i);
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
        GlasmoreUnitType type = (GlasmoreUnitType)this.type;
        float rX = x + Angles.trnsx(rotation - 90, type.fallSmokeX, type.fallSmokeY);
        float rY = y + Angles.trnsy(rotation - 90, type.fallSmokeX, type.fallSmokeY);

        // When dying
        if(dead || health() <= 0){
            if(Mathf.chanceDelta(type.fallSmokeChance)){
                Fx.fallSmoke.at(rX, rY);
                Fx.burning.at(rX, rY);
            }

            // Compute a random spin speed (angular velocity) instead of a target angle
            if(!hasDriftAngle){
                if(vel().len() < 1f) vel().trns(Mathf.random(360f), 1f);
                driftAngle = Mathf.range(type.fallDriftScl / 15f);
                hasDriftAngle = true;
            }

            vel().rotate(driftAngle * Time.delta);

            float drag = 1f - (Math.abs(driftAngle) * 0.003f * Time.delta);
            vel().scl(Math.max(0.5f, drag));

            rotation = Angles.moveToward(rotation, vel().angle(), type.rotateSpeed * Time.delta);

            bladeMoveSpeedScl = Mathf.lerpDelta(bladeMoveSpeedScl, 0f, type.bladeDeathMoveSlowdown);
        }else{
            hasDriftAngle = false; // Reset the drift angle flag
            bladeMoveSpeedScl = Mathf.lerpDelta(bladeMoveSpeedScl, 1f, type.bladeDeathMoveSlowdown);
        }

        for(BladeMount blade : blades){
            blade.bladeRotation += ((blade.blade.bladeMaxMoveAngle * bladeMoveSpeedScl) + blade.blade.bladeMinMoveAngle) * Time.delta;
        }
    }
}