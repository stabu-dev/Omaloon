package omaloon.content;

import arc.struct.*;
import ent.anno.Annotations.*;
import mindustry.ai.types.*;
import mindustry.content.Items;
import mindustry.gen.*;
import mindustry.type.*;
import omaloon.type.liquid.*;

public class OlUnitTypes {
    public static @EntityDef(value = {Unitc.class}) UnitType discovery;

    public static void load(){
        discovery = new GlasmoreUnitType("discovery"){{
            controller = u -> new BuilderAI(true, 500f);
            constructor = UnitEntity::create;
            isEnemy = false;

            lowAltitude = true;
            flying = true;
            mineSpeed = 4.5f;
            mineTier = 3;
            mineItems = Seq.with(OlItems.cobalt, Items.beryllium);
            buildSpeed = 0.3f;
            drag = 0.03f;
            speed = 2f;
            rotateSpeed = 13f;
            accel = 0.1f;
            itemCapacity = 20;
            health = 110f;
            engineOffset = 5f;
            hitSize = 8f;
            alwaysUnlocked = true;
        }};
    }
}
