package omaloon.core;

import arc.*;
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

public class ClientRelatedApplicationListenerTest extends AbstractRelatedApplicationListenerTest{

    private MockClientLauncher clientLauncher;


    @SneakyThrows
    private static AtomicReference<ClientLauncher> getClientLauncherRef(){
        Field field = OlVars.class.getDeclaredField("clientLauncher");
        field.setAccessible(true);
        //noinspection unchecked
        return (AtomicReference<ClientLauncher>)field.get(null);
    }

    @AfterAll
    static void afterAll(){
        getClientLauncherRef().set(null);
    }

    @SneakyThrows
    @BeforeEach
    void setUp(){
        getClientLauncherRef().set(clientLauncher = new MockClientLauncher());
        for(ApplicationListener listener : tmpListeners){
            clientLauncher.add(listener);
        }
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
        return clientLauncher.modules();
    }
}
