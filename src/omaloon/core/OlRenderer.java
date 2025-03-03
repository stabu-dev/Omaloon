package omaloon.core;

import arc.*;
import mindustry.game.EventType.*;

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

    public void draw(){}

    public void preDraw(){}

}
