package omaloon.core;

import arc.Core;
import arc.Events;
import arc.KeyBinds;
import arc.util.Time;
import mindustry.Vars;
import mindustry.game.EventType;
import omaloon.OmaloonMod;
import omaloon.ui.StartSplash;
import omaloon.ui.dialogs.OlEndDialog;
import omaloon.ui.dialogs.OlGameDataDialog;
import omaloon.ui.dialogs.OlGameDialog;
import omaloon.ui.dialogs.OlInputDialog;
import omaloon.ui.fragments.CliffFragment;
import omaloon.ui.fragments.ShapedEnvPlacerFragment;

import static arc.Core.settings;


public class OlUI {
    public static ShapedEnvPlacerFragment shapedEnvPlacerFragment;
    public static CliffFragment cliffFragment;
    public static OlInputDialog olInputDialog;
    public static OlGameDataDialog olGameDataDialog;
    public static OlGameDialog olGameDialog;
    public static OlEndDialog olEndDialog;

    public OlUI(KeyBinds.KeyBind... binds) {
        setKeybinds(binds);


        Events.on(EventType.ClientLoadEvent.class,it->onClient());
    }
    protected void onClient(){
        StartSplash.build(Vars.ui.menuGroup);
        StartSplash.show();
        if (Vars.mobile) return;

        shapedEnvPlacerFragment.build(Vars.ui.hudGroup);
        cliffFragment.build(Vars.ui.hudGroup);

        shapedEnvPlacerFragment = new ShapedEnvPlacerFragment();
        cliffFragment = new CliffFragment();
        olInputDialog = new OlInputDialog();
        olGameDataDialog = new OlGameDataDialog();
        olGameDialog = new OlGameDialog();
        olEndDialog = new OlEndDialog();
    }

    /**
     * @author Zelaux
     * <a href="https://github.com/Zelaux/MindustryModCore/blob/v2/core/src/mmc/core/ModUI.java#L33">source</a>
     * */
    protected void setKeybinds(KeyBinds.KeyBind... modBindings){
        Time.mark();
        KeyBinds.KeyBind[] keyBinds = Core.keybinds.getKeybinds();
        KeyBinds.KeyBind[] defs = new KeyBinds.KeyBind[keyBinds.length + modBindings.length];
        for (int i = 0; i < defs.length; i++) {
            if (i<keyBinds.length){
                defs[i]=keyBinds[i];
            } else {
                defs[i]= modBindings[i-keyBinds.length];
            }
        }
        OmaloonMod.olLog("Time to combine arrays: @ms",Time.elapsed());
        Core.keybinds.setDefaults(defs);
        settings.load();
    }
}
