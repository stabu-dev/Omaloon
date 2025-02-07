package omaloon.entities.abilities;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.scene.event.*;
import arc.scene.ui.*;
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
import mindustry.world.meta.*;
import omaloon.ai.*;
import omaloon.gen.*;

import java.util.*;
import java.util.function.*;

public class DroneAbility extends Ability{
    private Unit paramUnit;
    private DroneAbility paramAbility;
    private final Vec2 paramPos = new Vec2();

    public String name = "omaloon-drone";
    public UnitType droneUnit;
    public float spawnTime = 60f;
    public float spawnX = 0f;
    public float spawnY = 0f;
    public Effect spawnEffect = Fx.spawn;
    public boolean parentizeEffects = false;
    public Vec2[] anchorPos = {new Vec2(5 * 8f, -5 * 8f)};
    public float layer = Layer.groundUnit - 0.01f;
    public float rotation = 0f;
    public int maxDroneCount = 1;
    protected float timer = 0f;
    public ArrayList<Unit> drones = new ArrayList<>();
    public Function<Unit, DroneAI> droneController = DroneAI::new;

    @Override
    public void init(UnitType type){
        this.data = 0;
    }

    public DroneAbility(){
    }

    @Override
    public void addStats(Table t){
        t.add("[lightgray]" + Stat.productionTime.localized() + ": []" + Strings.autoFixed(spawnTime, 2)).row();
        t.table(unit -> {
            Image icon = unit.image(droneUnit.fullIcon).get();
            icon.setScaling(Scaling.fit);
            icon.touchable = Touchable.enabled;
            icon.clicked(() -> Vars.ui.content.show(droneUnit));
            icon.addListener(new HandCursorListener());

            unit.row();
            unit.add(droneUnit.localizedName);
        }).row();
    }

    @Override
    public Ability copy(){
        DroneAbility ability = (DroneAbility)super.copy();
        ability.drones = new ArrayList<>();
        return ability;
    }

    @Override
    public String localized(){
        return Core.bundle.get("ability." + name);
    }

    @Override
    public void update(Unit unit){
        paramUnit = unit;
        paramAbility = this;
        paramPos.set(spawnX, spawnY).rotate(unit.rotation - 90f).add(unit);

        timer += Time.delta * Vars.state.rules.unitBuildSpeed(unit.team());

        if(drones.isEmpty()){
            //TODO mod groups
            //but I dont want to make PL into EntityAnno
            //this feature exits more than 1 or 2 years in MindustryModCore
            for(Unit u : Groups.unit){
                if(u.team() == unit.team()
                        && u.type == this.droneUnit
                        && u instanceof DroneUnit
                        && ((DroneUnit)u).owner == unit) {
                    drones.add(u);
                    u.controller(droneController.apply(unit));
                    data++;
                    updateAnchor();
                }
            }
        }else{
            updateAnchor();//TODO better solution
        }

        drones.removeIf(u -> {
            if(!u.isValid()){
                data--;
                timer = 0;
                return true;
            }
            return false;
        });

        if(data < maxDroneCount){
            if(timer > spawnTime){
                spawnDrone();
                timer = 0;
            }
        }
    }

    protected void spawnDrone(){
        spawnEffect.at(paramPos.x, paramPos.y, 0f, parentizeEffects ? paramUnit : null);
        Unit u = droneUnit.create(paramUnit.team());
        u.set(paramPos.x, paramPos.y);
        u.rotation = paramUnit.rotation + rotation;

        if(u instanceof DroneUnit drone) drone.owner(paramUnit);

        drones.add(0, u);
        data++;
        for(int i = 0; i < paramUnit.abilities.length; i++){
            Ability self = paramUnit.abilities[i];
            if(self == this && u instanceof Dronec drone) drone.abilityIndex(i);
        }
        u.controller(droneController.apply(paramUnit));
        updateAnchor();

        Events.fire(new UnitCreateEvent(u, null, paramUnit));
        if(!Vars.net.client()){
            u.add();
        }
    }

    public void updateAnchor(){
        for(int i = 0; i < drones.size(); i++){
            Unit u = drones.get(i);
            ((DroneAI)u.controller()).rally(anchorPos[i]);
        }
    }

    @Override
    public void draw(Unit unit){
        paramUnit = unit;
        paramAbility = this;
        paramPos.set(spawnX, spawnY).rotate(unit.rotation - 90f).add(unit);

        if(data < maxDroneCount && timer <= spawnTime){
            Draw.draw(layer, () -> Drawf.construct(paramPos.x, paramPos.y, droneUnit.fullIcon, paramUnit.rotation - 90, timer / spawnTime, 1f, timer));
        }
    }
}