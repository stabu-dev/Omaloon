package omaloon;

import arc.*;
import arc.func.*;
import arc.util.*;
import lombok.*;
import mindustry.*;
import omaloon.ui.*;

import static arc.Core.settings;

public class OlVars{
    @Getter(value = AccessLevel.PRIVATE, lazy = true)
    private static final Cons<ApplicationListener> listenerAdder = computeClientListeners();

    private static Cons<ApplicationListener> computeClientListeners(){
        if(Vars.headless){
            return Core.app::addListener;
        }else{
            val seq = Core.app.getListeners();
            for(int i = 0; i < seq.size; i++){
                ApplicationListener listener = seq.get(i);
                if(listener instanceof ClientLauncher launcher) return launcher::add;
            }
        }
        throw new IllegalArgumentException("Cannot run Omaloon because of strange error");
    }

    public static <T extends ApplicationListener> T appListener(T applicationListener){
        getListenerAdder().get(applicationListener);
        return applicationListener;
    }

    public static void init(){
        setKeybinds(OlBinding.values());
    }

    /**
     * @author Zelaux
     * <a href="https://github.com/Zelaux/MindustryModCore/blob/v2/core/src/mmc/core/ModUI.java#L33">source</a>
     */
    public static void setKeybinds(KeyBinds.KeyBind... modBindings){
        Time.mark();
        KeyBinds.KeyBind[] originalBinds = Core.keybinds.getKeybinds();
        KeyBinds.KeyBind[] newBinds = new KeyBinds.KeyBind[originalBinds.length + modBindings.length];

        System.arraycopy(originalBinds, 0, newBinds, 0, originalBinds.length);
        System.arraycopy(modBindings, 0, newBinds, originalBinds.length, modBindings.length);

        OmaloonMod.olLog("Time to combine arrays: @ms", Time.elapsed());
        Core.keybinds.setDefaults(newBinds);
        settings.load();
    }

    public static void onClient(Runnable runnable){
        Events.on(ClientLauncher.class, it -> runnable.run());
    }
}
