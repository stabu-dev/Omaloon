package omaloon.ui.dialogs;

import arc.*;
import arc.func.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arclibrary.settings.other.*;
import mindustry.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;
import omaloon.content.*;

import static omaloon.core.OlSettings.*;

public class OlGameDialog extends BaseDialog{
    public final ObjectMap<String, Object> settings = new ObjectMap<>();

    public OlGameDialog(){
        super("@settings.game");

        addCloseButton();

        rebuild();
    }

    public void addCheck(String name, @Nullable String description, String setting, boolean def, Boolc cons){
        CheckBox box = new CheckBox(name);
        box.update(() -> box.setChecked(Core.settings.getBool(setting, def)));
        box.changed(() -> {
            Core.settings.put(setting, box.isChecked());
            cons.get(box.isChecked());
        });
        box.left();
        cont.add(box).left().padTop(3f).get();
        if(description != null) Vars.ui.addDescTooltip(box, description);
        settings.put(setting, def);
        cont.row();
    }

    public void addSlider(String name, @Nullable String description, String setting, Slider slider, float def, Func<Float, String> sp){
        slider.setValue(Core.settings.getFloat(setting, def));
        Label value = new Label("", Styles.outlineLabel);
        Table content = new Table();
        content.add(name, Styles.outlineLabel).left().growX().wrap();
        content.add(value).padLeft(10f).right();
        content.margin(3f, 33f, 3f, 33f);
        content.touchable = Touchable.disabled;
        slider.changed(() -> {
            Core.settings.put(setting, slider.getValue());
            value.setText(sp.get(slider.getValue()));
        });
        slider.change();
        Stack added = cont.stack(slider, content).width(Math.min(Core.graphics.getWidth() / 1.2f, 460f)).left().padTop(4f).get();
        if(description != null) Vars.ui.addDescTooltip(added, description);
        settings.put(setting, def);
        cont.row();
    }

    public void rebuild(){
        cont.clear();

        addSlider(
            "@setting.omaloon-shield-opacity", null, shieldOpacity.key,
            new Slider(0, 100, 1, false), shieldOpacity.def(),
            f -> Strings.autoFixed(f, 20) + "%"
        );

        addCheck(showDisclaimer,false, b -> {});
        addCheck(enableSoftCleaner,true, b -> {});
        addCheck(checkUpdates,false,b->{});
        addCheck(displayLiquidStats,true, OlLiquids::changeDisplayLiquidStats);

        cont.button("@settings.reset", () -> {
            resetToDefaults();
            rebuild();
        }).size(250, 50);
    }

    private void addCheck(BooleanSettingKey key, boolean hasDesc, Boolc boolc){
        String bundle = "@setting." + key.key;
        addCheck(
            bundle, hasDesc ? bundle + ".description" : null, key.key,
            key.def(), boolc
        );
    }

    public void resetToDefaults(){
        settings.each((name, value) -> Core.settings.put(name, value));
    }
}
