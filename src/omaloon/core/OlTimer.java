package omaloon.core;

import arc.*;
import mindustry.game.EventType.*;
import omaloon.entities.abilities.*;

public class OlTimer{
    public static int prevClock;
    public static int clock;
    private static int internalClock;
    static {
        Events.run(Trigger.update,()->{
            prevClock=internalClock;
            internalClock++;
            clock=internalClock;
        });
    }

    private static class TestClass implements IClockUpdatable{
        public void update(){
            System.out.println("Hello world");
        }
    }
}
