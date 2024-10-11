package omaloon.entities.abilities;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import omaloon.ai.*;
import omaloon.gen.*;

import java.util.*;
import java.util.function.*;

public class DroneAbility extends Ability {
    private static Unit paramUnit;
    private static DroneAbility paramAbility;
    private static final Vec2 paramPos = new Vec2();

    public UnitType drone;
    public float constructTime = 60f;
    public float spawnX = 0f;
    public float spawnY = 0f;
    public Effect spawnEffect = Fx.spawn;
    public boolean parentizeEffects = false;
    public Vec2[] rallyPos = {new Vec2(5 * 8f, -5 * 8f)};
    public float layer = Layer.groundUnit - 0.01f;
    public float rotation = 0f;
    public int maxDroneCount = 2;
    protected float timer = 0f;
    protected ArrayList<Unit> drones = new ArrayList<>();
    public Function<Unit, DroneAI> ai = DroneAI::new;

    /** Using data as current drone count for consistency with ability system */
    @Override
    public void init(UnitType type) {
        data = 0;
    }

    public DroneAbility(){
    }

    @Override
    public Ability copy() {
        DroneAbility ability = (DroneAbility) super.copy();
        ability.drones = new ArrayList<>();
        return ability;
    }

    @Override
    public void update(Unit unit) {
        paramUnit = unit;
        paramAbility = this;
        paramPos.set(spawnX, spawnY).rotate(unit.rotation - 90f).add(unit);

        timer += Time.delta * Vars.state.rules.unitBuildSpeed(unit.team());

        if (drones.isEmpty()) {
            for (Unit u : Groups.unit) {
                if (u.team() == unit.team() && u instanceof DroneUnit && ((DroneUnit) u).owner == unit) {
                    drones.add(u);
                    u.controller(ai.apply(unit));
                    data++;
                    updateRally();
                }
            }
        }

        drones.removeIf(u -> {
            if (!u.isValid()) {
                data--;
                return true;
            }
            return false;
        });

        if (data < maxDroneCount) {
            if (timer > constructTime) {
                spawnDrone();
                timer = 0;
            }
        }
    }

    protected void spawnDrone() {
        spawnEffect.at(paramPos.x, paramPos.y, 0f, parentizeEffects ? paramUnit : null);
        Unit u = drone.create(paramUnit.team());
        u.set(paramPos.x, paramPos.y);
        u.rotation = paramUnit.rotation + rotation;

        if (u instanceof DroneUnit droneUnit) droneUnit.owner(paramUnit);

        drones.add(0, u);
        data++;
        u.controller(ai.apply(paramUnit));
        updateRally();

        Events.fire(new UnitCreateEvent(u, null, paramUnit));
        if (!Vars.net.client()) {
            u.add();
        }
    }

    public void updateRally() {
        for (int i = 0; i < drones.size(); i++) {
            Unit u = drones.get(i);
            ((DroneAI) u.controller()).rally(rallyPos[i]);
        }
    }

    @Override
    public void draw(Unit unit){
        paramUnit = unit;
        paramAbility = this;
        paramPos.set(spawnX, spawnY).rotate(unit.rotation - 90f).add(unit);

        if(data < maxDroneCount && timer <= constructTime){
            Draw.draw(layer, () -> {
                Drawf.construct(paramPos.x, paramPos.y, drone.fullIcon, paramUnit.rotation - 90, timer / constructTime, 1f, timer);
            });
        }
    }
}