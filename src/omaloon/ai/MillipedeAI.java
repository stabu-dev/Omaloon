package omaloon.ai;

import arc.math.geom.*;
import arc.util.*;
import mindustry.ai.types.*;

import static mindustry.Vars.state;


public class MillipedeAI extends FlyingAI {
    public Vec2 pos = new Vec2();
    public float score = 0f;
    public float time = 0f;
    protected float rotateTime = 0f;

    @Override
    public void updateMovement(){

        if(target != null && unit.hasWeapons()){
            if(unit.type.circleTarget){
                circleAttack(120f);
            }else{
                moveTo(target, unit.type.range * 0.8f);
                unit.lookAt(target);
            }
        }

        if(target == null && state.rules.waves && unit.team == state.rules.defaultTeam){
            moveTo(getClosestSpawner(), state.rules.dropZoneRadius + 130f);
        }
        rotateTime = Math.max(0f, rotateTime - Time.delta);
        if(time <= 0) score = 0f;
        time = Math.max(0f, time - Time.delta);
    }
}
