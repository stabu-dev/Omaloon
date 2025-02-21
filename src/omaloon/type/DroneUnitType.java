package omaloon.type;

import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;
import omaloon.ai.*;
import omaloon.gen.*;

public class DroneUnitType extends GlassmoreUnitType{
    public DroneUnitType(String name){
        super(name);
        hidden = flying = true;
        allowedInPayloads = playerControllable = logicControllable = false;
        isEnemy = false;
        drawItems = true;
        constructor = DroneUnit::create;
    }

    @Override
    public void init(){
        super.init();
        if(!(sample instanceof Dronec)){
            throw new IllegalArgumentException(String.format(
                "%s is not implementing %s",sample.getClass(),Dronec.class
            ));
        }
    }

    @Override
    public Unit create(Team team){
        return super.create(team);
    }

    @Override
    public void update(Unit unit){
        super.update(unit);
        if(!Vars.net.client() || unit.dead)return;
        if(!(unit.controller() instanceof DroneAI droneAI)){

            return;
        }
        droneAI.updateFromClient();
    }
}
