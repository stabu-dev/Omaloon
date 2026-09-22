package omaloon.ai;

import arc.math.geom.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.abilities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import omaloon.entities.abilities.*;

/** Parent link and lifecycle for ability-spawned drones. */
public abstract class DroneAI extends AIController{
    private static final float adoptTime = 120f;

    protected Unit parent;
    protected final Vec2 targetPos = new Vec2();
    protected float parentlessTime = 0f;

    public void link(Unit parent){
        this.parent = parent;
        parentlessTime = 0f;
    }

    public boolean linkedTo(Unit parent){
        return parent != null && this.parent == parent;
    }

    public boolean adoptable(){
        return parent == null;
    }

    protected @Nullable DroneAbility droneAbility(){
        if(parent == null) return null;
        for(Ability ability : parent.abilities){
            if(ability instanceof DroneAbility drone && drone.droneUnit == unit.type) return drone;
        }
        return null;
    }

    protected boolean droneGone(){
        if(parent != null && parent.dead()){
            Call.unitDestroy(unit.id());
            return true;
        }

        if(parent != null && !parent.isValid()){
            Fx.spawn.at(unit.x, unit.y, 0f, unit.type());
            Call.unitDespawn(unit);
            return true;
        }

        if(parent == null){
            parentlessTime += Time.delta;
            if(parentlessTime > adoptTime){
                Fx.spawn.at(unit.x, unit.y, 0f, unit.type());
                Call.unitDespawn(unit);
                return true;
            }
        }

        return false;
    }

    @Override
    public void updateUnit(){
        if(droneGone()) return;

        if(parent == null){
            stopShooting();
            updateVisuals();
            return;
        }

        super.updateUnit();
    }

    public void updateIdle(){
        DroneAbility ability = droneAbility();
        if(ability == null) return;

        moveTo(targetPos.trns(parent.rotation - 90f, ability.idleX, ability.idleY).add(parent), 2f, 20);
        if(unit.within(targetPos, unit.hitSize)){
            unit.lookAt(parent.rotation);
        }else{
            unit.lookAt(targetPos);
        }
    }
}
