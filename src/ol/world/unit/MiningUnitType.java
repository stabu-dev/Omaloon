package ol.world.unit;

import arc.graphics.g2d.Draw;
import mindustry.ai.UnitCommand;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
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

    @Override
    public void drawBody(Unit unit){
        applyColor(unit);

        Drawf.spinSprite(region, unit.x, unit.y, unit.rotation - 90);

        Draw.reset();
    }

    @Override
    public void drawCell(Unit unit){
        applyColor(unit);

        Draw.color(cellColor(unit));
        Drawf.spinSprite(cellRegion, unit.x, unit.y, unit.rotation - 90);
        Draw.reset();
    }
}