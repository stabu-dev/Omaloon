package omaloon.core;

import arc.*;
import arc.util.*;
import lombok.*;
import omaloon.core.extra.*;
import omaloon.core.extra.RelatedApplicationListener.*;
import org.jetbrains.annotations.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractRelatedApplicationListenerTest{

    public static final NamedApplicationListener[] tmpListeners;
    public static final NamedApplicationListener injected = listener("injected");

    static{
        tmpListeners = new NamedApplicationListener[4];
        for(int i = 0; i < tmpListeners.length; i++){
            tmpListeners[i] = listener(i + "");
        }
    }


    static @NotNull NamedApplicationListener listener(String name){
        return new NamedApplicationListener(name);
    }

    static @NotNull NamedApplicationListener getUnknown(){
        return listener("unknown");
    }

    static int[] i(int... ints){
        return ints;
    }

    abstract ApplicationListener[] getModules();

    @SneakyThrows
    protected void tryInject(RelativeOrder order, NamedApplicationListener anchor, int[] expectedScheme){
        RelatedApplicationListener.register(injected, order, anchor);
        ApplicationListener[] modules = getModules();
        int[] currentScheme = new int[modules.length];
        for(int i = 0; i < modules.length; i++){
            ApplicationListener module_ = modules[i];
            NamedApplicationListener module = (NamedApplicationListener)module_;
            currentScheme[i] = Strings.parseInt(module.name, -1);
        }
        assertEquals(Arrays.toString(expectedScheme), Arrays.toString( currentScheme));
    }

}
