package omaloon.type.liquid;

import arc.graphics.*;
import mindustry.type.*;
import mindustry.type.ammo.*;
import mindustry.world.meta.*;
import omaloon.content.*;

public class GlasmoreUnitType extends UnitType {
    public GlasmoreUnitType(String name) {
        super(name);
        outlineColor = Color.valueOf("2f2f36");
        envDisabled = Env.space;
        ammoType = new ItemAmmoType(OlItems.cobalt);
        researchCostMultiplier = 8f;
    }
}
