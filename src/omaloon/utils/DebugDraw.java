package omaloon.utils;

import arc.*;
import arc.graphics.g2d.*;
import arc.struct.*;
import mindustry.*;
import mindustry.game.EventType.*;
import mindustry.graphics.*;
import omaloon.core.*;
import org.intellij.lang.annotations.*;

public class DebugDraw{
    private static final Seq<Runnable> requests = new Seq<>();
    private static final Seq<Runnable> requests2 = new Seq<>();
    private static boolean hasInit = false;
    private static boolean step1 = false;
    private static boolean isDraw = true;

    static{
        register();
    }

    public static void request(Runnable runnable){
        if(step1) requests.add(runnable);
        else requests2.add(runnable);
    }

    public static void request(@MagicConstant(valuesFromClass = Layer.class) float layer, Runnable runnable){
        request(() -> {
            Draw.draw(layer, runnable);
        });
    }

    private static void register(){
        Events.run(Trigger.draw, DebugDraw::draw);
        Events.run(Trigger.update, DebugDraw::update);
    }

    private static void update(){
        isDraw = OlSettings.debugDraw.get();

    }

    private static void draw(){


        step1 = !step1;
        Seq<Runnable> current;
        if(!step1) current = requests;
        else current = requests2;
        if(!isDraw() || !hasInit && !(isDraw = OlSettings.debugDraw.get())){
            current.clear();
            return;
        }
        current.removeAll(it -> {
            it.run();
            return true;
        });
    }

    public static boolean isDraw(){
        return isDraw && !Vars.headless;
    }

    public static void switchEnabled(boolean isDraw){
        DebugDraw.isDraw = isDraw;
    }
}
