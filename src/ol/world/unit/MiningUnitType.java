package ol.world.unit;

import mindustry.ai.UnitCommand;
import mindustry.world.blocks.production.Drill;
import ol.ai.MiningUnitAI;
import ol.entity.MiningUnitEntity;
import ol.type.units.OlUnitType;

public class MiningUnitType extends OlUnitType {
    public Drill placedDrill;

    public MiningUnitType(String name) {
        super(name);
        targetable = false;
        logicControllable = false;
        playerControllable = false;
        aiController = MiningUnitAI::new;
        controller = (ignored) -> aiController.get();
        constructor = MiningUnitEntity::new;
        defaultCommand = UnitCommand.mineCommand;
        isEnemy = false;
    }
}