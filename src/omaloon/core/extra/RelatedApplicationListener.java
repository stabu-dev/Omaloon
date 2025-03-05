package omaloon.core.extra;

import arc.*;
import arc.struct.*;
import arc.util.*;
import lombok.*;
import mindustry.*;
import omaloon.*;
import org.intellij.lang.annotations.*;

import java.lang.reflect.*;

public class RelatedApplicationListener{
    @SneakyThrows
    public static <T extends ApplicationListener> T register(T listener, RelativeOrder relativeOrder, ApplicationListener anchor) throws AnchorNotFound{
        if(Vars.headless){
            Seq<ApplicationListener> listeners = Core.app.getListeners();

            int i =
                resolveIndex(listener,
                    relativeOrder,
                    anchor,
                    listeners.indexOf(anchor),
                    listeners.size);

            listeners.insert(i,listener);
        }else{
            ClientLauncher launcher = OlVars.getClientLauncher();
            Field field = ApplicationCore.class.getDeclaredField("modules");
            field.setAccessible(true);
            ApplicationListener[] modules = (ApplicationListener[])field.get(launcher);

            int i =
                resolveIndex(listener,
                    relativeOrder,
                    anchor,
                    Structs.indexOf(modules, anchor),
                    modules.length);

            ApplicationListener[] newModules=new ApplicationListener[modules.length+1];
            System.arraycopy(modules,0,newModules,0,i);
            newModules[i]=listener;
            System.arraycopy(modules,i,newModules,i+1,modules.length-i);

            field.set(launcher,newModules);
        }
        return listener;
    }

    private static  int resolveIndex(ApplicationListener listener, RelativeOrder relativeOrder, ApplicationListener anchor, int i, int length) throws AnchorNotFound{
        if(i ==-1){
            i = relativeOrder.resolveNoIndex(length);
            if(i ==-1)throw new AnchorNotFound(listener, relativeOrder, anchor);
            return i;
        }
        return relativeOrder.resolveShift(i, length);
    }

    @AllArgsConstructor
    public enum RelativeOrder{
        before(-1),
        beforeOrEnd(1),
        beforeOrStart(0),
        after(-1),
        afterOrEnd(1),
        afterOrStart(0),
        ;
        public final int id = ordinal();
        @MagicConstant(intValues = {-1, 0, 1})
        private final int type;

        public int resolveShift(int index, int length){
            if(id <= beforeOrStart.ordinal()){
                return index;
            }

            return Math.min(index + 1,length);

        }

        public int resolveNoIndex(int length){
            if(type == -1) return -1;
            return length * type;
        }
    }
}
