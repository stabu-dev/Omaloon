package omaloon.core;

import arc.*;
import mindustry.*;
import mindustry.core.GameState.*;
import mindustry.game.EventType.*;

import static mindustry.Vars.*;

public class EditorListener implements ApplicationListener{
    boolean isEditor;

    public EditorListener(){
        Events.on(StateChangeEvent.class, e -> {
            if(e.from == State.menu && e.to == State.playing && state.isEditor()){
                if(true){
                    state.map.tags.put("name", editor.tags.get("name"));

                    isEditor = true;
                }
            }else if(isEditor && e.to == State.menu){
                isEditor = false;
            }
        });
    }

    public boolean isEditor(){
        return isEditor;
    }
}
