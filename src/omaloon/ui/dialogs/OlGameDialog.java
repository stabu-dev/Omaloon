package omaloon.ui.dialogs;

import arc.*;
import arc.func.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;

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
            "@setting.omaloon-shield-opacity", null, "omaloon-shield-opacity",
            new Slider(0, 100, 1, false), 20,
            f -> Strings.autoFixed(f, 20) + "%"
        );

        addCheck(
            "@setting.omaloon-show-disclaimer", null, "omaloon-show-disclaimer",
            false, b -> {
            }
        );
        addCheck(
            "@setting.omaloon-enable-soft-cleaner", "@setting.omaloon-enable-soft-cleaner.description", "omaloon-enable-soft-cleaner",
            true, b -> {
            }
        );
        addCheck(
            "@setting.omaloon-check-updates", null, "omaloon-check-updates",
            true, b -> {
            }
        );

        addCheck(
            "@setting.omaloon-display-liquid-stats", "@setting.omaloon-display-liquid-stats.description", "omaloon-display-liquid-stats",
            true, b -> {
            }
        );

        cont.button("@settings.reset", () -> {
            resetToDefaults();
            rebuild();
        }).size(250, 50);
    }

    public void resetToDefaults(){
        settings.each((name, value) -> Core.settings.put(name, value));
    }
}
