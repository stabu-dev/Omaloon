package omaloon.ui.dialogs;

import arc.util.*;
import mindustry.gen.*;
import omaloon.content.*;
import omaloon.core.*;

public class CampaignResetDialog extends OlBaseDialog{
    public CampaignResetDialog(){
        super("@dialog.omaloon-reset.title");

        cont.add("@dialog.omaloon-reset")
        .width(500f)
        .wrap()
        .pad(4f)
        .get()
        .setAlignment(Align.center, Align.center);

        fadingButton("@button.omaloon-reset-campaign", Icon.trash, 2f, () -> {
            hide();
            OlCampaignReset.clear(OlPlanets.glasmore);
        });

        fadingButton("@button.omaloon-keep-campaign", Icon.cancel, 1.5f, this::hide);
    }
}
