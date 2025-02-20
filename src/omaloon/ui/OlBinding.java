package omaloon.ui;

import arc.KeyBinds.*;
import arc.input.InputDevice.*;
import arc.input.*;

public enum OlBinding implements KeyBind{
    shaped_env_placer(KeyCode.o, "omaloon-editor"),
    cliff_placer(KeyCode.p, "omaloon-editor");

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
