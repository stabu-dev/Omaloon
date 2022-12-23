package ol.content;

import arc.*;
import arc.graphics.*;

import mindustry.game.EventType.*;
import mindustry.type.*;

import ol.type.*;

import static mindustry.Vars.*;
import static mindustry.content.StatusEffects.*;

public class OlStatusEffects {
    public static StatusEffect
            slime;

    public static void load(){
        slime = new OlSlimeStatus("slime") {{
            color = Color.valueOf("a8d4ff");

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
