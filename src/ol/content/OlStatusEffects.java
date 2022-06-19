package ol.content;

import arc.*;
import mindustry.game.EventType.*;
import mindustry.type.*;
import ol.graphics.OlPal;
import ol.type.OlSlimeStatus;

import static mindustry.Vars.*;
import static mindustry.content.StatusEffects.*;

public class OlStatusEffects {
    public static StatusEffect slime;

    public static void load(){
        slime = new OlSlimeStatus("slime"){{
            color = OlPal.OLDalanite;
            speedMultiplier = 0.8f;
            buildSpeedMultiplier = 0.8f;
            effect = OlFx.sticky;
            effectChance = 0.1f;
            init(() -> {
                affinity(shocked, (unit, result, time) -> {
                    unit.damagePierce(transitionDamage);
                    if(unit.team == state.rules.waveTeam){
                        Events.fire(Trigger.shock);
                    }
                });
                opposite(burning, melting);
            });
        }};
    }
}
