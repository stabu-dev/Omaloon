package omaloon.core;

import arc.*;
import arc.struct.*;
import arc.struct.ObjectMap.*;
import arclibrary.settings.other.*;
import mindustry.*;
import mindustry.game.EventType.*;
import omaloon.ui.*;
import omaloon.utils.*;
import org.jetbrains.annotations.*;

import static arclibrary.utils.entries.Entries.objectEntry;


public class OlControl implements ApplicationListener{
    @SuppressWarnings("unchecked")
    public static final ObjectMap<OlBinding, BooleanSettingKey> bindingToBoolSetting= Seq.with(
        objectEntry(OlBinding.switchBuildDroneAttack,OlSettings.debugDraw),
        objectEntry(OlBinding.switchBuildDroneAttack,OlSettings.droneAutoAIM_Build),
        objectEntry(OlBinding.switchAlwaysDroneAttack,OlSettings.droneAutoAIM_Always)
    ).asMap(it->it.key,it->it.value);
    public OlInput input;
    {
        OlSettings.debugDraw.getUpdateListeners().add((booleanSettingKey, value) -> {
            DebugDraw.switchEnabled(value);
        });
    }

    @Override
    public void init(){
        input = new OlInput(Vars.control.input);
    }

    @Override
    public void update(){
        for(Entry<OlBinding, BooleanSettingKey> entry : bindingToBoolSetting){
            if(Core.input.keyTap(entry.key)){
                BooleanSettingKey value = entry.value;
                value.set(value.get());
            }
        }
    }
}
