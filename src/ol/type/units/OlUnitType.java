package ol.type.units;

import me13.core.units.XeonUnitType;
import mindustry.world.meta.*;

import ol.graphics.*;

public class OlUnitType extends XeonUnitType {
    public OlUnitType(String name) {
        super(name);
        outlineColor = OlPal.omaloonOutline;
        envDisabled = Env.space;
        researchCostMultiplier = 10f;
    }
}
