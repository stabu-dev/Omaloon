package omaloon.ui.fragments;

import arc.*;
import arc.graphics.*;
import arc.input.*;
import arc.math.*;
import arc.scene.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import arc.scene.ui.layout.*;
import mindustry.gen.*;
import mindustry.ui.*;
import omaloon.*;
import omaloon.ui.*;
import omaloon.world.blocks.environment.*;

import static mindustry.Vars.*;

public class CliffFragment extends Table{
    private Table layout;
    private boolean enabled;
    private final Color col = Color.valueOf("645654");

    public CliffFragment(){
        setFillParent(true);
        visible(() -> ui.hudfrag.shown && OmaloonMod.editorListener.isEditor());
        touchable(() -> enabled && visible ? Touchable.enabled : Touchable.disabled);
        left();

        add(layout = new Table(Styles.black5, t -> {
            t.table(title -> {
                title.image(Icon.treeSmall).size(15f).center().padRight(15f).color(col);
                title.label(() -> "@fragment.omaloon.cliff-placer").grow();
                title.image(Icon.treeSmall).size(15f).center().padLeft(15f).color(col);
            }).growX().padBottom(10f).row();
            t.table(Styles.black3, buttons -> {
                buttons.button("@ui.omaloon-process-cliffs", Icon.play, Styles.nonet, OlCliff::processCliffs).growX().height(50f).pad(5f).row();
                buttons.button("@ui.omaloon-un-process-cliffs", Icon.undo, Styles.nonet, OlCliff::unProcessCliffs).growX().height(50f).pad(5f);
            }).growX();
        })).margin(10f);
    }

    public void build(Group parent){
        layout.actions(Actions.alpha(0));
        parent.addChildAt(0, this);

        if(!mobile){
            Core.scene.addListener(new InputListener(){
                @Override
                public boolean keyDown(InputEvent event, KeyCode keycode){
                    if(Core.input.keyTap(OlBinding.cliff_placer) && visible){
                        toggle();
                        return true;
                    }
                    return false;
                }
            });
        }
    }

    private void toggle(){
        if(!visible || layout.hasActions()) return;
        enabled = !enabled;
        if(enabled){
            layout.actions(
                Actions.moveBy(-layout.getWidth(), 0),
                Actions.parallel(
                    Actions.alpha(1, 0.3f, Interp.pow3Out),
                    Actions.moveBy(layout.getWidth(), 0, 0.3f, Interp.pow3Out)
                )
            );
        }else{
            layout.actions(
                Actions.parallel(
                    Actions.moveBy(-layout.getWidth(), 0, 0.3f, Interp.pow3Out),
                    Actions.alpha(0, 0.3f, Interp.pow3Out)
                ),
                Actions.moveBy(layout.getWidth(), 0)
            );
        }
    }
}
