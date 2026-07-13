package omaloon.entities.comp;

import arc.math.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.gen.*;
import mindustry.type.*;
import omaloon.annotations.Annotations.*;
import omaloon.entities.*;
import omaloon.type.*;

@SuppressWarnings("unused")
@EntityComponent
abstract class OrnithopterComp implements Unitc{
    public transient float bladeMoveSpeedScl = 1f;
    @Import
    float x, y, rotation;
    @Import
    boolean dead;
    @Import
    UnitType type;
    private float driftAngle;
    private boolean hasDriftAngle = false;

    public float driftAngle(){
        return driftAngle;
    }

    @Override
    public void destroy(){
        if(Vars.headless) return;

        for(var part : type.parts){
            if(part instanceof omaloon.entities.part.BladePart blade){
                int len = blade.mirror ? 2 : 1;
                for(int s = 0; s < len; s++){
                    float sideMultiplier = (s == 0 ? 1f : -1f);
                    float rx = x + Angles.trnsx(rotation - 90, blade.x * sideMultiplier, blade.y);
                    float ry = y + Angles.trnsy(rotation - 90, blade.x * sideMultiplier, blade.y);

                    omaloon.content.OlFx.bladeDestroy.at(rx, ry, rotation - 90,
                    new BladeDestroyData(blade.bladeRegion, blade.bladeOutlineRegion, blade.bladeSizeScl, sideMultiplier));
                }
            }
        }
    }

    @Override
    public void update(){
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
    }
}