package omaloon.core;

import arc.*;
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


public class OlUI implements ApplicationListener{
    public static ShapedEnvPlacerFragment shapedEnvPlacerFragment;
    public static CliffFragment cliffFragment;
    public static OlInputDialog olInputDialog;
    public static OlGameDataDialog olGameDataDialog;
    public static OlGameDialog olGameDialog;
    public static OlEndDialog olEndDialog;

    public OlUI() {
        Events.on(EventType.ClientLoadEvent.class,it->onClient());
    }
    protected void onClient(){
        StartSplash.build(Vars.ui.menuGroup);
        StartSplash.show();
        if (Vars.mobile) return;

        shapedEnvPlacerFragment = new ShapedEnvPlacerFragment();
        cliffFragment = new CliffFragment();
        olInputDialog = new OlInputDialog();
        olGameDataDialog = new OlGameDataDialog();
        olGameDialog = new OlGameDialog();
        olEndDialog = new OlEndDialog();

        shapedEnvPlacerFragment.build(Vars.ui.hudGroup);
        cliffFragment.build(Vars.ui.hudGroup);
    }

}
