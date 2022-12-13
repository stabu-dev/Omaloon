package ol.type;

import arc.graphics.g2d.*;
import arc.math.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;

/**
 * Draws an imitation of mucus
 */
public class OlSlimeStatus extends StatusEffect {
    public float transparency = 0.9f;
    public float layer = Layer.shields;
    public OlSlimeStatus(String name){
        super(name);
    }

    @Override
    public void draw(Unit unit){
        super.draw(unit);
        Draw.z(layer);
        Draw.color();
        Draw.mixcol(color, 1f);
        Draw.alpha(Vars.renderer.animateShields ? transparency : Mathf.absin(23f, transparency * 0.8f));
        Draw.rect(unit.type.shadowRegion, unit.x, unit.y, unit.rotation - 90);
        Draw.reset();
    }
}