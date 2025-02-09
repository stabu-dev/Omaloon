package omaloon.ui;

import arc.KeyBinds.*;
import arc.input.InputDevice.*;
import arc.input.*;
import mindustry.input.*;

import static arc.Core.*;

public enum OlBinding implements KeyBind{
    shaped_env_placer(KeyCode.o, "omaloon-editor"),
    switchDebugDraw(KeyCode.f12, "omaloon-debug-draw"),
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

    public static void load(){
        KeyBind[] orign = Binding.values();
        KeyBind[] moded = values();
        KeyBind[] binds = new KeyBind[orign.length + moded.length];

        System.arraycopy(orign, 0, binds, 0, orign.length);
        System.arraycopy(moded, 0, binds, orign.length, moded.length);

        keybinds.setDefaults(binds);
        settings.load(); // update controls
//        ui.controls = new KeybindDialog();
    }
}
