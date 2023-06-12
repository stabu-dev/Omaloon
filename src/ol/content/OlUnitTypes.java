package ol.content;

import mindustry.ai.types.*;
import mindustry.gen.*;
import mindustry.type.*;
import ol.type.units.*;

public class OlUnitTypes {
    public static UnitType
            //core units
            discoverer;

    public static void load(){
        discoverer = new OlUnitType("discoverer"){{
            aiController = BuilderAI::new;
            constructor = UnitEntity::create;
            isEnemy = false;

            lowAltitude = true;
            flying = true;
            mineSpeed = 4.5f;
            mineTier = 1;
            buildSpeed = 0.3f;
            drag = 0.03f;
            speed = 2f;
            rotateSpeed = 13f;
            accel = 0.1f;
            itemCapacity = 20;
            health = 110f;
            engineOffset = 6f;
            hitSize = 8f;
            alwaysUnlocked = true;
        }};
    }
}
