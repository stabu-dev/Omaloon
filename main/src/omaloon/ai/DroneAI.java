package omaloon.ai;

import arc.math.geom.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.abilities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import omaloon.entities.abilities.*;

/** Shared parent-link and lifecycle for ability-spawned drones. */
public abstract class DroneAI extends AIController{
    protected Unit parent;
    protected int abilityIndex = -1;
    protected final Vec2 targetPos = new Vec2();

    public boolean hasParent(){
        return parent != null && parent.isValid();
    }

    public void link(Unit parent, int abilityIndex){
        this.parent = parent;
        this.abilityIndex = abilityIndex;
    }

    public boolean linkedTo(Unit parent, int abilityIndex){
        return hasParent() && this.parent == parent && this.abilityIndex == abilityIndex;
    }

    protected @Nullable DroneAbility droneAbility(){
        if(!hasParent() || abilityIndex < 0 || parent.abilities == null || abilityIndex >= parent.abilities.length) return null;
        return parent.abilities[abilityIndex] instanceof DroneAbility ability ? ability : null;
    }

    protected boolean linkParent(){
        if(hasParent()) return true;
        parent = null;
        abilityIndex = -1;

        for(Unit other : Groups.unit){
            if(!other.isValid() || other.team() != unit.team() || other.abilities == null) continue;
            for(int i = 0; i < other.abilities.length; i++){
                if(other.abilities[i] instanceof DroneAbility ability && ability.droneUnit == unit.type() && (int)ability.data == unit.id()){
                    parent = other;
                    abilityIndex = i;
                    return true;
                }
            }
        }
        return false;
    }

    /** @return true when per-frame logic must stop. */
    protected boolean droneGone(){
        if(!linkParent()){
            Fx.spawn.at(unit.x, unit.y, 0f, unit.type());
            Call.unitDespawn(unit);
            return true;
        }

        if(parent.dead()){
            Call.unitDestroy(unit.id());
            return true;
        }

        if(parent.team() != unit.team()) unit.team(parent.team());
        return false;
    }

    /** Idle positioning around the parent. */
    public void updateIdle(){
        DroneAbility ability = droneAbility();
        if(ability == null) return;

        moveTo(targetPos.trns(parent.rotation - 90f, ability.idleX, ability.idleY).add(parent), 2f, 20);
        if(unit.within(targetPos, unit.hitSize)){
            unit.lookAt(parent.rotation);
        }else unit.lookAt(targetPos);
    }
}
