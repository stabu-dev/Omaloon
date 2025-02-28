package omaloon.ui.dialogs;

import mindustry.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;
import omaloon.content.*;
import omaloon.core.settings.*;

public class OlGameDataDialog extends BaseDialog{
    public OlGameDataDialog(){
        super("@settings.omaloon-moddata");

        addCloseButton();

        cont.table(Tex.button, cat -> {
            cat.button(
                "@settings.clearresearch",
                Icon.trash,
                Styles.flatt,
                Vars.iconMed,
                () -> Vars.ui.showConfirm("@settings.omaloon-clearresearch-confirm", () -> DataBase.resetTree(OlPlanets.glasmore.techTree))
            ).growX().marginLeft(8).height(50).row();
            cat.button(
                "@settings.clearcampaignsaves",
                Icon.trash,
                Styles.flatt,
                Vars.iconMed,
                () -> Vars.ui.showConfirm("@settings.omaloon-clearcampaignsaves-confirm", () -> DataBase.resetSaves(OlPlanets.glasmore))
            ).growX().marginLeft(8).height(50).row();
            cat.button(
                "@settings.omaloon-resethints",
                Icon.trash,
                Styles.flatt,
                Vars.iconMed,
                () -> Vars.ui.showConfirm("@settings.omaloon-resethints-confirm", EventHints::reset)
            ).growX().marginLeft(8).height(50).row();
        }).width(400f).row();
    }
}
