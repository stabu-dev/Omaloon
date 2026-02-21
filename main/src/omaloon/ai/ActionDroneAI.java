package omaloon.ai;

import arc.math.geom.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.world.*;
import omaloon.entities.abilities.*;
import omaloon.gen.*;

public class ActionDroneAI extends AIController {
    protected Unit parent;
    protected Vec2 targetPos = new Vec2();
    protected Tile mineTile;

    public void updateBuilding() {

    }

    public void updateIdle() {
        DroneAbility ability = (DroneAbility) parent.abilities[((DroneTetherc) unit).abilityIndex()];

        moveTo(targetPos.trns(parent.rotation - 90f, ability.idleX, ability.idleY).add(parent), 2f, 20);
        if (unit.within(targetPos, unit.hitSize)) {
            unit.lookAt(parent.rotation);
        } else unit.lookAt(targetPos);
    }

    // TODO you can make the item transform into another, fix needed
    public void updateMining() {
        if (parent.mineTile != null) {
            mineTile = parent.mineTile == mineTile ? null : parent.mineTile;
            parent.mineTile = null;
        }
        if (mineTile != null && !parent.within(mineTile.worldx(), mineTile.worldy(), parent.type.mineRange)) mineTile = null;
        if (mineTile != null && unit.within(mineTile.worldx(), mineTile.worldy(), unit.type.mineRange)) unit.mineTile = mineTile;
        if (
            mineTile == null ||
            parent.stack.amount >= parent.type.itemCapacity
        ) {
            mineTile = unit.mineTile = null;
        }
        if (unit.stack.amount > 0) {
            for (int i = 0; i < unit.stack.amount; i++) {
                if (parent.stack.amount >= parent.type.itemCapacity) break;
                Call.transferItemToUnit(unit.stack.item, unit.x, unit.y, parent);
            }
            unit.stack.amount = 0;
        }
    }

    @Override
    public void updateMovement() {
        if (mineTile != null) {
            moveTo(targetPos.set(mineTile.worldx(), mineTile.worldy()), unit.type.mineRange - unit.hitSize, 50);
        } else {
            updateIdle();
        }
    }

    @Override
    public void updateUnit() {
        if (unit instanceof DroneTetherc drone) parent = drone.parent();
        super.updateUnit();
    }

    @Override
    public void updateTargeting() {
        updateMining();
        updateBuilding();
    }
}
