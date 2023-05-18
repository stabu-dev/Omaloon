package ol.type.units;

import mindustry.type.*;
import mindustry.world.meta.*;

import ol.graphics.*;

public class OlUnitType extends UnitType {

    public OlUnitType(String name) {
        super(name);
        outlineColor = OlPal.omaloonOutline;
        envDisabled = Env.space;
        researchCostMultiplier = 10f;
    }
}
