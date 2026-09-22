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
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.meta.*;
import omaloon.ai.*;

/** Spawns and tracks a single drone unit bound to the parent. */
public class DroneAbility extends Ability{
    public String name = "omaloon-drone";
    public UnitType droneUnit;
    public float spawnTime = 60f;
    public float spawnX = 0f;
    public float spawnY = 0f;
    public float idleX = 0f;
    public float idleY = 0f;
    public Effect spawnEffect = Fx.spawn;
    public float layer = Layer.groundUnit - 0.01f;
    public float rotation = 0f;

    protected transient float timer = 0f;
    protected transient @Nullable Unit droneRef;

    public DroneAbility(UnitType droneUnit){
        this.droneUnit = droneUnit;
    }

    @Override
    public void display(Table t){
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

    /** Finds the drone by mirrored id. Display only, never use for spawning. */
    public @Nullable Unit getDrone(Unit unit){
        Unit drone = Groups.unit.getByID((int)data);

        if(drone == null || !drone.isValid() || drone.type() != droneUnit || drone.team() != unit.team()) return null;
        return drone;
    }

    /** Reclaims a bound drone or adopts a stray. */
    protected @Nullable Unit recover(Unit unit){
        Unit stray = null;
        for(Unit other : Groups.unit){
            if(other.type() != droneUnit || other.team() != unit.team() || !other.isValid()) continue;
            if(other.controller() instanceof DroneAI ai){
                if(ai.linkedTo(unit)) return other;
                if(stray == null && ai.adoptable()) stray = other;
            }
        }
        if(stray != null) ((DroneAI)stray.controller()).link(unit);
        return stray;
    }

    @Override
    public void init(UnitType type){
        data = -1;
    }

    @Override
    public String getBundle(){
        return "ability." + name;
    }

    @Override
    public void update(Unit unit){
        if(Vars.net.client()){
            if(data == -1) timer += Time.delta * Vars.state.rules.unitBuildSpeed(unit.team());
            else timer = 0f;
            return;
        }

        Unit drone = droneRef;
        if(drone != null && drone.isValid() && drone.controller() instanceof DroneAI ai && ai.linkedTo(unit)){
            data = drone.id();
            timer = 0f;
            return;
        }

        droneRef = null;
        data = -1;

        if(timer <= 0f){
            drone = recover(unit);
            if(drone != null){
                droneRef = drone;
                data = drone.id();
                return;
            }
        }

        timer += Time.delta * Vars.state.rules.unitBuildSpeed(unit.team());
        if(timer < spawnTime || !Units.canCreate(unit.team(), droneUnit)) return;

        spawn(unit);
    }

    protected void spawn(Unit unit){
        float sX = Angles.trnsx(unit.rotation - 90f, spawnX, spawnY) + unit.x;
        float sY = Angles.trnsy(unit.rotation - 90f, spawnX, spawnY) + unit.y;

        Unit out = droneUnit.create(unit.team());
        out.set(sX, sY);
        out.rotation = unit.rotation + rotation;
        if(out.controller() instanceof DroneAI ai) ai.link(unit);
        Events.fire(new UnitCreateEvent(out, null, unit));
        out.add();
        Units.notifyUnitSpawn(out);

        if(spawnEffect != Fx.none) spawnEffect.at(sX, sY, 0f, droneUnit);

        droneRef = out;
        data = out.id();
        timer = 0f;
    }
}
