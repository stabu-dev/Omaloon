package omaloon.entities.abilities;

import arc.*;
import arc.func.*;
import arc.graphics.g2d.*;
import arc.math.geom.*;
import arc.scene.event.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.entities.units.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.meta.*;
import omaloon.ai.*;
import omaloon.core.*;
import omaloon.gen.*;
import omaloon.type.*;

public class DroneAbility extends Ability implements IClockedAbility{
    public static final int DRONE_SEARCH_TIME = 50;
    private static final Vec2[] EMPTY_VEC2_ARRAY = new Vec2[0];
    private final Vec2 calculatedSpawnPos = new Vec2();
    public String name = "omaloon-drone";
    public DroneUnitType droneUnit;
    public float spawnTime = 60f;
    public float spawnX = 0f;
    public float spawnY = 0f;
    public Effect spawnEffect = Fx.spawn;
    public boolean parentizeEffects = false;
    public Vec2[] anchorPos = EMPTY_VEC2_ARRAY;
    public float layer = Layer.groundUnit - 0.01f;
    public float rotation = 0f;
    public int maxDroneCount = 1;
    public Seq<Unit> drones = new Seq<>();
    public Func<Unit, DroneAI> droneController = DroneAI::new;
    protected float timer = 0f;
    protected float droneSearchTimer = DRONE_SEARCH_TIME;


    public DroneAbility(DroneUnitType droneUnit){
        this.droneUnit = droneUnit;
    }

    public DroneAbility(UnitType droneUnit){
        droneUnit(droneUnit);
    }

    private DroneAbility(){

    }

    public void droneUnit(UnitType droneType){
        if(!(droneType instanceof DroneUnitType drone)) throw new IllegalArgumentException("Expected " + DroneUnitType.class + " but found " + droneType.getClass());
        this.droneUnit = drone;
    }

    @Override
    public void init(UnitType type){
        this.data = 0;
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
        ability.drones = new Seq<>();
        return ability;
    }

    @Override
    public String localized(){
        return Core.bundle.get("ability." + name);
    }

    @Override
    public void update(Unit unit){
        calculateSpawnPos(unit);

        timer += Time.delta * Vars.state.rules.unitBuildSpeed(unit.team());

        if(drones.isEmpty()){
            if(data > 0){
                droneSearchTimer -= Time.delta;
                if(droneSearchTimer <= 0){
                    droneSearchTimer = DRONE_SEARCH_TIME;
                    data = 0;
                }
            }else{
                droneSearchTimer = DRONE_SEARCH_TIME;
            }


            //TODO mod groups
            //but I dont want to make PL into EntityAnno
            //this feature exits more than 1 or 2 years in MindustryModCore
            /*for(Unit u : Groups.unit){
                if(u.team() == unit.team()
                    && u.type == this.droneUnit
                    && u instanceof DroneUnit
                    && ((DroneUnit)u).owner == unit){
                    registerDrone(u.self(), unit);
                }
            }*/
        }else{
            droneSearchTimer = DRONE_SEARCH_TIME;
            updateAnchor(unit);//TODO better solution
        }

        drones.removeAll(u -> {
            if(!u.isValid()){
                data--;
                timer = 0;
                return true;
            }
            return false;
        });

        if(data < maxDroneCount){
            if(timer > spawnTime){
                spawnDrone(unit);
                timer = 0;
            }
        }
    }

    private Vec2 calculateSpawnPos(Unit unit){
        return calculatedSpawnPos.set(spawnX, spawnY).rotate(unit.rotation - 90f).add(unit);
    }

    protected void spawnDrone(Unit unit){
        calculateSpawnPos(unit);
        spawnEffect.at(calculatedSpawnPos.x, calculatedSpawnPos.y, 0f, parentizeEffects ? unit : null);

        Unit drone = droneUnit.create(unit.team());
        Dronec dronec = drone.self();
        drone.set(calculatedSpawnPos.x, calculatedSpawnPos.y);
        drone.rotation = unit.rotation + rotation;

        dronec.ownerID(unit.id);

        boolean isNotClient = !Vars.net.client();

        registerDrone(dronec, unit, isNotClient);
        for(int i = 0; i < unit.abilities.length; i++){
            Ability self = unit.abilities[i];
            if(self != this) continue;
            dronec.abilityIndex(i);
            break;
        }

        Events.fire(new UnitCreateEvent(drone, null, unit));
        if(isNotClient){
            drone.add();
        }
    }

    public void updateAnchor(Unit unit){
        for(int i = 0; i < drones.size; i++){
            Unit u = drones.get(i);
            UnitController controller = u.controller();
            DroneAI droneAI;
            if(controller instanceof DroneAI it){
                droneAI = it;
            }else{
                unit.controller(droneAI = new DroneAI(unit));
                controller.unit(Nulls.unit);
                droneAI.unit(u);
            }
            droneAI.rally(anchorPos[i]);
        }
    }

    @Override
    public void draw(Unit unit){
        calculateSpawnPos(unit);

        if(!(data < maxDroneCount) || !(timer <= spawnTime)) return;

        Draw.draw(layer, () -> Drawf.construct(calculatedSpawnPos.x, calculatedSpawnPos.y, droneUnit.fullIcon, unit.rotation - 90, timer / spawnTime, 1f, timer));
    }

    public boolean registerDrone(Dronec u, Unit owner, boolean addInList){
        Class<? extends DroneUnitType> aClass = droneUnit.getClass();
        if(!aClass.isInstance(u.type()))
            return false;
        if(addInList){
            int indexToReplace = drones.indexOf(it -> !it.isValid() || it == u);
            if(indexToReplace != -1){
                drones.set(indexToReplace, u.self());
            }else{
                if(data == maxDroneCount) return false;
                data++;
                drones.add((Unit)u);
            }
        }
        DroneAI controller = droneController.get(owner);
        u.controller(controller);
        updateAnchor(owner);
        if(addInList && u.whenWasUpdated() == OlTimer.clock){
            if(!u.dead()){
                if(Vars.net.client()){
                    controller.updateFromClient();
                }else{
                    controller.updateUnit();
                }
            }
        }
        return true;
    }
}