package omaloon.core;

import arc.*;
import arc.mock.*;
import arc.struct.*;
import lombok.*;
import mindustry.*;
import omaloon.*;
import omaloon.core.extra.*;
import omaloon.core.extra.RelatedApplicationListener.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import java.lang.reflect.*;
import java.util.concurrent.atomic.*;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ServerRelatedApplicationListenerTest extends AbstractRelatedApplicationListenerTest{
    private static Seq<ApplicationListener> myListeners=new Seq<>(ApplicationListener.class);


    @AfterAll
    static void afterAll(){
        Core.app=null;
        Vars.headless=false;
    }
    @SneakyThrows
    @BeforeEach
    void setUp(){
        Vars.headless=true;
        Core.app=new MockApplication(){
            @Override
            public Seq<ApplicationListener> getListeners(){
                return myListeners;
            }
        };
        myListeners.set(tmpListeners);
    }

    @SneakyThrows()
    @ParameterizedTest
    @EnumSource(RelativeOrder.class)
    void testInject(RelativeOrder order){
        tryInject(order, tmpListeners[1], switch(order){
            case before, beforeOrEnd, beforeOrStart -> i(0, -1, 1, 2, 3);
            case after, afterOrEnd, afterOrStart -> i(0, 1, -1, 2, 3);
        });
    }

    @ParameterizedTest
    @EnumSource(RelativeOrder.class)
    void checkMissing(RelativeOrder order){
        int[] scheme = switch(order){
            case before, after -> {
                assertThrows(AnchorNotFound.class, () -> {
                    tryInject(order, getUnknown(), i());
                });

                yield null;
            }
            case beforeOrStart, afterOrStart -> i(-1, 0, 1, 2, 3);
            case beforeOrEnd, afterOrEnd -> i(0, 1, 2, 3, -1);
        };
        if(scheme == null) return;
        tryInject(order, getUnknown(), scheme);
    }

    @Override
    ApplicationListener[] getModules(){
        return myListeners.toArray();
    }
}
