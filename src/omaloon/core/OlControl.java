package omaloon.core;

import arc.*;
import mindustry.*;
import omaloon.ui.*;
import omaloon.utils.*;


public class OlControl implements ApplicationListener{
    public OlInput input;

    @Override
    public void init(){
        input=new OlInput(Vars.control.input);
    }

    @Override
    public void update(){
        if(Core.input.keyTap(OlBinding.switchDebugDraw)){
            DebugDraw.switchEnabled();
        }
    }
}
