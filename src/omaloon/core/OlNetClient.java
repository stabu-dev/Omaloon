package omaloon.core;

import arc.*;
import mindustry.*;
import mindustry.gen.*;
import omaloon.ai.drone.*;
import omaloon.entities.abilities.*;

public class OlNetClient implements ApplicationListener{
    @Override
    public void update(){
        if(!Vars.mobile) return;
        if(Vars.player == null) return;
        Unit unit = Vars.player.unit();
        if(unit == null) return;
        if(!DroneAbility.isDroneOwner(unit.type)) return;
        int[] indecies = DroneAbility.abilityIndecies(unit.type);
        for(int i : indecies){
            DroneAbility ability = (DroneAbility)unit.abilities[i];
            for(Unit drone : ability.drones){
                if(!(drone.controller() instanceof AttackDroneAI attackDroneAI)) continue;
                attackDroneAI.beforeSync();

            }
        }
    }
}
