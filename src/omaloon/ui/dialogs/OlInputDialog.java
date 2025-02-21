package omaloon.ui.dialogs;

import arc.*;
import arc.graphics.*;
import arc.input.*;
import arc.math.*;
import arc.scene.actions.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;
import omaloon.ui.*;

import static arc.Core.*;

public class OlInputDialog extends BaseDialog{
    ObjectMap<String, Table> categories = new ObjectMap<>();
    Table rebindTable;
    Dialog rebindDialog;

    boolean rebindAxis, rebindMin;
    KeyBinds.KeyBind rebindKey;
    KeyCode minKey;

    public OlInputDialog(){
        super("@keybind.title");
        addCloseButton();
        setFillParent(true);
        fill(Styles.black5, t -> rebindTable = t);
        rebindTable.actions(Actions.fadeOut(0));
        rebindDialog = new Dialog("a", new DialogStyle(){{
            titleFont = Fonts.def;
        }});
        shown(this::build);
    }

    private void build(){
        cont.clear();
        categories.clear();
        cont.pane(table -> {
            for(KeyBinds.Section section : keybinds.getSections()){
                var binds = OlBinding.values();
                for(OlBinding keybind : binds){
                    Table t = categories.get(keybind.category(), () -> {
                        table.row();
                        return table.table(init -> {
                            init.label(() -> Core.bundle.get("binding.category-" + keybind.category())).color(Pal.accent).row();
                            init.image().color(Pal.accent).growX().row();
                        }).get().table().minWidth(400f).padTop(10f).growX().get();
                    });

                    if(keybind.defaultValue(section.device.type()) instanceof KeyBinds.Axis){
                        t.add(bundle.get("keybind." + keybind.name() + ".name", Strings.capitalize(keybind.name())), Color.white).left().padRight(40).padLeft(8);

                        t.labelWrap(() -> {
                            KeyBinds.Axis axis = keybinds.get(section, keybind);
                            return axis.key != null ? axis.key.toString() : axis.min + " [red]/[] " + axis.max;
                        }).color(Pal.accent).left().minWidth(90).fillX().padRight(20);

                        t.button("@settings.rebind", Styles.defaultt, () -> {
                            if(!rebindTable.hasActions()){
                                rebindAxis = true;
                                rebindMin = true;
                                openDialog(section, keybind, true);
                            }
                        }).width(130f);
                    }else{
                        t.add(bundle.get("keybind." + keybind.name() + ".name", Strings.capitalize(keybind.name())), Color.white).left().padRight(40).padLeft(8);
                        t.label(() -> keybinds.get(section, keybind).key.toString()).color(Pal.accent).left().minWidth(90).padRight(20);

                        t.button("@settings.rebind", Styles.defaultt, () -> {
                            if(!rebindTable.hasActions()){
                                rebindAxis = false;
                                rebindMin = false;
                                openDialog(section, keybind, true);
                            }
                        }).width(130f);
                    }
                    t.button("@settings.resetKey", Styles.defaultt, () -> keybinds.resetToDefault(section, keybind)).width(130f).pad(2f).padLeft(4f);
                    t.row();
                }
            }
        }).height(800f);
    }

    private void openDialog(KeyBinds.Section section, KeyBinds.KeyBind name, boolean act){
        rebindTable.clear();
        rebindTable.add(rebindAxis ? bundle.get("keybind.press.axis") : bundle.get("keybind.press"));
        rebindKey = name;
        rebindDialog.show();

        rebindDialog.clear();
        if(section.device.type() == InputDevice.DeviceType.keyboard){
            rebindDialog.addListener(new InputListener(){
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button){
                    if(Core.app.isAndroid() || rebindTable.hasActions()) return false;
                    rebind(section, name, button);
                    return false;
                }

                @Override
                public boolean keyDown(InputEvent event, KeyCode keycode){
                    if(keycode == KeyCode.escape || rebindTable.hasActions()) return false;
                    rebind(section, name, keycode);
                    return false;
                }

                @Override
                public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY){
                    if(!rebindAxis || rebindTable.hasActions()) return false;
                    rebind(section, name, KeyCode.scroll);
                    return false;
                }
            });
        }

        if(act) rebindTable.actions(
            Actions.moveBy(rebindTable.getWidth(), 0),
            Actions.fadeIn(0),
            Actions.moveBy(-rebindTable.getWidth(), 0f, 0.5f, Interp.sine)
        );

        Time.runTask(30f, () -> getScene().setScrollFocus(rebindDialog));
    }

    private void rebind(KeyBinds.Section section, KeyBinds.KeyBind bind, KeyCode newKey){
        rebindDialog.hide();
        if(rebindKey == null) return;
        boolean isAxis = bind.defaultValue(section.device.type()) instanceof KeyBinds.Axis;

        if(isAxis){
            if(newKey.axis || !rebindMin){
                section.binds.get(section.device.type(), OrderedMap::new).put(rebindKey, newKey.axis ? new KeyBinds.Axis(newKey) : new KeyBinds.Axis(minKey, newKey));
            }
        }else{
            section.binds.get(section.device.type(), OrderedMap::new).put(rebindKey, new KeyBinds.Axis(newKey));
        }

        if(rebindAxis && isAxis && rebindMin && !newKey.axis){
            minKey = newKey;
            openDialog(section, rebindKey, false);
        }else{
            rebindKey = null;
            rebindAxis = false;
            rebindTable.actions(
                Actions.moveBy(rebindTable.getWidth(), 0f, 0.5f, Interp.sine),
                Actions.fadeOut(0f),
                Actions.moveBy(-rebindTable.getWidth(), 0f)
            );
        }
    }
}
