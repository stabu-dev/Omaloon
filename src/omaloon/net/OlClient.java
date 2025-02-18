package omaloon.net;

import arc.*;
import arc.struct.*;
import arc.util.pooling.Pool.*;
import lombok.*;
import mindustry.annotations.Annotations.*;
import mindustry.entities.units.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.world.blocks.ConstructBlock.*;
import mindustry.world.blocks.storage.CoreBlock.*;
import omaloon.gen.*;

import static mindustry.Vars.world;

public class OlClient{
    private static final Seq<SetPlanRequest> requests = new Seq<>(SetPlanRequest.class);

    static{
        Events.run(Trigger.preDraw, OlClient::handlePlanRequest);
    }

    private static void planRequest(@NonNull Dronec unit, BuildPlan plan){
        if(requests.size >= requests.items.length || requests.items[requests.size] == null){
            requests.add(new SetPlanRequest(unit, plan));
            return;
        }
        requests.items[requests.size].set(unit, plan);
        requests.size++;
    }

    private static void handlePlanRequest(){
        for(int i = 0; i < requests.size; i++){
            SetPlanRequest request = requests.items[i];
            Dronec unit = request.unit;
            unit.lastActive(request.plan);
            unit.buildAlpha(1f);
            request.reset();
        }
        requests.size = 0;
    }

    @Remote(called = Loc.client, targets = Loc.server)
    public static void utilityDroneSyncBuilding(Unit owner, Unit unit, BuildPlan rawPlan, Building core0, float bs){
        if(!(unit instanceof Dronec drone)) return;
        Queue<BuildPlan> ownerPlans = owner.plans;
        int expectedTilePos = rawPlan.tile().pos();
        BuildPlan plan = null;
        for(int i = 0; i < ownerPlans.size; i++){
            BuildPlan buildPlan = ownerPlans.get(i);
            if(buildPlan.tile().pos() != expectedTilePos) continue;
            plan = buildPlan;
        }
        if(plan == null) return;
        planRequest(drone, plan);
        if(!(world.build(plan.tile().pos()) instanceof ConstructBuild entity)) return;
        if(!(core0 instanceof CoreBuild core)) return;
        if(plan.breaking){
            entity.deconstruct(unit, core, bs);
        }else{
            entity.construct(unit, core, bs, plan.config);
        }
    }


    @AllArgsConstructor
    @NoArgsConstructor
    private static class SetPlanRequest implements Poolable{
        Dronec unit;
        BuildPlan plan;

        public SetPlanRequest set(Dronec unit, BuildPlan plan){
            this.unit = unit;
            this.plan = plan;
            return this;
        }

        @Override
        public void reset(){
            unit = null;
            plan = null;
        }
    }
}
