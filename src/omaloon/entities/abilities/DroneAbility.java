package omaloon.entities.abilities;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
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
import omaloon.gen.DroneUnit;

import java.util.*;
import java.util.function.*;

public class DroneAbility extends Ability {
    public UnitType drone;
    public float constructTime = 60f;
    public float spawnX = 0f;
    public float spawnY = 0f;
    public Effect spawnEffect = Fx.spawn;
    public boolean parentizeEffects = false;
    public Vec2[] rallyPos = {new Vec2(5 * 8f, -5 * 8f)};
    public float layer = Layer.groundUnit - 0.01f;
    public float rotation = 0f;
    public int droneCount = 2;
    protected float timer = 0f;
    protected ArrayList<Unit> drones = new ArrayList<>();
    public Function<Unit, DroneAI> ai = DroneAI::new;

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
        timer += Time.delta * Vars.state.rules.unitBuildSpeed(unit.team());

        if (drones.isEmpty()) {
            for (Unit u : Groups.unit) {
                if (u.team() == unit.team() && u instanceof DroneUnit && ((DroneUnit) u).owner == unit) {
                    drones.add(u);
                    u.controller(ai.apply(unit));
                    updateRally();
                }
            }
        }

        drones.removeIf(u -> !u.isValid());

        if (drones.size() < droneCount) {
            if (timer > constructTime) {
                float x = unit.x + Angles.trnsx(unit.rotation(), spawnY, spawnX), y = unit.y + Angles.trnsy(unit.rotation(), spawnY, spawnX);
                spawnEffect.at(x, y, 0f, parentizeEffects ? unit : null);
                Unit u = this.drone.create(unit.team());
                u.set(x, y);
                u.rotation = unit.rotation() + rotation;

                if (u instanceof DroneUnit droneUnit) droneUnit.owner(unit);

                drones.add(0, u);
                u.controller(ai.apply(unit));
                updateRally();

                Events.fire(new UnitCreateEvent(u, null, unit));
                if (!Vars.net.client()){
                    u.add();
                }

                timer = 0;
            }
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
        if(drones.size() < droneCount && timer <= constructTime){
            Draw.draw(layer, () -> {
                float x = unit.x + Angles.trnsx(unit.rotation, spawnY, spawnX), y = unit.y + Angles.trnsy(unit.rotation, spawnY, spawnX);
                Drawf.construct(x, y, this.drone.fullIcon, unit.rotation - 90, timer / constructTime, 1f, timer);
            });
        }
    }
}
