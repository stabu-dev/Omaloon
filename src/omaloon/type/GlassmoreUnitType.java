package omaloon.type;

import arc.graphics.*;
import mindustry.content.Fx;
import mindustry.entities.effect.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.type.ammo.*;
import mindustry.world.meta.*;
import omaloon.content.*;
import omaloon.entities.abilities.*;

public class GlassmoreUnitType extends UnitType {

    public GlassmoreUnitType(String name) {
        super(name);
        outlineColor = Color.valueOf("2f2f36");
        envDisabled = Env.space;
        ammoType = new ItemAmmoType(OlItems.cobalt);
        researchCostMultiplier = 8f;

        abilities.add(
                new HailShieldAbility() {{
                    regen = 0.001f;
                    regenBroken = 0.05f;
                    layerOffset = 1f;
                    breakEffect = new WrapEffect(Fx.unitShieldBreak, Pal.heal, 1f);
                }}
        );
    }
}
