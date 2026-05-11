package omaloon.world.blocks.distribution;

import arc.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.units.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.distribution.*;
import mindustry.world.meta.*;
import omaloon.annotations.Annotations.*;

import static mindustry.Vars.itemSize;

public class TubeRouter extends Router{
    public @Load("@-bottom") TextureRegion bottomRegion;
    public @Load("@-rotator") TextureRegion rotatorRegion;
    public @Load(value = "@-side#0$", lengths = {2}) TextureRegion[] sideRegion;

    public static final Interp itemInterp = t -> {
        float s = 2f * t - 1f;
        return 0.7f * s * s + 0.3f;
    };

    public TubeRouter(String name){
        super(name);
        rotate = true;
    }

    @Override
    public void drawPlanRegion(BuildPlan plan, Eachable<BuildPlan> list){
        Draw.rect(bottomRegion, plan.drawx(), plan.drawy());
        Draw.rect(rotatorRegion, plan.drawx(), plan.drawy());
        Draw.rect(region, plan.drawx(), plan.drawy());

        TextureRegion side = sideRegion[plan.rotation > 1 ? 1 : 0];
        if(side.found()) Draw.rect(side, plan.drawx(), plan.drawy(), plan.rotation * 90f);
    }

    @Override
    protected TextureRegion[] icons(){
        return new TextureRegion[]{
            Core.atlas.find(name + "-bottom"),
            Core.atlas.find(name + "-rotator"),
            region,
            Core.atlas.find(name + "-side0", name)
        };
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(Stat.itemsMoved, 60f / speed, StatUnit.itemsSecond);
    }

    public class TubeRouterBuild extends RouterBuild{
        public byte targetRot = (byte)rotation;
        public Building visualTarget = null;
        public float visualTurn = 0f;
        public float currentRotorAngle = 0f;

        @Override
        public boolean acceptItem(Building source, Item item){
            return super.acceptItem(source, item) && source != front();
        }

        @Override
        public boolean canControl(){
            return false;
        }

        protected float turnTo(int direction){
            return lastInput == null ? 0f : Mathf.mod(direction + 1 - relativeTo(lastInput), 4) - 1;
        }

        protected float itemDrawDistance(float angle, float distance, boolean hasDestination){
            if(hasDestination) return distance;

            float limit = Math.max(size * 4f - itemSize / 2f, 0f);
            float axis = Math.max(Math.abs(Mathf.cosDeg(angle)), Math.abs(Mathf.sinDeg(angle)));
            return axis <= 0.0001f ? distance : Math.min(distance, limit / axis);
        }

        public Building getPredictedTarget(Item item){
            int counter = targetRot;
            Building fallback = null;

            for(int i = 0; i < proximity.size; i++){
                Building other = proximity.get((i + counter) % proximity.size);
                if(lastInput != null && other.tile == lastInput) continue;

                if(other.acceptItem(this, item)) return other;
                if(fallback == null && other.team == team && other.block.group == BlockGroup.transportation) fallback = other;
            }

            return fallback;
        }

        @Override
        public void draw(){
            Draw.z(Layer.block - 0.2f);
            Draw.rect(bottomRegion, x, y);

            Draw.z(Layer.block - 0.1f);

            if(items.any() && lastInput != null){
                Building dest = visualTarget != null && visualTarget.isValid() ? visualTarget : null;

                float ctime = Mathf.clamp(time);
                float d = itemInterp.apply(ctime);
                currentRotorAngle = visualTurn * 90f * ctime;

                float angle = currentRotorAngle + relativeTo(lastInput) * 90f;
                float distance = itemDrawDistance(angle, size * 4f * d, dest != null && dest.acceptItem(this, lastItem));
                Draw.rect(items.first().uiIcon, x + Angles.trnsx(angle, distance), y + Angles.trnsy(angle, distance), itemSize, itemSize);
            }

            Draw.z(Layer.block);

            drawRotator(currentRotorAngle);
            Draw.rect(region, x, y);

            TextureRegion side = sideRegion[rotation > 1 ? 1 : 0];
            if(side.found()) Draw.rect(side, x, y, rotdeg());
        }

        public void drawRotator(float rotation){
            Drawf.spinSprite(rotatorRegion, x, y, rotation + 45f);
        }

        @Override
        public @Nullable Building getTileTarget(@Nullable Item item, Tile from, boolean set){
            if(item == null) return null;
            int counter = targetRot;

            for(int i = 0; i < proximity.size; i++){
                Building other = proximity.get((i + counter) % proximity.size);
                if(set) targetRot = (byte)((targetRot + 1) % proximity.size);

                if(other.tile == from && from.block() == Blocks.overflowGate) continue;
                if(other.acceptItem(this, item)) return other;
            }

            return null;
        }

        @Override
        public void updateTile(){
            if(lastItem == null && items.any()){
                lastItem = items.first();
                time = 0f;
                visualTarget = getPredictedTarget(lastItem);
                visualTurn = turnTo(visualTarget != null ? relativeTo(visualTarget) : rotation);
            }

            if(lastItem != null){
                time += 1f / speed * delta();

                Building target = getTileTarget(lastItem, lastInput, false);

                if(target != null && target != visualTarget){
                    visualTarget = target;
                    visualTurn = turnTo(relativeTo(visualTarget));
                }

                if(target != null && (time >= 1f || instantTransfer)){
                    getTileTarget(lastItem, lastInput, true);
                    target.handleItem(this, lastItem);
                    items.remove(lastItem, 1);
                    lastItem = null;
                    visualTarget = null;
                }
            }
        }
    }
}
