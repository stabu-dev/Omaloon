package omaloon.type;

import omaloon.gen.*;

public class DroneUnitType extends GlassmoreUnitType{
    public DroneUnitType(String name){
        super(name);
        hidden = flying = true;
        playerControllable = logicControllable = false;
        isEnemy = false;
        drawItems = true;
        constructor = DroneUnit::create;
    }
}
