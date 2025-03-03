package omaloon.core;

import arc.*;
import arc.graphics.g2d.*;
import mindustry.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import omaloon.ai.*;
import omaloon.entities.abilities.*;

public class OlRenderer implements ApplicationListener{
    {
        Events.run(Trigger.draw, this::draw);
        Events.run(Trigger.drawOver, this::drawOver);
        Events.run(Trigger.preDraw, this::preDraw);
        Events.run(Trigger.postDraw, this::postDraw);
        Events.run(Trigger.uiDrawBegin, this::uiDrawBegin);
        Events.run(Trigger.uiDrawEnd, this::uiDrawEnd);
        Events.run(Trigger.universeDrawBegin, this::universeDrawBegin);
        Events.run(Trigger.universeDraw, this::universeDraw);
        Events.run(Trigger.universeDrawEnd, this::universeDrawEnd);
    }

    /**
     * planets drawn and bloom disabled
     */
    public void universeDrawEnd(){}

    /**
     * skybox drawn and bloom is enabled - use Vars.renderer.planets
     */
    public void universeDraw(){}

    /**
     * before/after bloom used, skybox or planets drawn
     */
    public void universeDrawBegin(){}

    public void uiDrawEnd(){}

    public void uiDrawBegin(){}

    public void drawOver(){}

    public void postDraw(){}

    public void draw(){
        Unit playerUnit = Vars.player.unit();
        UnitType playerType = playerUnit.type;
        if(!DroneAbility.isDroneOwner(playerType)) return;
        int[] indecies = DroneAbility.abilityIndecies(playerType);


        for(int i : indecies){
            if(!(playerUnit.abilities[i] instanceof DroneAbility ability)) continue;//IDK why, but I guess it can be
            for(Unit drone : ability.drones){
                if(!(drone.controller() instanceof DroneAI ai)) return;//ignore other stuff
                ai.globalDraw();
            }
        }


    }

    public void preDraw(){}

}
