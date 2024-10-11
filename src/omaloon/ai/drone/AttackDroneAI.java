package omaloon.ai.drone;

import mindustry.gen.*;
import omaloon.ai.*;

public class AttackDroneAI extends DroneAI {
    public AttackDroneAI(Unit owner) {
        super(owner);
    }

    @Override
    public void updateMovement() {
        if (owner.isShooting()) {
            if (unit.hasWeapons()) {
                posTeam.set(owner.aimX(), owner.aimY());

                moveTo(posTeam, unit.type().range * 0.75f);
                unit.lookAt(posTeam);
            }
        } else {
            rally();
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