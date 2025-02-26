package omaloon.core;

import arc.*;
import mindustry.*;
import mindustry.game.EventType.*;
import omaloon.ui.*;
import omaloon.utils.*;


public class OlControl implements ApplicationListener{
    public OlInput input;

    {
        Events.run(ClientLoadEvent.class, () -> {
            input = new OlInput(Vars.control.input);
        });
        ;
    }

    @Override
    public void update(){
        if(Core.input.keyTap(OlBinding.switchDebugDraw)){
            DebugDraw.switchEnabled();
        }
    }
}
