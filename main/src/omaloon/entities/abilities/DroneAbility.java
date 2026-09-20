package omaloon.entities.abilities;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.event.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.meta.*;
import omaloon.ai.*;

/** Spawns and tracks a single drone unit bound to the parent. */
public class DroneAbility extends Ability {
    public String name = "omaloon-drone";
    public UnitType droneUnit;
    public float spawnTime = 60f;
    public float spawnX = 0f;
    public float spawnY = 0f;
    public float idleX = 0f;
    public float idleY = 0f;
    public Effect spawnEffect = Fx.spawn;
    public boolean parentizeEffects = false;
    public float layer = Layer.groundUnit - 0.01f;
    public float rotation = 0f;

    protected float timer = 0f;
    protected int abilityIndex = -1;
    protected float lastData = -1;
    protected boolean initialized = false;

    public DroneAbility(UnitType droneUnit){
        this.droneUnit = droneUnit;
    }

    @Override
    public void display(Table t) {
        t.table(Styles.grayPanel, a -> {
            a.left();
            a.image(droneUnit.fullIcon).with(image -> {
                image.addListener(new HandCursorListener());
                image.touchable = Touchable.enabled;
                image.clicked(() -> Vars.ui.content.show(droneUnit));
            }).padRight(10);
            a.table(stats -> {
                stats.add("[accent]" + (Core.bundle.has(getBundle()) ? localized() : droneUnit.localizedName)).left().row();
                stats.add("[lightgray]" + Stat.productionTime.localized() + ": []" + Strings.autoFixed(spawnTime, 2)).left().row();
            });
        }).pad(5).margin(10).growX().top().uniformX();
    }

    @Override
    public void draw(Unit unit){
        if(getDrone(unit) != null) return;

        Draw.draw(layer, () -> Drawf.construct(
                Angles.trnsx(unit.rotation - 90f, spawnX, spawnY) + unit.x,
                Angles.trnsy(unit.rotation - 90f, spawnX, spawnY) + unit.y,
                droneUnit.fullIcon,
                unit.rotation - 90,
                Mathf.clamp(timer / spawnTime),
                1f,
                timer
        ));
    }

    /** Resolves the drone by id, rejecting stale links. */
    public @Nullable Unit getDrone(Unit unit) {
        Unit drone = Groups.unit.getByID((int) data);

        if(drone == null || !drone.isValid() || drone.type() != droneUnit || drone.team() != unit.team()) return null;
        return drone;
    }

    /** Finds the live drone claimed by this unit and slot. */
    public @Nullable Unit findDrone(Unit unit){
        for(Unit u : Groups.unit){
            if(!u.isValid() || u.type() != droneUnit || u.team() != unit.team()) continue;
            if(u.id() == (int)data) return u;
            if(u.controller() instanceof DroneAI ai && ai.linkedTo(unit, abilityIndex)) return u;
        }
        return null;
    }

    @Override
    public void init(UnitType type){
        data = -1;
        abilityIndex = type.abilities.indexOf(this);
    }

    @Override
    public String getBundle(){
        return "ability." + name;
    }

    @Override
    public void update(Unit unit){
        Unit drone = getDrone(unit);

        // Id lookup missed: rescan before assuming the drone is gone.
        if(drone == null){
            drone = findDrone(unit);
            if(drone != null) data = drone.id;
        }

        if(lastData != data) {
            if(data != -1 && initialized) {
                float sX = Angles.trnsx(unit.rotation - 90f, spawnX, spawnY) + unit.x;
                float sY = Angles.trnsy(unit.rotation - 90f, spawnX, spawnY) + unit.y;

                if(spawnEffect != Fx.none) {
                    spawnEffect.at(sX, sY, 0f, droneUnit);
                }
            }
            lastData = data;
        }

        initialized = true;

        if (drone == null) {
            timer += Time.delta * Vars.state.rules.unitBuildSpeed(unit.team());
        } else timer = 0;

        if(!Vars.net.client() && timer > spawnTime) {
            // Revalidate by scan so a stale link can never stack drones.
            Unit live = findDrone(unit);
            if(live != null){
                data = live.id;
                timer = 0;
            }else if(Units.canCreate(unit.team, droneUnit)){
                float sX = Angles.trnsx(unit.rotation - 90f, spawnX, spawnY) + unit.x;
                float sY = Angles.trnsy(unit.rotation - 90f, spawnX, spawnY) + unit.y;

                Unit newDrone = droneUnit.create(unit.team());
                newDrone.set(sX, sY);
                newDrone.rotation = unit.rotation + rotation;
                if(newDrone.controller() instanceof DroneAI ai) ai.link(unit, abilityIndex);
                newDrone.add();
                timer = 0;
                data = newDrone.id;
            }
        }
    }
}
