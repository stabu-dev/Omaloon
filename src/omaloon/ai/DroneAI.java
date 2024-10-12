package omaloon.ai;

import arc.math.geom.*;
import arc.util.*;
import mindustry.entities.units.*;
import mindustry.gen.*;

public class DroneAI extends AIController {
    protected Unit owner;
    protected Vec2 rallyPos;
    protected PosTeam posTeam;

    public DroneAI(Unit owner) {
        this.owner = owner;
        this.rallyPos = new Vec2();
        this.posTeam = PosTeam.create();
    }

    @Override
    public void updateUnit() {
        if (!owner.isValid()) {
            Call.unitDespawn(unit);
            return;
        }
        super.updateUnit();
    }

    @Override
    public void updateMovement() {
        rally();
    }

    public void rally(Vec2 pos) {
        rallyPos = pos;
    }

    public void rally() {
        Tmp.v2.set(owner.x, owner.y);
        Vec2 targetPos = Tmp.v1.set(rallyPos).add(Tmp.v2).rotateAround(Tmp.v2, owner.rotation - 90);

        float distance = unit.dst(targetPos);

        moveTo(targetPos, 2f, 30f);

        if (distance > 5f) {
            unit.lookAt(targetPos.x, targetPos.y);
        } else {
            unit.lookAt(owner.rotation());
        }
    }
}
