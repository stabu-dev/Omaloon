package omaloon;

import arc.*;
import arc.func.*;
import arc.util.*;
import lombok.*;
import mindustry.*;
import mindustry.game.EventType.*;
import omaloon.ui.*;

import static arc.Core.settings;

public class OlVars{
    @Getter(lazy = true)
    private static final ClientLauncher clientLauncher = findClientLauncher();
    @Getter(value = AccessLevel.PRIVATE, lazy = true)
    private static final Cons<ApplicationListener> listenerAdder = computeClientListeners();

    private static ClientLauncher findClientLauncher(){
        if(Vars.headless) return null;
        val seq = Core.app.getListeners();
        for(int i = 0; i < seq.size; i++){
            ApplicationListener listener = seq.get(i);
            if(listener instanceof ClientLauncher launcher) return launcher;
        }

        if(Vars.platform instanceof ClientLauncher launcher) return launcher;

        throw new RuntimeException("Cannot run Omaloon because your client is strange...");
    }

    private static Cons<ApplicationListener> computeClientListeners(){
        if(Vars.headless){
            return Core.app::addListener;
        }else{
            return getClientLauncher()::add;
        }
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

    public static void onClientLoad(Runnable runnable){
        Events.on(ClientLoadEvent.class, it -> runnable.run());
    }
    public static void onServerLoad(Runnable runnable){
        Events.on(ServerLoadEvent.class, it -> runnable.run());
    }
    public static void onAnyLoad(Runnable runnable){
        onClientLoad(runnable);
        onServerLoad(runnable);
    }
}
