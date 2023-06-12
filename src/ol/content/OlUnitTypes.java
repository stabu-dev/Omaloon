package ol.content;

import me13.core.units.XeonUnits;
import mindustry.ai.types.*;
import mindustry.content.Blocks;
import mindustry.content.UnitTypes;
import mindustry.entities.Units;
import mindustry.gen.*;
import mindustry.type.*;
import ol.content.blocks.OlMiningBlocks;
import ol.entity.MiningUnitEntity;
import ol.type.units.*;
import ol.world.unit.MiningUnitType;

public class OlUnitTypes {
    public static UnitType
            //core units
            discoverer, drillUnit;

    public static void load() {
        XeonUnits.add(MiningUnitEntity.class, MiningUnitEntity::new);
        XeonUnits.setupID();

        discoverer = new OlUnitType("discoverer") {{
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

        drillUnit = new MiningUnitType("unit-drill-unit") {{
            alwaysUnlocked = true;
            itemCapacity = 10;
            flying = true;
            hitSize = 0f;
            engineOffset = 15 / 4f;
            engineSize = 1.5f;
            drag = 0.06f;
            accel = 0.12f;
            speed = 0.5f;
            health = 100;
            range = 50f;
            itemOffsetY = 0;
            shadowElevationScl = 0.2f;
            setEnginesMirror(
                    new UnitEngine(15 / 4f, 0, 1.5f, 360f),
                    new UnitEngine(0, 15 / 4f, 1.5f, 90f)
            );
            hittable = playerControllable = logicControllable = false;
            alwaysCreateOutline = outlines = false;
        }};
    }
}
