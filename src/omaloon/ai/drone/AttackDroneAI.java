package omaloon.ai.drone;

import mindustry.gen.*;
import omaloon.ai.*;

public class AttackDroneAI extends DroneAI {
    public AttackDroneAI(Unit owner) {
        super(owner);
    }

    @Override
    public void updateMovement() {
        if (!owner.isShooting()) {
            rally();
            return;
        }
        if (!unit.hasWeapons()) return;

        posTeam.set(owner.aimX(), owner.aimY());

        float distanceToTarget = unit.dst(posTeam);
        float distanceToOwner = unit.dst(owner);

        unit.lookAt(posTeam);

        if (distanceToOwner < owner.range()) {
            moveTo(posTeam, unit.type().range * 0.75f, 30f);
            return;
        }

        moveTo(owner, owner.range() * 0.95f, 30f);
        if (distanceToTarget > unit.type().range) {
            unit.lookAt(posTeam);
            unit.controlWeapons(true, true);
        }
    }

    @Override
    public Teamc target(float x, float y, float range, boolean air, boolean ground) {
        return (!owner.isValid() && !owner.isShooting()) ? null : posTeam;
    }

    @Override
    public boolean shouldShoot() {
        return owner.isShooting();
    }
}