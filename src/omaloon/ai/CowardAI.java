package omaloon.ai;

import mindustry.ai.types.*;

import static mindustry.Vars.state;

public class CowardAI extends FlyingAI{
    @Override
    public void updateMovement(){
        unloadPayloads();

        if(target != null && unit.hasWeapons()){
            if(unit.health >= unit.maxHealth){
                circleAttack(120f);
            }else{
                moveTo(target, unit.type.range * 0.8f, 20f);
                unit.lookAt(target);
            }
        }

        if(target == null && state.rules.waves && unit.team == state.rules.defaultTeam){
            moveTo(getClosestSpawner(), state.rules.dropZoneRadius + 130f);
        }
    }
}
