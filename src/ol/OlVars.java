package ol;

import arc.struct.*;
import arc.util.Log;
import mma.*;
import ol.content.*;

import static arc.Core.*;


public class OlVars extends ModVars{
    //core region
    private static final Seq<Runnable> onLoad = new Seq<>();

    static{
        new OlVars();
    }

    //end region
    private OlVars(){

    }

    /**
     * Used to load OlVars to computer memory causing the static block work.
     */
    @SuppressWarnings("unused")
    public static void create(){

    }

    public static void log(String info) {
        app.post(() -> Log.infoTag("ol", info));
    }

    public static void error(Throwable info) {
        app.post(() -> Log.err("ol", info));
    }

    public static void load(){
        onLoad.each(Runnable::run);
        onLoad.clear();
    }

    @Override
    protected void onLoad(Runnable runnable){
        onLoad.add(runnable);
    }

    @Override
    public void loadContent(){
        Runnable[] oLContent = Seq.<Runnable>with(
        OlItems::load,
        OlStatusEffects::load,
        OlLiquids::load,
        new OlBlocks(),
        OlPlanets::load,
        OlSounds::load
        ).flatMap(contentList -> Seq.with(contentList instanceof OlBlocks b ? b.list : new Runnable[]{contentList})).toArray(Runnable.class);
        for(Runnable runnable : oLContent){
            runnable.run();
        }
    }
}