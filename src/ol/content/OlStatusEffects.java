package ol.content;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.math.*;

import mindustry.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;

import static mindustry.Vars.*;
import static mindustry.content.StatusEffects.*;

public class OlStatusEffects {
    public static StatusEffect
            slime;

    public static void load(){
        slime = new StatusEffect("slime") {{
            color = Color.valueOf("a8d4ff");

            speedMultiplier = 0.8f;
            buildSpeedMultiplier = 0.8f;

            //effect = OlFx.sticky;
            effectChance = 0.1f;

            init(() -> {
                affinity(shocked, (unit, result, time) -> {
                    unit.damagePierce(transitionDamage);

                    if(unit.team == state.rules.waveTeam){
                        Events.fire(EventType.Trigger.shock);
                    }
                });

                opposite(burning, melting);
            });
        }
            @Override
            public void draw(Unit unit) {
                super.draw(unit);

                Draw.z(Layer.shields);
                Draw.color();
                Draw.mixcol(color, 1f);
                Draw.alpha(Vars.renderer.animateShields ? 0.9f : Mathf.absin(23f, 0.9f * 0.8f));
                Draw.rect(unit.type.shadowRegion, unit.x, unit.y, unit.rotation - 90);
                Draw.reset();
            }
        };
    }
}
