package ol.ui;

import arc.*;
import mindustry.*;

/**
 * Omaloon mod settings
 */
public class OlSettings{
    public static void init(){
        boolean tmp = Core.settings.getBool("ui-rechallenged", false);
        Core.settings.put("ui-rechallenged", false);
        Core.settings.put("ui-rechallenged", tmp);
    }
}