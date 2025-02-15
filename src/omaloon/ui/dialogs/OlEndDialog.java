package omaloon.ui.dialogs;

import arc.scene.actions.*;
import arc.scene.ui.layout.*;
import mindustry.ui.dialogs.*;

import static mindustry.Vars.mobile;

public class OlEndDialog extends BaseDialog{
    public OlEndDialog(){
        super("");
        titleTable.remove();
        buttons.remove();

        cont.stack(
            new Table(t -> t.add("@ui.omaloon-finished-campaign")),
            new Table(t -> t.add(mobile ? "@ui.omaloon-exit-dialog.mobile" : "@ui.omaloon-exit-dialog")).bottom().left()
        ).grow();

        actions(Actions.fadeOut(1));
        clicked(() -> hide(Actions.sequence(
            Actions.fadeIn(0),
            Actions.fadeOut(1)
        )));
    }
}
