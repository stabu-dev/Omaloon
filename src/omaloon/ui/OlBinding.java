package omaloon.ui;

import arc.KeyBinds.*;
import arc.input.InputDevice.*;
import arc.input.*;
import arc.struct.*;
import org.jetbrains.annotations.*;

public enum OlBinding implements KeyBind{
    shaped_env_placer(KeyCode.o, "omaloon-editor"),
    switchDebugDraw(KeyCode.f12, "omaloon-debug-draw"),
    cliff_placer(KeyCode.p, "omaloon-editor");

    public static final @NotNull ObjectMap<String, Seq<OlBinding>> categoryMap;
    static {
        categoryMap = new ObjectMap<>();
        for(OlBinding bind : OlBinding.values()){
            categoryMap.get(bind.category(),Seq::new).add(bind);
        }
    }
    private final KeybindValue defaultValue;
    private final String category;

    OlBinding(KeybindValue defaultValue, String category){
        this.defaultValue = defaultValue;
        this.category = category;
    }

    @Override
    public KeybindValue defaultValue(DeviceType type){
        return defaultValue;
    }

    @Override
    public String category(){
        return category;
    }
}
