package omaloon.content;

import arc.*;
import mindustry.core.GameState.*;
import mindustry.game.*;
import mindustry.graphics.*;
import mindustry.type.*;

import static arc.graphics.Color.*;
import static mindustry.Vars.*;
import static mindustry.content.Liquids.*;
import static mindustry.content.StatusEffects.*;
import static omaloon.content.OlLiquids.*;

public class OlStatusEffects{
    public static StatusEffect
    glacied, breeze,
    filledWithWater, filledWithGlacium, filledWithSlag, filledWithOil;

    public static void load(){
        glacied = new StatusEffect("glacied"){{
            color = valueOf("5195ab");

            speedMultiplier = 0.8f;
            buildSpeedMultiplier = 0.8f;

            effect = OlFx.glacied.layer(Layer.debris);
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
        }};

        breeze = new StatusEffect("wind-breeze"){{
            color = valueOf("ffffff");
            speedMultiplier = 1.2f;
        }};

        filledWithWater = new StatusEffect("filled-with-water"){{
            color = water.color;
        }
            @Override
            public boolean isHidden(){
                return state.getState() != State.menu;
            }
        };

        filledWithGlacium = new StatusEffect("filled-with-glacium"){{
            color = glacium.color;
        }
            @Override
            public boolean isHidden(){
                return state.getState() != State.menu;
            }
        };

        filledWithSlag = new StatusEffect("filled-with-slag"){{
            color = slag.color;
        }
            @Override
            public boolean isHidden(){
                return state.getState() != State.menu;
            }
        };

        filledWithOil = new StatusEffect("filled-with-oil"){{
            color = oil.color;
        }
            @Override
            public boolean isHidden(){
                return state.getState() != State.menu;
            }
        };
    }
}
